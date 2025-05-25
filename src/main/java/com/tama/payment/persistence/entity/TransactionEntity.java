package com.tama.payment.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "transaction")
public class TransactionEntity {

    @Id
    @Builder.Default
    UUID id = UUID.randomUUID();

    double amount;

    @NotNull
    UUID recipient;

    @NotNull
    UUID sender;
}
