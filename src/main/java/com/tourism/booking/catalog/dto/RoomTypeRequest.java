package com.tourism.booking.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Request to create or update a room type")
@Data
public class RoomTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    @Positive
    @Schema(description = "Maximum number of guests")
    private Integer maxGuests;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Schema(description = "Base price per night")
    private BigDecimal basePricePerNight;

    @NotNull
    @Positive
    @Schema(description = "Physical units of this room type")
    private Integer totalRooms;
}
