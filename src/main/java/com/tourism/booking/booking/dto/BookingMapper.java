package com.tourism.booking.booking.dto;

import com.tourism.booking.booking.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .hotelId(booking.getHotelId())
                .roomTypeId(booking.getRoomTypeId())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guests(booking.getGuests())
                .status(booking.getStatus())
                .guestEmail(booking.getGuestEmail())
                .guestName(booking.getGuestName())
                .totalAmount(booking.getTotalAmount())
                .originalAmount(booking.getOriginalAmount())
                .discountAmount(booking.getDiscountAmount())
                .appliedPromoCode(booking.getAppliedPromoCode())
                .customerUserId(booking.getCustomerUserId())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}