package com.tourism.booking.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "Request to create a booking")
@Data
public class BookingRequest {

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long hotelId;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomTypeId;

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

    @NotNull
    @Positive
    private Integer guests;

    @NotBlank
    @Email
    @Size(max = 200)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String guestEmail;

    @NotBlank
    @Size(max = 100)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String guestName;

    @Schema(description = "Optional promo code (must match quote)")
    private String promoCode;
}
