package com.tourism.booking.availability_pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Availability information for a single day")
public class CalendarDayDto {

    @Schema(description = "Date")
    private LocalDate date;

    @Schema(description = "Whether at least one room is free that night")
    private boolean available;

    @Schema(description = "Rooms still available that night")
    private int availableRooms;

    @Schema(description = "Total inventory for the room type")
    private int totalRooms;
}