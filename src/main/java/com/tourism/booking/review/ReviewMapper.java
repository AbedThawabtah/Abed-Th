package com.tourism.booking.review;

import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .hotelId(r.getHotel().getId())
                .customerUserId(r.getCustomer().getId())
                .customerUsername(r.getCustomer().getUsername())
                .bookingId(r.getBooking() != null ? r.getBooking().getId() : null)
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
