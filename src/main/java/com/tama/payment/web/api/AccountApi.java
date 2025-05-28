package com.tama.payment.web.api;


import com.tama.payment.web.model.request.AccountCreateRequestDto;
import com.tama.payment.web.model.response.AccountResponseDto;
import com.tama.payment.web.model.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "account", description = "The account API to be able to create and list test accounts")
public interface AccountApi {

   

    /**
     * GET /accounts
     * List all the accounts.
     *
     * @return List of available accounts. (status code 200)
     */
    @Operation(
            operationId = "listAccounts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of available accounts.",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AccountResponseDto.class))
                            })
            }
    )
    ResponseEntity<List<AccountResponseDto>> listAccounts();

    /**
     * POST /accounts
     * Create account.
     *
     * @param accountRequest (required)
     * @return Created account with account id. (status code 201)
     * or Request payload is invalid. (status code 400)
     */
    @Operation(
            operationId = "createAccount",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Created account with creation date-time and account id.",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AccountResponseDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "Request payload is invalid.",
                            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponseDto.class))})
            }
    )
    ResponseEntity<AccountResponseDto> createAccount(
            @Parameter(name = "AccountRequest", required = true) AccountCreateRequestDto accountRequest
    );
    
}
