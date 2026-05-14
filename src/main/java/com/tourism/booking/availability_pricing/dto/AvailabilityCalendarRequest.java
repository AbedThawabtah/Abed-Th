package com.tourism.booking.availability_pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request for availability calendar")
public class AvailabilityCalendarRequest {

    @NotNull
    @Schema(description = "Hotel ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long hotelId;

    @NotNull
    @Schema(description = "Room type ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roomTypeId;

    @NotNull
    @Schema(description = "Start date of the calendar", example = "2026-07-01")
    private LocalDate fromDate;

    @NotNull
    @Schema(description = "End date of the calendar", example = "2026-07-31")
    private LocalDate toDate;
}
