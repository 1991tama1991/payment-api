package com.tama.payment.service;

import com.tama.payment.exception.PaymentException;
import com.tama.payment.model.PaymentEvent;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static com.tama.payment.model.enums.ErrorCode.MESSAGE_SENDING_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventSenderComponentTest {

    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    @InjectMocks
    private PaymentEventSenderComponent paymentEventSenderComponent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentEventSenderComponent, "topicName", "test-topic");
    }

    @Test
    void sendEvent_validEvent_messageSentSuccessfully() {
        // Given
        PaymentEvent event = new PaymentEvent();
        CompletableFuture<SendResult<String, PaymentEvent>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));

        when(kafkaTemplate.send("test-topic", "payment", event))
                .thenReturn(future);

        // When & Then
        assertDoesNotThrow(() -> paymentEventSenderComponent.sendEvent(event));
    }

    @Test
    void sendEvent_kafkaThrowsException_shouldThrowPaymentException() {
        // Given
        PaymentEvent event = new PaymentEvent();
        CompletableFuture<SendResult<String, PaymentEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new KafkaException("Kafka failure"));

        when(kafkaTemplate.send("test-topic", "payment", event))
                .thenReturn(future);

        // When & Then
        PaymentException exception = assertThrows(PaymentException.class, () ->
                paymentEventSenderComponent.sendEvent(event));

        assertEquals(MESSAGE_SENDING_ERROR, exception.getErrorCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
    }
}
