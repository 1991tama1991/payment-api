package com.tama.payment.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @Builder.Default
    UUID id = UUID.randomUUID();

    double amount;

    @NotNull
    UUID recipient;

    @NotNull
    UUID sender;

    @NotNull
    @Builder.Default
    OffsetDateTime dateTime = OffsetDateTime.now();
}
