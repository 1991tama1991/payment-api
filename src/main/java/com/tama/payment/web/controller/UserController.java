package com.tama.payment.web.controller;

import com.tama.payment.web.api.UserApi;
import com.tama.payment.service.UserService;
import com.tama.payment.web.model.UserCreateRequestDto;
import com.tama.payment.web.model.UserResponseDto;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController implements UserApi {

    UserService userService;

    @Override
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto userRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userRequest));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

}
