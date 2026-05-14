package com.tourism.booking.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "Request to create or update a hotel")
@Data
public class HotelRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Size(max = 500)
    private String address;

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Star rating 1-5")
    private Integer starRating;

    @Size(max = 2000)
    private String description;
}

    