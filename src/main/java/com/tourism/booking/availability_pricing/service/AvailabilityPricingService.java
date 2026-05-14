package com.tourism.booking.availability_pricing.service;

import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.availability_pricing.dto.AvailabilityPricingRequest;
import com.tourism.booking.availability_pricing.dto.AvailabilityPricingResponse;
import com.tourism.booking.booking.repository.BookingRepository;
import com.tourism.booking.catalog.entity.RoomType;
import com.tourism.booking.catalog.repository.HotelRepository;
import com.tourism.booking.catalog.repository.RoomTypeRepository;
import com.tourism.booking.promo.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AvailabilityPricingService {

    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.25");
    private static final BigDecimal WEEKDAY_MULTIPLIER = BigDecimal.ONE;

    private final BookingRepository bookingRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final PromoCodeService promoCodeService;

    @Transactional(readOnly = true)
    public AvailabilityPricingResponse getQuote(AvailabilityPricingRequest request) {

        validateDates(request.getCheckIn(), request.getCheckOut());

        if (!hotelRepository.existsById(request.getHotelId())) {
            throw new ResourceNotFoundException("Hotel", request.getHotelId());
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", request.getRoomTypeId()));

        if (!roomType.getHotel().getId().equals(request.getHotelId())) {
            throw new BusinessException("Room type does not belong to the selected hotel");
        }

        if (roomType.getMaxGuests() < request.getGuests()) {
            return AvailabilityPricingResponse.builder()
                    .available(false)
                    .availableRooms(0)
                    .totalRooms(roomType.getTotalRooms())
                    .message("Room max guests (" + roomType.getMaxGuests() +
                            ") is less than requested guests (" + request.getGuests() + ")")
                    .build();
        }

        int totalRooms = roomType.getTotalRooms() != null ? roomType.getTotalRooms() : 1;
        long overlapping = bookingRepository.countOverlappingInventoryBookings(
                request.getRoomTypeId(),
                request.getCheckIn(),
                request.getCheckOut());
        int availableRooms = (int) Math.max(0, totalRooms - overlapping);
        boolean available = availableRooms > 0;

        if (!available) {
            return AvailabilityPricingResponse.builder()
                    .available(false)
                    .availableRooms(0)
                    .totalRooms(totalRooms)
                    .message("No rooms left for the selected dates")
                    .build();
        }

        int weekdayNights = 0;
        int weekendNights = 0;

        LocalDate d = request.getCheckIn();
        while (d.isBefore(request.getCheckOut())) {
            if (d.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekendNights++;
            } else {
                weekdayNights++;
            }
            d = d.plusDays(1);
        }

        int nights = weekdayNights + weekendNights;
        BigDecimal base = roomType.getBasePricePerNight();

        BigDecimal subtotal =
                base.multiply(BigDecimal.valueOf(weekdayNights)).multiply(WEEKDAY_MULTIPLIER)
                        .add(base.multiply(BigDecimal.valueOf(weekendNights)).multiply(WEEKEND_MULTIPLIER));
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        PromoCodeService.PromoPricingResult promoResult =
                promoCodeService.applyPromo(request.getPromoCode(), subtotal, request.getCheckIn());

//        boolean pendingBlock = bookingRepository.existsOverlappingPendingBooking(
//                request.getRoomTypeId(),
//                request.getCheckIn(),
//                request.getCheckOut());
//        if (pendingBlock) {
//            return AvailabilityPricingResponse.builder()
//                    .available(false)
//                    .availableRooms(availableRooms)
//                    .totalRooms(totalRooms)
//                    .originalAmount(promoResult.originalAmount())
//                    .discountAmount(promoResult.discountAmount())
//                    .totalAmount(promoResult.finalAmount())
//                    .appliedPromoCode(promoResult.appliedPromoCode())
//                    .message("Another user is currently booking this room type. Please try again shortly.")
//                    .basePricePerNight(base)
//                    .nights(nights)
//                    .weekdayNights(weekdayNights)
//                    .weekendNights(weekendNights)
//                    .build();
//        }

        return AvailabilityPricingResponse.builder()
                .available(true)
                .availableRooms(availableRooms)
                .totalRooms(totalRooms)
                .message(promoResult.message())
                .originalAmount(promoResult.originalAmount())
                .discountAmount(promoResult.discountAmount())
                .totalAmount(promoResult.finalAmount())
                .appliedPromoCode(promoResult.appliedPromoCode())
                .basePricePerNight(base)
                .nights(nights)
                .weekdayNights(weekdayNights)
                .weekendNights(weekendNights)
                .build();
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessException("Check-out must be after check-in");
        }
    }
}
