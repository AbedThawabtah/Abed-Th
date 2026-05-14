package com.tourism.booking;

import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingExpirationJob {

    private final BookingRepository bookingRepository;

    @Scheduled(fixedRate = 300000) // كل 5 دقائق
    public void cancelExpiredPendingBookings() {

        Instant expirationTime = Instant.now().minus(Duration.ofMinutes(30));

        List<Booking> expired =
                bookingRepository.findExpiredPendingBookings(expirationTime);

        for (Booking booking : expired) {
            booking.setStatus(Booking.BookingStatus.CANCELLED);
        }

        bookingRepository.saveAll(expired);
    }
}