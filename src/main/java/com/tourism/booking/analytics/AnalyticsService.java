package com.tourism.booking.analytics;

import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.analytics.dto.ManagerDashboardResponse;
import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.repository.BookingRepository;
import com.tourism.booking.catalog.repository.RoomTypeRepository;
import com.tourism.booking.payment.entity.PaymentIntent;
import com.tourism.booking.payment.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final BookingRepository bookingRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Transactional(readOnly = true)
    public ManagerDashboardResponse managerDashboard(Long hotelId, LocalDate from, LocalDate to) {
        if (!to.isAfter(from)) {
            throw new BusinessException("'to' must be after 'from'");
        }

        Instant start = from.atStartOfDay(ZONE).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(ZONE).toInstant();

        long total = bookingRepository.countCreatedInPeriod(start, end, hotelId);
        long confirmed = bookingRepository.countByStatusCreatedInPeriod(start, end, hotelId, Booking.BookingStatus.CONFIRMED);
        long cancelled = bookingRepository.countByStatusCreatedInPeriod(start, end, hotelId, Booking.BookingStatus.CANCELLED);
        long pending = bookingRepository.countByStatusCreatedInPeriod(start, end, hotelId, Booking.BookingStatus.PENDING);

        BigDecimal revenue = paymentIntentRepository.sumAmountInPeriod(PaymentIntent.PaymentStatus.SUCCESS, start, end, hotelId);
        BigDecimal refunded = paymentIntentRepository.sumAmountInPeriod(PaymentIntent.PaymentStatus.REFUNDED, start, end, hotelId);

        BigDecimal cancelRate = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(cancelled)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        BigDecimal avgValue = confirmed == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(confirmed), 2, RoundingMode.HALF_UP);

        List<Long> topRoom = bookingRepository.findMostBookedRoomTypeIds(start, end, hotelId, PageRequest.of(0, 1));
        Long mostBookedRoomTypeId = topRoom.isEmpty() ? null : topRoom.getFirst();

        BigDecimal occupancy = computeOccupancy(hotelId, from, to);

        return ManagerDashboardResponse.builder()
                .hotelId(hotelId)
                .from(from.toString())
                .to(to.toString())
                .totalBookings(total)
                .confirmedBookings(confirmed)
                .cancelledBookings(cancelled)
                .pendingBookings(pending)
                .totalRevenue(revenue)
                .refundedAmount(refunded)
                .cancellationRatePercent(cancelRate)
                .averageBookingValue(avgValue)
                .mostBookedRoomTypeId(mostBookedRoomTypeId)
                .occupancyRatePercent(occupancy)
                .build();
    }

    private BigDecimal computeOccupancy(Long hotelId, LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days <= 0) {
            return null;
        }

        long inventory;
        if (hotelId != null) {
            inventory = roomTypeRepository.sumTotalRoomsByHotelId(hotelId);
        } else {
            inventory = roomTypeRepository.sumTotalRoomsAllHotels();
        }
        if (inventory == 0) {
            return null;
        }

        LocalDate periodEndExclusive = to.plusDays(1);
        List<Booking> stays = bookingRepository.findConfirmedOverlappingStay(hotelId, from, to);
        long soldNights = 0;
        for (Booking b : stays) {
            LocalDate s = b.getCheckIn().isBefore(from) ? from : b.getCheckIn();
            LocalDate e = b.getCheckOut().isBefore(periodEndExclusive) ? b.getCheckOut() : periodEndExclusive;
            if (e.isAfter(s)) {
                soldNights += ChronoUnit.DAYS.between(s, e);
            }
        }

        long capacityNights = inventory * days;
        if (capacityNights == 0) {
            return null;
        }
        return BigDecimal.valueOf(soldNights)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(capacityNights), 2, RoundingMode.HALF_UP);
    }
}

