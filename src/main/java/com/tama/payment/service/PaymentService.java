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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.tama.payment.model.enums.ErrorCode.ENTITY_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentService {
    PaymentRepository paymentRepository;
    AccountRepository accountRepository;
    ModelMapper modelMapper;
    BalanceValidator balanceValidator;
    PaymentEventSenderComponent paymentEventSenderComponent;

    @Transactional
    public PaymentResponseDto createPayment(PaymentCreateRequestDto paymentRequest) {

        // todo check order
        List<UUID> accounts = orderAccountsById(paymentRequest);

        AccountEntity firstLockedAccount = getAccountEntity(accounts, 0);
        AccountEntity secondLockedAccount = getAccountEntity(accounts, 1);

        Double amount = paymentRequest.getAmount();

        if (firstLockedAccount.getId().equals(paymentRequest.getSender())) {
            transferMoney(firstLockedAccount, amount, secondLockedAccount);
        } else {
            transferMoney(secondLockedAccount, amount, firstLockedAccount);
        }

        PaymentEntity savedPayment = paymentRepository.save(modelMapper.map(paymentRequest, PaymentEntity.class));

        log.debug("Payment: {} has been created.", savedPayment);

        paymentEventSenderComponent.sendEvent(modelMapper.map(savedPayment, PaymentEvent.class));

        return modelMapper.map(savedPayment, PaymentResponseDto.class);
    }

    public List<PaymentResponseDto> getPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(paymentEntity -> modelMapper.map(paymentEntity, PaymentResponseDto.class))
                .toList();
    }

    private AccountEntity getAccountEntity(List<UUID> accounts, int index) {
        return accountRepository.findByIdForUpdate(accounts.get(index))
                .orElseThrow(() -> new PaymentException(ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private List<UUID> orderAccountsById(PaymentCreateRequestDto paymentRequest) {
        return Stream.of(paymentRequest.getSender(), paymentRequest.getRecipient())
                .sorted()
                .toList();
    }

    private void transferMoney(AccountEntity sender, Double amount, AccountEntity recipient) {
        double balanceOfSender = sender.getBalance();

        balanceValidator.validate(balanceOfSender, amount);

        sender.setBalance(balanceOfSender - amount);
        recipient.setBalance(recipient.getBalance() + amount);
    }

}
