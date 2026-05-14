package com.tourism.booking;

import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.repository.BookingRepository;
import com.tourism.booking.catalog.entity.Hotel;
import com.tourism.booking.catalog.entity.RoomType;
import com.tourism.booking.catalog.repository.HotelRepository;
import com.tourism.booking.catalog.repository.RoomTypeRepository;
import com.tourism.booking.payment.entity.PaymentIntent;
import com.tourism.booking.payment.repository.PaymentIntentRepository;
import com.tourism.booking.promo.DiscountType;
import com.tourism.booking.promo.PromoCode;
import com.tourism.booking.promo.PromoCodeRepository;
import com.tourism.booking.review.Review;
import com.tourism.booking.review.ReviewRepository;
import com.tourism.booking.security.AppUser;
import com.tourism.booking.security.AppUserRepository;
import com.tourism.booking.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            PromoCodeRepository promoCodeRepository,
            BookingRepository bookingRepository,
            PaymentIntentRepository paymentIntentRepository,
            ReviewRepository reviewRepository) {

        return args -> {

            // ✅ Idempotent: skip if data already exists
            if (appUserRepository.count() > 0) {
                log.info("Database already seeded — skipping.");
                return;
            }

            AppUser admin = appUserRepository.save(AppUser.builder()
                    .username("admin")
                    .email("admin@booking.local")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());

            AppUser manager = appUserRepository.save(AppUser.builder()
                    .username("manager")
                    .email("manager@booking.local")
                    .passwordHash(passwordEncoder.encode("manager123"))
                    .role(Role.MANAGER)
                    .build());

            AppUser customer = appUserRepository.save(AppUser.builder()
                    .username("customer")
                    .email("customer@test.com")
                    .passwordHash(passwordEncoder.encode("customer123"))
                    .role(Role.CUSTOMER)
                    .build());

            log.info("Seeded users: admin (id={}), manager (id={}), customer (id={})",
                    admin.getId(), manager.getId(), customer.getId());

            Hotel h1 = new Hotel();
            h1.setName("Royal Hotel");
            h1.setCity("Nablus");
            h1.setAddress("City Center");
            h1.setStarRating(4);
            h1.setDescription("Modern hotel in the city center");
            h1.setOwners(List.of(manager, admin));
            hotelRepository.save(h1);

            Hotel h2 = new Hotel();
            h2.setName("Sea View Resort");
            h2.setCity("Gaza");
            h2.setAddress("Beach Road");
            h2.setStarRating(5);
            h2.setDescription("Luxury resort with sea view");
            hotelRepository.save(h2);

            Hotel h3 = new Hotel();
            h3.setName("Mountain Lodge");
            h3.setCity("Ramallah");
            h3.setAddress("Mountain Area");
            h3.setStarRating(3);
            h3.setDescription("Quiet place near the mountains");
            hotelRepository.save(h3);

            RoomType r1 = new RoomType();
            r1.setName("Standard Room");
            r1.setDescription("Comfortable room with WiFi");
            r1.setMaxGuests(2);
            r1.setBasePricePerNight(new BigDecimal("80.00"));
            r1.setTotalRooms(10);
            r1.setHotel(h1);
            roomTypeRepository.save(r1);

            RoomType r2 = new RoomType();
            r2.setName("Deluxe Room");
            r2.setDescription("Spacious room with city view");
            r2.setMaxGuests(3);
            r2.setBasePricePerNight(new BigDecimal("120.00"));
            r2.setTotalRooms(5);
            r2.setHotel(h1);
            roomTypeRepository.save(r2);

            RoomType r3 = new RoomType();
            r3.setName("Suite");
            r3.setDescription("Luxury suite with sea view");
            r3.setMaxGuests(4);
            r3.setBasePricePerNight(new BigDecimal("250.00"));
            r3.setTotalRooms(3);
            r3.setHotel(h2);
            roomTypeRepository.save(r3);

            promoCodeRepository.save(PromoCode.builder()
                    .code("SAVE10")
                    .discountType(DiscountType.PERCENTAGE)
                    .discountValue(new BigDecimal("10"))
                    .validFrom(LocalDate.now().minusMonths(1))
                    .validTo(LocalDate.now().plusMonths(6))
                    .minBookingAmount(new BigDecimal("50"))
                    .maxUses(100)
                    .currentUses(0)
                    .active(true)
                    .build());

            promoCodeRepository.save(PromoCode.builder()
                    .code("FLAT25")
                    .discountType(DiscountType.FIXED)
                    .discountValue(new BigDecimal("25.00"))
                    .validFrom(LocalDate.now().minusMonths(1))
                    .validTo(LocalDate.now().plusMonths(6))
                    .minBookingAmount(new BigDecimal("100"))
                    .maxUses(50)
                    .currentUses(0)
                    .active(true)
                    .build());

            LocalDate pastIn = LocalDate.now().minusDays(14);
            LocalDate pastOut = LocalDate.now().minusDays(12);
            Booking confirmedPast = Booking.builder()
                    .hotelId(h1.getId())
                    .roomTypeId(r1.getId())
                    .checkIn(pastIn)
                    .checkOut(pastOut)
                    .guests(2)
                    .guestEmail(customer.getEmail())
                    .guestName("Seeded Customer")
                    .status(Booking.BookingStatus.CONFIRMED)
                    .totalAmount(new BigDecimal("160.00"))
                    .customerUserId(customer.getId())
                    .originalAmount(new BigDecimal("160.00"))
                    .discountAmount(BigDecimal.ZERO)
                    .build();
            confirmedPast = bookingRepository.save(confirmedPast);

            Booking upcoming = Booking.builder()
                    .hotelId(h1.getId())
                    .roomTypeId(r1.getId())
                    .checkIn(LocalDate.now().plusDays(7))
                    .checkOut(LocalDate.now().plusDays(10))
                    .guests(2)
                    .guestEmail(customer.getEmail())
                    .guestName("Seeded Customer")
                    .status(Booking.BookingStatus.CONFIRMED)
                    .totalAmount(new BigDecimal("240.00"))
                    .customerUserId(customer.getId())
                    .originalAmount(new BigDecimal("240.00"))
                    .discountAmount(BigDecimal.ZERO)
                    .build();
            upcoming = bookingRepository.save(upcoming);

            PaymentIntent paid = PaymentIntent.builder()
                    .bookingId(upcoming.getId())
                    .amount(upcoming.getTotalAmount())
                    .status(PaymentIntent.PaymentStatus.SUCCESS)
                    .build();
            paymentIntentRepository.save(paid);

            reviewRepository.save(Review.builder()
                    .hotel(h1)
                    .customer(customer)
                    .booking(confirmedPast)
                    .rating(5)
                    .comment("Excellent stay, great service.")
                    .build());

            log.info("Sample hotels, room types, promos, bookings, and reviews loaded");
        };
    }
}