package com.tama.payment.service;

import com.tama.payment.model.PaymentEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEventSenderComponent {

    final static String KEY = "payment";

    @Value("${payment.topic}")
    String topicName;

    final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendEvent(PaymentEvent event){
        kafkaTemplate.send(topicName, KEY , event);

        log.debug("Producer sent the message {}", event);
    }

}
