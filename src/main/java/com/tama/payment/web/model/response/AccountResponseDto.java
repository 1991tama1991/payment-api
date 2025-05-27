package com.tama.payment.web.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Schema(name = "AccountResponse", description = "A representation of account response.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponseDto {

    UUID id;
    double balance;
}
