package com.tama.payment.service.validation;

import com.tama.payment.exception.PaymentException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.tama.payment.model.enums.ErrorCode.TOO_LOW_BALANCE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class BalanceValidatorTest {

    private final BalanceValidator balanceValidator = new BalanceValidator();

    @ParameterizedTest
    @CsvSource({
            "100.0, 50.0",
            "100.0, 99.99",
            "100.0, 100.0",
            "200.0, 199.99"
    })
    void validate_validBalances_noException(double balance, double amount) {
        // Given

        // When / Then
        assertDoesNotThrow(() -> balanceValidator.validate(balance, amount));
    }

    @ParameterizedTest
    @CsvSource({
            "100.0, 100.01",
            "100.0, 150.0",
            "0.0, 0.01"
    })
    void validate_invalidBalances_throwsPaymentException(double balance, double amount) {
        // Given

        // When / Then
        PaymentException exception = assertThrows(PaymentException.class, () ->
                balanceValidator.validate(balance, amount));

        assertEquals(TOO_LOW_BALANCE, exception.getErrorCode());
        assertEquals(BAD_REQUEST, exception.getHttpStatus());
    }
}
