package com.tama.payment.web.errorhandling;

import com.tama.payment.exception.PaymentException;
import com.tama.payment.web.model.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        HttpStatus httpStatus = BAD_REQUEST;

        List<String> errorDetailsResponses = createMethodArgumentErrorMessage(exception);

        return new ResponseEntity<>(ErrorResponseDto.builder()
                .statusCode(httpStatus.value())
                .message((String.join(",", errorDetailsResponses)))
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

    private List<String> createMethodArgumentErrorMessage(MethodArgumentNotValidException exception) {
        return exception.getAllErrors().stream()
                .filter(Objects::nonNull)
                .map(this::createMethodArgumentErrorMessage)
                .toList();
    }

    private String createMethodArgumentErrorMessage(ObjectError error) {
        Optional<String> fieldName = provideFieldName(error);

        return FORM_VALIDATION_ERROR.getMessage() + provideMessage(error, fieldName)
                .orElse(error.getDefaultMessage());
    }

    private Optional<String> provideMessage(ObjectError error, Optional<String> fieldName) {
        return fieldName.map(field -> field + " - " + error.getDefaultMessage());
    }

    private Optional<String> provideFieldName(ObjectError error) {
        return Arrays.stream(error.getArguments())
                .findFirst()
                .map(this::provideFieldName);
    }

    private String provideFieldName(Object object) {
        if (object instanceof DefaultMessageSourceResolvable defaultMessageSourceResolvable) {
            String fieldName = defaultMessageSourceResolvable.getDefaultMessage();
            return StringUtils.isNotBlank(fieldName)
                    ? fieldName
                    : null;
        }

        return null;
    }
}
