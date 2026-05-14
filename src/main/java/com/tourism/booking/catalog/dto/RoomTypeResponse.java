package com.tourism.booking.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "Room type response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeResponse {

    private Long id;
    private Long hotelId;
    private String name;
    private String description;
    private Integer maxGuests;
    private BigDecimal basePricePerNight;
    private Integer totalRooms;
}
