package com.tama.payment.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEvent {
    UUID id;
    double amount;
    UUID recipient;
    UUID sender;
    OffsetDateTime dateTime;
}
