package com.tama.payment.exception;

import com.tama.payment.model.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentException extends RuntimeException{
    ErrorCode errorCode;
    HttpStatus httpStatus;

}
