package com.tourism.booking.booking.service;

import com.tourism.booking.catalog.entity.Hotel;
import com.tourism.booking.catalog.repository.HotelRepository;
import com.tourism.booking.catalog.repository.RoomTypeRepository;
import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.PagedResponse;
import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.audit.AuditAction;
import com.tourism.booking.audit.AuditLogService;
import com.tourism.booking.availability_pricing.dto.AvailabilityPricingRequest;
import com.tourism.booking.availability_pricing.service.AvailabilityPricingService;
import com.tourism.booking.booking.dto.BookingMapper;
import com.tourism.booking.booking.dto.BookingRequest;
import com.tourism.booking.booking.dto.BookingResponse;
import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.repository.BookingRepository;
import com.tourism.booking.booking.repository.BookingSpecifications;
import com.tourism.booking.notification.service.NotificationService;
import com.tourism.booking.payment.entity.PaymentIntent;
import com.tourism.booking.payment.repository.PaymentIntentRepository;
import com.tourism.booking.promo.PromoCodeService;
import com.tourism.booking.security.CustomUserDetailsService;
import com.tourism.booking.security.Role;
import com.tourism.booking.security.SecurityUtils;
import com.tourism.booking.security.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements BookingServiceInterface {

    private static final int CANCELLATION_HOURS_BEFORE_CHECKIN = 48;

    private final BookingRepository bookingRepository;
    private final AvailabilityPricingService availabilityPricingService;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final PaymentIntentRepository paymentIntentRepository;
    private final AuditLogService auditLogService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PromoCodeService promoCodeService;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;


    // ---------------------------------------------------
    // Create Booking
    // ---------------------------------------------------


    @Transactional
    public BookingResponse createBooking(BookingRequest request) {

        AppUser actor = customUserDetailsService.loadAppUserByUsername(SecurityUtils.currentUsername());
        if (actor.getRole() == Role.CUSTOMER
                && !actor.getEmail().equalsIgnoreCase(request.getGuestEmail().trim())) {
            throw new BusinessException("Guest email must match your account email");
        }

        AvailabilityPricingRequest quoteRequest = new AvailabilityPricingRequest();
        quoteRequest.setHotelId(request.getHotelId());
        quoteRequest.setRoomTypeId(request.getRoomTypeId());
        quoteRequest.setCheckIn(request.getCheckIn());
        quoteRequest.setCheckOut(request.getCheckOut());
        quoteRequest.setGuests(request.getGuests());
        quoteRequest.setPromoCode(request.getPromoCode());

        var quote = availabilityPricingService.getQuote(quoteRequest);

        if (!quote.isAvailable()) {
            throw new BusinessException(quote.getMessage() != null ? quote.getMessage() : "Room not available");
        }


        long overlapping = bookingRepository.countOverlappingInventoryBookings(
                request.getRoomTypeId(),
                request.getCheckIn(),
                request.getCheckOut()
        );

        int totalRooms = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow()
                .getTotalRooms();

        if (overlapping >= totalRooms) {
            throw new BusinessException("No rooms available for the selected dates");
        }



        Long customerUserId = actor.getRole() == Role.CUSTOMER ? actor.getId() : null;

        Booking booking = Booking.builder()
                .hotelId(request.getHotelId())
                .roomTypeId(request.getRoomTypeId())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .guests(request.getGuests())
                .guestEmail(request.getGuestEmail())
                .guestName(request.getGuestName())
                .status(Booking.BookingStatus.PENDING)
                .totalAmount(quote.getTotalAmount())
                .customerUserId(customerUserId)
                .originalAmount(quote.getOriginalAmount())
                .discountAmount(quote.getDiscountAmount())
                .appliedPromoCode(quote.getAppliedPromoCode())
                .build();

        booking = bookingRepository.save(booking);
        auditLogService.log(AuditAction.BOOKING_CREATED, "BOOKING", String.valueOf(booking.getId()),
                "hotelId=" + booking.getHotelId());

        return bookingMapper.toResponse(booking);
    }


    // ---------------------------------------------------
    // Confirm Booking
    // ---------------------------------------------------


    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {

        AppUser actor = customUserDetailsService.loadAppUserByUsername(SecurityUtils.currentUsername());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        final long b = booking.getHotelId();
        Hotel hotel = hotelRepository.findById(booking.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", b));

        if (hotel.getOwners() == null || hotel.getOwners().stream().noneMatch(owner -> owner.getId().equals(actor.getId()))) {
            throw new BusinessException("Hotel owner must exist");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BusinessException("Booking can only be confirmed when in PENDING status");
        }

        boolean paid = paymentIntentRepository
                .existsByBookingIdAndStatus(bookingId, PaymentIntent.PaymentStatus.SUCCESS);

        if (!paid) {
            throw new BusinessException("Booking cannot be confirmed before successful payment");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        booking = bookingRepository.save(booking);

        promoCodeService.incrementUsageIfPresent(booking.getAppliedPromoCode());

        notificationService.sendBookingConfirmation(booking);

        auditLogService.log(AuditAction.BOOKING_CONFIRMED, "BOOKING", String.valueOf(bookingId), null);

        return bookingMapper.toResponse(booking);
    }


    // ---------------------------------------------------
    // Cancel Booking
    // ---------------------------------------------------

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {

        AppUser actor = customUserDetailsService.loadAppUserByUsername(SecurityUtils.currentUsername());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!actor.getId().equals(booking.getCustomerUserId()))     {
            throw new BusinessException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }

        LocalDateTime cancelDeadline =
                booking.getCheckIn()
                        .atStartOfDay()
                        .minusHours(CANCELLATION_HOURS_BEFORE_CHECKIN);

        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new BusinessException("Cancellation allowed only before 48 hours of check-in");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);

        booking = bookingRepository.save(booking);

        notificationService.sendBookingCancellation(booking);

        auditLogService.log(AuditAction.BOOKING_CANCELLED, "BOOKING", String.valueOf(bookingId), null);

        return bookingMapper.toResponse(booking);
    }


    // ---------------------------------------------------
    // Guest Booking History
    // ---------------------------------------------------

    @Transactional(readOnly = true)
    public List<BookingResponse> getGuestHistory(String guestEmail) {
        return bookingRepository
                .findByGuestEmailOrderByCreatedAtDesc(guestEmail)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        AppUser user = customUserDetailsService.loadAppUserByUsername(SecurityUtils.currentUsername());
        return bookingRepository.findByCustomerUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }


    // ---------------------------------------------------
    // Manager Upcoming Bookings
    // ---------------------------------------------------

    @Transactional(readOnly = true)
    public List<BookingResponse> getManagerUpcoming(Long hotelId) {
        return bookingRepository
                .findUpcomingByHotel(hotelId, LocalDate.now())
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }


    // ---------------------------------------------------
    // Browse Bookings (Filtering + Pagination)
    // ---------------------------------------------------

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> browseBookings(
            Long hotelId,
            Long roomTypeId,
            String email,
            Booking.BookingStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        Specification<Booking> spec = Specification
                .where(BookingSpecifications.hotelEquals(hotelId))
                .and(BookingSpecifications.roomTypeEquals(roomTypeId))
                .and(BookingSpecifications.emailEquals(email))
                .and(BookingSpecifications.statusEquals(status))
                .and(BookingSpecifications.checkInBetween(fromDate, toDate));

        Page<Booking> page = bookingRepository.findAll(spec, pageable);

        List<BookingResponse> content = page.getContent()
                .stream()
                .map(bookingMapper::toResponse)
                .toList();

        return PagedResponse.from(page, content);
    }
}