package com.tourism.booking.security.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank
    @Schema(description = "Refresh token returned from login")
    private String refreshToken;
}
