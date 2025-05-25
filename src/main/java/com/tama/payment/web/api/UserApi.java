package com.tama.payment.web.api;


import com.tama.payment.web.model.UserCreateRequestDto;
import com.tama.payment.web.model.UserResponseDto;
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

@Tag(name = "user", description = "The user API to be able to create test users")
public interface UserApi {

   

    /**
     * GET /users
     * List all the users.
     *
     * @return List of available users. (status code 200)
     */
    @Operation(
            operationId = "listUsers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of available users.",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UserResponseDto.class))
                            })
            }
    )
    ResponseEntity<List<UserResponseDto>> listUsers();

    /**
     * POST /users
     * Create user.
     *
     * @param userRequest (required)
     * @return Created user with user id. (status code 201)
     * or Request payload is invalid. (status code 400)
     */
    @Operation(
            operationId = "createUser",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Created user with creation date-time and user id.",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = UserResponseDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "Request payload is invalid.",
                            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))})
            }
    )
    ResponseEntity<UserResponseDto> createUser(
            @Parameter(name = "UserRequest", required = true) UserCreateRequestDto userRequest
    );
    
}
