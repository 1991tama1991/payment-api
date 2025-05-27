package com.tama.payment.web.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Schema(name = "ErrorResponse", description = "A representation of error response.")

@Data
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ErrorResponse{

    String message;
    int statusCode;
}
