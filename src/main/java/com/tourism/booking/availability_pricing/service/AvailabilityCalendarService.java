package com.tourism.booking.availability_pricing.service;

import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.availability_pricing.dto.AvailabilityCalendarRequest;
import com.tourism.booking.availability_pricing.dto.AvailabilityCalendarResponse;
import com.tourism.booking.availability_pricing.dto.CalendarDayDto;
import com.tourism.booking.booking.repository.BookingRepository;
import com.tourism.booking.catalog.entity.RoomType;
import com.tourism.booking.catalog.repository.HotelRepository;
import com.tourism.booking.catalog.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityCalendarService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Transactional(readOnly = true)
    public AvailabilityCalendarResponse getCalendar(AvailabilityCalendarRequest request) {

        validateDates(request.getFromDate(), request.getToDate());

        if (!hotelRepository.existsById(request.getHotelId())) {
            throw new ResourceNotFoundException("Hotel", request.getHotelId());
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", request.getRoomTypeId()));

        if (!roomType.getHotel().getId().equals(request.getHotelId())) {
            throw new BusinessException("Room type does not belong to the selected hotel");
        }

        int totalRooms = roomType.getTotalRooms() != null ? roomType.getTotalRooms() : 1;

        List<CalendarDayDto> days = new ArrayList<>();

        LocalDate current = request.getFromDate();

        while (!current.isAfter(request.getToDate())) {

            LocalDate day = current;

            long used = bookingRepository.countBookingsCoveringNight(request.getRoomTypeId(), day);
            int remaining = (int) Math.max(0, totalRooms - used);
            boolean dayAvailable = remaining > 0;

            days.add(new CalendarDayDto(day, dayAvailable, remaining, totalRooms));

            current = current.plusDays(1);
        }

        return AvailabilityCalendarResponse.builder()
                .hotelId(request.getHotelId())
                .roomTypeId(request.getRoomTypeId())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .days(days)
                .build();
    }

    private void validateDates(LocalDate from, LocalDate to) {
        if (!to.isAfter(from)) {
            throw new BusinessException("toDate must be after fromDate");
        }
    }
}