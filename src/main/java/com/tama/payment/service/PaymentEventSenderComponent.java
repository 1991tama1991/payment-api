package com.tama.payment.service;

import com.tama.payment.exception.PaymentException;
import com.tama.payment.model.PaymentEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.tama.payment.model.enums.ErrorCode.MESSAGE_SENDING_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEventSenderComponent {

    final static String KEY = "payment";

    @Value("${payment.topic}")
    String topicName;

    final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendEvent(PaymentEvent event) {
        CompletableFuture<SendResult<String, PaymentEvent>> future = kafkaTemplate.send(topicName, KEY, event);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.debug("Producer sent the message {}", event);
            } else {
                log.error("Error during sending the notification", exception);
                throw new PaymentException(MESSAGE_SENDING_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });

    }

}
