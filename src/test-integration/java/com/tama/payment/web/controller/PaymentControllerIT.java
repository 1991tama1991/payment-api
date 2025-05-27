package com.tama.payment.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tama.payment.component.ErrorResponseCheckerComponent;
import com.tama.payment.configuration.TestBrokerConfig;
import com.tama.payment.model.PaymentEvent;
import com.tama.payment.persistence.entity.PaymentEntity;
import com.tama.payment.persistence.repository.AccountRepository;
import com.tama.payment.persistence.repository.PaymentRepository;
import com.tama.payment.web.model.request.PaymentCreateRequestDto;
import com.tama.payment.web.model.response.ErrorResponseDto;
import com.tama.payment.web.model.response.PaymentResponseDto;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.tama.payment.model.enums.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("it")
@Transactional
@Sql(scripts = "/db/test-init/payment-controller-it.sql", executionPhase = BEFORE_TEST_METHOD)
@Import({TestBrokerConfig.class, ErrorResponseCheckerComponent.class})
@EmbeddedKafka(partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9096", "port=9096"})
class PaymentControllerIT {

    static final String BASE_URL = "/api/v1/payments";

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    Consumer<String, String> consumer;
    @Autowired
    ErrorResponseCheckerComponent errorResponseCheckerComponent;

//    @Autowired
//    ErrorResponseCheckerComponent errorResponseCheckerComponent;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void createPayment_withValidRequest_successfullyCreates() throws Exception {
        UUID senderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID recipientId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        double amount = 100.0;

        PaymentCreateRequestDto paymentCreateRequestDto = PaymentCreateRequestDto.builder()
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .build();

        long countBeforeRequest = paymentRepository.count();

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(paymentCreateRequestDto)))
                .andReturn()
                .getResponse();

        PaymentResponseDto paymentResponseDto = objectMapper.readValue(response.getContentAsString(),
                PaymentResponseDto.class);

        Optional<PaymentEntity> paymentEntity = paymentRepository.findById(paymentResponseDto.getId());

        paymentEntity.ifPresentOrElse(payment -> {

            assertEquals(paymentCreateRequestDto.getAmount(), paymentResponseDto.getAmount());
            assertEquals(paymentCreateRequestDto.getRecipient(), paymentResponseDto.getRecipient());
            assertEquals(paymentCreateRequestDto.getSender(), paymentResponseDto.getSender());

            assertNotNull(paymentResponseDto.getDateTime());
            assertEquals(paymentResponseDto.getDateTime().toInstant(), payment.getDateTime().toInstant());

            assertNotNull(paymentResponseDto.getId());
            assertEquals(paymentResponseDto.getId(), payment.getId());

            assertEquals(paymentCreateRequestDto.getAmount(), payment.getAmount());
            assertEquals(paymentCreateRequestDto.getSender(), payment.getSender());
            assertEquals(paymentCreateRequestDto.getRecipient(), payment.getRecipient());

        }, Assertions::fail);

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

        accountRepository.findById(senderId).ifPresentOrElse(senderEntity -> {
            assertEquals(400.0, senderEntity.getBalance());
        }, Assertions::fail);

        accountRepository.findById(recipientId).ifPresentOrElse(senderEntity -> {
            assertEquals(1100.0, senderEntity.getBalance());
        }, Assertions::fail);

        assertEquals(countBeforeRequest + 1, paymentRepository.count());

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "payment", Duration.of(3000, ChronoUnit.MILLIS));

        assertNotNull(record);
        assertNotNull(record.value());

        PaymentEvent paymentEvent = objectMapper.readValue(record.value(), PaymentEvent.class);

        paymentEntity.ifPresentOrElse(payment -> {
            assertEquals(paymentEvent.getSender(), payment.getSender());
            assertEquals(paymentEvent.getRecipient(), payment.getRecipient());
            assertEquals(paymentEvent.getAmount(), payment.getAmount());
            assertEquals(paymentEvent.getId(), payment.getId());
            assertEquals(paymentEvent.getDateTime().toInstant(), payment.getDateTime().toInstant());
        }, Assertions::fail);

    }

    @Test
    void createPayment_withInvalidRequest_paymentIsNotCreated() throws Exception {
        UUID recipientId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        double amount = 100.0;

        PaymentCreateRequestDto paymentCreateRequestDto = PaymentCreateRequestDto.builder()
                .sender(null)
                .recipient(recipientId)
                .amount(amount)
                .build();

        long countBeforeRequest = paymentRepository.count();

        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(paymentCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponseDto expectedErrorResponse = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(FORM_VALIDATION_ERROR.getMessage() + "sender - must not be null")
                .build();

        errorResponseCheckerComponent.checkErrorResponse(contentAsString, expectedErrorResponse);

        assertEquals(countBeforeRequest, paymentRepository.count());

        accountRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")).ifPresentOrElse(recipient ->
            assertEquals(1000.0, recipient.getBalance()), Assertions::fail
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                KafkaTestUtils.getSingleRecord(consumer, "payment", Duration.of(1000, ChronoUnit.MILLIS)));

        assertEquals("No records found for topic", exception.getMessage());
    }

    @Test
    void createPayment_withValidRequestNotExistingSender_paymentIsNotCreated() throws Exception {
        UUID recipientId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        double amount = 100.0;

        PaymentCreateRequestDto paymentCreateRequestDto = PaymentCreateRequestDto.builder()
                .sender(UUID.fromString("22222222-2222-2222-2222-222222222223"))
                .recipient(recipientId)
                .amount(amount)
                .build();

        long countBeforeRequest = paymentRepository.count();

        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(paymentCreateRequestDto)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponseDto expectedErrorResponse = ErrorResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ENTITY_NOT_FOUND.getMessage())
                .build();

        errorResponseCheckerComponent.checkErrorResponse(contentAsString, expectedErrorResponse);

        assertEquals(countBeforeRequest, paymentRepository.count());

        accountRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")).ifPresentOrElse(recipient ->
                assertEquals(1000.0, recipient.getBalance()), Assertions::fail
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                KafkaTestUtils.getSingleRecord(consumer, "payment", Duration.of(1000, ChronoUnit.MILLIS)));

        assertEquals("No records found for topic", exception.getMessage());
    }

    @Test
    void createPayment_withValidRequestNotExistingRecipient_paymentIsNotCreated() throws Exception {
        UUID senderId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        double amount = 100.0;

        PaymentCreateRequestDto paymentCreateRequestDto = PaymentCreateRequestDto.builder()
                .sender(senderId)
                .recipient(UUID.fromString("22222222-2222-2222-2222-222222222223"))
                .amount(amount)
                .build();

        long countBeforeRequest = paymentRepository.count();

        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(paymentCreateRequestDto)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponseDto expectedErrorResponse = ErrorResponseDto.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ENTITY_NOT_FOUND.getMessage())
                .build();

        errorResponseCheckerComponent.checkErrorResponse(contentAsString, expectedErrorResponse);

        assertEquals(countBeforeRequest, paymentRepository.count());

        accountRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")).ifPresentOrElse(sender ->
                assertEquals(1000.0, sender.getBalance()), Assertions::fail
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                KafkaTestUtils.getSingleRecord(consumer, "payment", Duration.of(1000, ChronoUnit.MILLIS)));

        assertEquals("No records found for topic", exception.getMessage());
    }

    @Test
    void createPayment_withValidRequestNotEnoughBalance_paymentIsNotCreated() throws Exception {
        UUID senderId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID recipientId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        double amount = 1000.0;

        PaymentCreateRequestDto paymentCreateRequestDto = PaymentCreateRequestDto.builder()
                .sender(senderId)
                .recipient(recipientId)
                .amount(amount)
                .build();

        long countBeforeRequest = paymentRepository.count();

        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(paymentCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponseDto expectedErrorResponse = ErrorResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(TOO_LOW_BALANCE.getMessage())
                .build();

        errorResponseCheckerComponent.checkErrorResponse(contentAsString, expectedErrorResponse);

        assertEquals(countBeforeRequest, paymentRepository.count());

        accountRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")).ifPresentOrElse(sender ->
                assertEquals(1000.0, sender.getBalance()), Assertions::fail
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                KafkaTestUtils.getSingleRecord(consumer, "payment", Duration.of(1000, ChronoUnit.MILLIS)));

        assertEquals("No records found for topic", exception.getMessage());
    }
}
