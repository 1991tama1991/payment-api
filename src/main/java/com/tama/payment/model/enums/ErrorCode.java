package com.tama.payment.model.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum ErrorCode {

    ENTITY_NOT_FOUND("Entity is not found."),
    TOO_LOW_BALANCE("Balance is too low for the payment"),

    UNEXPECTED_ERROR("Unexpected error happened."),
    FORM_VALIDATION_ERROR("Form is not valid: "),

    REQUEST_IS_NOT_VALID("Request is not valid");

    String message;
}
