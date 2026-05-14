package com.tourism.booking.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long hotelId;
    private Long customerUserId;
    private String customerUsername;
    private Long bookingId;
    private int rating;
    private String comment;
    private Instant createdAt;
}
