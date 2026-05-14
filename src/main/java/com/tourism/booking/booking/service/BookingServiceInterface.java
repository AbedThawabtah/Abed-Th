package com.tourism.booking.booking.service;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.booking.dto.BookingRequest;
import com.tourism.booking.booking.dto.BookingResponse;
import com.tourism.booking.booking.entity.Booking;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingServiceInterface {
    public BookingResponse createBooking(BookingRequest request);
    public BookingResponse confirmBooking(Long bookingId);
    public BookingResponse cancelBooking(Long bookingId);
    public List<BookingResponse> getGuestHistory(String guestEmail);
    public List<BookingResponse> getManagerUpcoming(Long hotelId);
    public List<BookingResponse> getMyBookings();
    public PagedResponse<BookingResponse> browseBookings
            (
            Long hotelId,
            Long roomTypeId,
            String email,
            Booking.BookingStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable);
}
