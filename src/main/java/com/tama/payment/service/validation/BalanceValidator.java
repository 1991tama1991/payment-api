package com.tama.payment.service.validation;

import com.tama.payment.exception.PaymentException;
import com.tama.payment.model.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class BalanceValidator {

    public void validate(double balance, double amount) {
        if (balance - amount < 0.00) {
            throw new PaymentException(ErrorCode.TOO_LOW_BALANCE, HttpStatus.BAD_REQUEST);
        }
    }

}
