package com.tourism.booking.availability_pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of Availability&Pricing check")
public class AvailabilityPricingResponse {

    @Schema(description = "Whether at least one room is available for the given dates")
    private boolean available;

    @Schema(description = "Remaining bookable units for the date range")
    private Integer availableRooms;

    @Schema(description = "Total physical inventory for this room type")
    private Integer totalRooms;

    @Schema(description = "Human-readable reason when not available or promo message")
    private String message;

    @Schema(description = "Price before discount")
    private BigDecimal originalAmount;

    @Schema(description = "Discount applied from promo")
    private BigDecimal discountAmount;

    @Schema(description = "Final amount after discount (same as charged total)")
    private BigDecimal totalAmount;

    @Schema(description = "Promo code that was applied, if any")
    private String appliedPromoCode;

    @Schema(description = "Base price per night (before multipliers)")
    private BigDecimal basePricePerNight;

    @Schema(description = "Number of nights")
    private int nights;

    @Schema(description = "Weekday nights count")
    private int weekdayNights;

    @Schema(description = "Weekend nights count")
    private int weekendNights;

}
