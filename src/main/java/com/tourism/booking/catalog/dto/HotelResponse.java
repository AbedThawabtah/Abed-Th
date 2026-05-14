package com.tourism.booking.catalog.dto;

import com.tourism.booking.review.ReviewResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Hotel response (with optional room types)")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponse {

    @Schema(description = "Hotel ID")
    private Long id;

    private String name;
    private String city;
    private String address;
    private Integer starRating;
    private String description;

    @Schema(description = "Public URL to fetch hotel image (null when not uploaded)")
    private String imageUrl;

    @Schema(description = "Included when viewing hotel details")
    private List<RoomTypeResponse> roomTypes;

    @Schema(description = "Average guest rating (1-5)")
    private Double averageRating;

    @Schema(description = "Number of reviews")
    private Long reviewsCount;

    @Schema(description = "Recent reviews when viewing hotel details")
    private List<ReviewResponse> recentReviews;
}