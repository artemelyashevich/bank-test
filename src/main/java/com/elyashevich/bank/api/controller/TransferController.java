package com.elyashevich.bank.api.controller;

import com.elyashevich.bank.api.dto.exception.ExceptionBodyDto;
import com.elyashevich.bank.api.dto.transfer.TransferDto;
import com.elyashevich.bank.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer Operations", description = "API for money transfer operations")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    @Operation(
            summary = "Perform money transfer",
            description = "Transfer money between authenticated user and another user account",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer completed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid transfer request",
                            content = @Content(schema = @Schema(implementation = ExceptionBodyDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - authentication required"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - insufficient funds or other business rule violation"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Recipient account not found"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<Void> perform(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transfer details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransferDto.class))
            )
            @Valid @RequestBody TransferDto dto) {
        var userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        this.transferService.transfer(userId, dto.toUserId(), dto.amount());
        return ResponseEntity.ok().build();
    }
}