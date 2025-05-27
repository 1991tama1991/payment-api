package com.tama.payment.web.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Schema(name = "ErrorResponseDto", description = "A representation of error response.")

@Data
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ErrorResponseDto {

    String message;
    int statusCode;
}
