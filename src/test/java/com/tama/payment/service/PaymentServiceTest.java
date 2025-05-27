package com.tama.payment.service;

import com.tama.payment.exception.PaymentException;
import com.tama.payment.model.PaymentEvent;
import com.tama.payment.persistence.entity.AccountEntity;
import com.tama.payment.persistence.entity.PaymentEntity;
import com.tama.payment.persistence.repository.AccountRepository;
import com.tama.payment.persistence.repository.PaymentRepository;
import com.tama.payment.service.validation.BalanceValidator;
import com.tama.payment.web.model.request.PaymentCreateRequestDto;
import com.tama.payment.web.model.response.PaymentResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tama.payment.model.enums.ErrorCode.ENTITY_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private BalanceValidator balanceValidator;
    @Mock
    private PaymentEventSenderComponent paymentEventSenderComponent;
    @InjectMocks
    private PaymentService paymentService;

    @Captor
    ArgumentCaptor<PaymentEntity> paymentEntityArgumentCaptor;

    @Captor
    ArgumentCaptor<PaymentEvent> paymentEventCaptor;

    @Test
    void createPayment_validInput_successfulPayment() {
        // Given
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        double amount = 100.0;

        double balanceOfSender = 500.0;
        AccountEntity senderAccount = AccountEntity.builder()
                .id(senderId)
                .balance(balanceOfSender)
                .build();

        AccountEntity recipientAccount = AccountEntity.builder()
                .id(recipientId)
                .balance(200.0)
                .build();

        PaymentCreateRequestDto requestDto = PaymentCreateRequestDto.builder()
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .build();

        PaymentEntity mappedEntity = PaymentEntity.builder()
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .build();

        OffsetDateTime dateTimeFromDb = OffsetDateTime.now();
        UUID paymentIdFromDb = UUID.randomUUID();

        PaymentEntity savedEntity = PaymentEntity.builder()
                .id(paymentIdFromDb)
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .dateTime(dateTimeFromDb)
                .build();

        PaymentEvent paymentEvent = PaymentEvent.builder()
                .id(paymentIdFromDb)
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .dateTime(dateTimeFromDb)
                .build();

        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .id(paymentIdFromDb)
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .dateTime(dateTimeFromDb)
                .build();

        when(accountRepository.findByIdForUpdate(senderId))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(recipientId))
                .thenReturn(Optional.of(recipientAccount));
        when(paymentRepository.save(mappedEntity))
                .thenReturn(savedEntity);

        when(modelMapper.map(requestDto, PaymentEntity.class))
                .thenReturn(mappedEntity);
        when(modelMapper.map(savedEntity, PaymentEvent.class))
                .thenReturn(paymentEvent);
        when(modelMapper.map(savedEntity, PaymentResponseDto.class))
                .thenReturn(responseDto);

        doNothing().when(balanceValidator)
                .validate(balanceOfSender, amount);

        // When
        PaymentResponseDto actualResponse = paymentService.createPayment(requestDto);

        // Then
        verify(modelMapper).map(savedEntity, PaymentEvent.class);

        verify(paymentRepository).save(paymentEntityArgumentCaptor.capture());
        verify(paymentEventSenderComponent).sendEvent(paymentEventCaptor.capture());

        PaymentEntity capturedEntity = paymentEntityArgumentCaptor.getValue();
        PaymentEvent capturedEvent = paymentEventCaptor.getValue();

        assertEquals(capturedEntity, mappedEntity);
        assertEquals(capturedEvent, paymentEvent);

        assertEquals(responseDto, actualResponse);

        assertEquals(requestDto.getSender(), actualResponse.getSender());
        assertEquals(requestDto.getRecipient(), actualResponse.getRecipient());
        assertEquals(requestDto.getAmount(), actualResponse.getAmount());

        assertEquals(savedEntity.getId(), actualResponse.getId());
        assertEquals(savedEntity.getDateTime(), actualResponse.getDateTime());

        assertNotNull(actualResponse.getId());
        assertNotNull(actualResponse.getDateTime());

        assertEquals(400.0, senderAccount.getBalance());
        assertEquals(300.0, recipientAccount.getBalance());
    }

    @Test
    void createPayment_accountNotFound_throwsException() {
        // Given
        PaymentCreateRequestDto requestDto = PaymentCreateRequestDto.builder()
                .sender(UUID.randomUUID())
                .recipient(UUID.randomUUID())
                .amount(100.0)
                .build();

        when(accountRepository.findByIdForUpdate(any())).thenReturn(Optional.empty());

        // When / Then
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentService.createPayment(requestDto));

        assertEquals(ENTITY_NOT_FOUND, exception.getErrorCode());
        assertEquals(NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void getPayments_paymentsExist_returnsMappedList() {
        // Given
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        double amount = 100.0;
        OffsetDateTime dateTimeFromDb = OffsetDateTime.now();
        UUID paymentIdFromDb = UUID.randomUUID();

        PaymentEntity savedEntity = PaymentEntity.builder()
                .id(paymentIdFromDb)
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .dateTime(dateTimeFromDb)
                .build();

        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .id(paymentIdFromDb)
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .dateTime(dateTimeFromDb)
                .build();

        when(paymentRepository.findAll()).thenReturn(List.of(savedEntity));
        when(modelMapper.map(savedEntity, PaymentResponseDto.class))
                .thenReturn(responseDto);

        // When
        List<PaymentResponseDto> result = paymentService.getPayments();

        // Then
        assertEquals(1, result.size());
        PaymentResponseDto actualResponse = result.get(0);

        assertEquals(savedEntity.getSender(), actualResponse.getSender());
        assertEquals(savedEntity.getRecipient(), actualResponse.getRecipient());
        assertEquals(savedEntity.getAmount(), actualResponse.getAmount());
        assertEquals(savedEntity.getId(), actualResponse.getId());
        assertEquals(savedEntity.getDateTime(), actualResponse.getDateTime());
    }

}
