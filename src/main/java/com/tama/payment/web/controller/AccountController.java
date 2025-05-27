package com.tama.payment.web.controller;

import com.tama.payment.web.api.AccountApi;
import com.tama.payment.service.AccountService;
import com.tama.payment.web.model.request.AccountCreateRequestDto;
import com.tama.payment.web.model.response.AccountResponseDto;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AccountController implements AccountApi {

    AccountService accountService;

    @Override
    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(
            @Valid @RequestBody AccountCreateRequestDto accountRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(accountRequest));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> listAccounts() {
        return ResponseEntity.ok(accountService.getAccounts());
    }

}
