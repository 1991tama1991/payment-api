package com.tama.payment.web.api;


import com.tama.payment.web.model.request.PaymentCreateRequestDto;
import com.tama.payment.web.model.response.PaymentResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;

import java.util.List;

@Tag(name = "payment", description = "The payment API to be able to create and list payments")
public interface PaymentApi {

   

    /**
     * GET /payments
     * List all the payments.
     *
     * @return List of available payments. (status code 200)
     */
    @Operation(
            operationId = "listPayments",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of available payments.",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = PaymentResponseDto.class))
                            })
            }
    )
    ResponseEntity<List<PaymentResponseDto>> listPayments();

    /**
     * POST /payments
     * Create payment.
     *
     * @param paymentRequest (required)
     * @return Created payment with payment id. (status code 201)
     * or Request payload is invalid. (status code 400)
     */
    @Operation(
            operationId = "createPayment",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Created payment with creation date-time and payment id.",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = PaymentResponseDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "Request payload is invalid.",
                            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))})
            }
    )
    ResponseEntity<PaymentResponseDto> createPayment(
            @Parameter(name = "PaymentRequest", required = true) PaymentCreateRequestDto paymentRequest
    );
    
}
