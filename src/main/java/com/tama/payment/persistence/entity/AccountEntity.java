package com.tama.payment.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Builder.Default
    UUID id = UUID.randomUUID();

    @Builder.Default
    double balance = 0;
}
