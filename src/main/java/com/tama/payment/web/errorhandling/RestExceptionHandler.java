package com.tama.payment.web.errorhandling;

import com.tama.payment.exception.PaymentException;
import com.tama.payment.web.model.response.ErrorResponseDto;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tama.payment.model.enums.ErrorCode.FORM_VALIDATION_ERROR;
import static com.tama.payment.model.enums.ErrorCode.UNEXPECTED_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler({PaymentException.class})
    public ResponseEntity<ErrorResponseDto> handlePaymentException(PaymentException exception) {

        return new ResponseEntity<>(ErrorResponseDto.builder()
                .message(exception.getErrorCode().getMessage())
                .statusCode(exception.getHttpStatus().value())
                .build(), exception.getHttpStatus());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        HttpStatus httpStatus = BAD_REQUEST;

        return new ResponseEntity<>(ErrorResponseDto.builder()
                .message(FORM_VALIDATION_ERROR.getMessage())
                .statusCode(httpStatus.value())
                .build(), httpStatus);
    }

    // todo check it is thrown
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException exception) {
        HttpStatus httpStatus = BAD_REQUEST;

        return new ResponseEntity<>(ErrorResponseDto.builder()
                .message(FORM_VALIDATION_ERROR.getMessage())
                .statusCode(httpStatus.value())
                .build(), httpStatus);
    }

    @ExceptionHandler({Throwable.class})
    public ResponseEntity<ErrorResponseDto> handleThrowable(Throwable throwable) {
        HttpStatus internalServerError = INTERNAL_SERVER_ERROR;

        log.error("Unexpected exception: ", throwable);

        return new ResponseEntity<>(ErrorResponseDto.builder()
                .message(UNEXPECTED_ERROR.getMessage())
                .statusCode(internalServerError.value())
                .build(), internalServerError);
    }

}
