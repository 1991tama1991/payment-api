package com.tama.payment.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tama.payment.web.model.response.ErrorResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestComponent
@RequiredArgsConstructor
public class ErrorResponseCheckerComponent {

    private final ObjectMapper objectMapper;

    public void checkErrorResponse(String responseString,
                                   ErrorResponseDto expectedErrorResponseDto) throws JsonProcessingException {
        ErrorResponseDto actualErrorResponseDto = objectMapper.readValue(responseString, ErrorResponseDto.class);

        assertEquals(expectedErrorResponseDto.getStatusCode(), actualErrorResponseDto.getStatusCode());
        assertEquals(expectedErrorResponseDto.getMessage(), actualErrorResponseDto.getMessage());
    }

}
