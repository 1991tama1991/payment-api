package com.tama.payment.web.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Schema(name = "PaymentCreateRequest", description = "A representation of payment request while creating the entity.")
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequestDto {

    @NotNull
    @Positive
    Double amount;

    @NotNull
    UUID recipient;

    @NotNull
    UUID sender;
}
