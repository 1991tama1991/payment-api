package com.tama.payment.service;

import com.tama.payment.model.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        when(kafkaTemplate.send("test-topic", "payment", event))
                .thenReturn(future);

        // When & Then
        future.complete(mock(SendResult.class));

        assertDoesNotThrow(() -> paymentEventSenderComponent.sendEvent(event));
    }
}
