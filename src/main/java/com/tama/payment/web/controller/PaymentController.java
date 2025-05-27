package com.tama.payment.web.controller;

import com.tama.payment.service.PaymentService;
import com.tama.payment.web.api.PaymentApi;
import com.tama.payment.web.model.request.PaymentCreateRequestDto;
import com.tama.payment.web.model.response.PaymentResponseDto;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentController implements PaymentApi {

    PaymentService paymentService;

    @Override
    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(
            @Valid @RequestBody PaymentCreateRequestDto paymentRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(paymentRequest));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PaymentResponseDto>> listPayments() {
        return ResponseEntity.ok(paymentService.getPayments());
    }

}
