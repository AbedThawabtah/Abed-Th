package com.tourism.booking.availability_pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;


@Schema(description = "Parameters for Availability&Pricing check")
@Data
public class AvailabilityPricingRequest {

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long hotelId;

    @NotNull
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomTypeId;

    @NotNull
    @Schema(description = "Check-in date")
    private LocalDate checkIn;

    @NotNull
    @Schema(description = "Check-out date")
    private LocalDate checkOut;

    @NotNull
    @Positive
    @Schema(description = "Number of guests")
    private Integer guests;

    @Schema(description = "Optional promo code to validate against the quote")
    private String promoCode;
}
