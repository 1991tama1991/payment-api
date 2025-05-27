package com.tama.payment.web.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "PaymentResponse", description = "A representation of payment response.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponseDto {

    UUID id;
    double amount;
    UUID recipient;
    UUID sender;
    OffsetDateTime dateTime;
}
