package com.tourism.booking.availability_pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Availability calendar response")
public class AvailabilityCalendarResponse {

    @Schema(description = "Hotel ID")
    private Long hotelId;

    @Schema(description = "Room type ID")
    private Long roomTypeId;

    @Schema(description = "Start date of the calendar")
    private LocalDate fromDate;

    @Schema(description = "End date of the calendar")
    private LocalDate toDate;

    @Schema(description = "Daily availability list")
    private List<CalendarDayDto> days;
}
