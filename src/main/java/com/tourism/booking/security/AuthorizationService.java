package com.tourism.booking.security;



import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final BookingRepository bookingRepository;
    private final AppUserRepository appUserRepository;

    public boolean ownsBooking(Long bookingId) {
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            return false;
        }
        AppUser user = appUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        return bookingRepository.findById(bookingId)
                .map(b -> ownsBookingEntity(user, b))
                .orElse(false);
    }

    public boolean isSelfEmail(String email) {
        if (email == null) {
            return false;
        }
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            return false;
        }
        return appUserRepository.findByUsername(username)
                .map(u -> u.getEmail().equalsIgnoreCase(email.trim()))
                .orElse(false);
    }

    private boolean ownsBookingEntity(AppUser user, Booking b) {
        if (b.getId() != null) {
            return b.getId().equals(user.getId());
        }
        return b.getGuestEmail() != null && b.getGuestEmail().equalsIgnoreCase(user.getEmail());
    }
}

