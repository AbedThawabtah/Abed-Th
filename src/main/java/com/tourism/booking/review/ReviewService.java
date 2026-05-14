package com.tourism.booking.review;

import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.PagedResponse;
import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.audit.AuditAction;
import com.tourism.booking.audit.AuditLogService;
import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.repository.BookingRepository;
import com.tourism.booking.catalog.entity.Hotel;
import com.tourism.booking.catalog.repository.HotelRepository;
import com.tourism.booking.security.AppUser;
import com.tourism.booking.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuditLogService auditLogService;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse create(ReviewCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser customer = customUserDetailsService.loadAppUserByUsername(username);

        if (reviewRepository.existsByHotelIdAndCustomerId(request.getHotelId(), customer.getId())) {
            throw new BusinessException("You have already reviewed this hotel");
        }

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", request.getHotelId()));

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));
            if (!booking.getHotelId().equals(hotel.getId())) {
                throw new BusinessException("Booking does not belong to this hotel");
            }
            if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
                throw new BusinessException("Only confirmed bookings qualify for a review");
            }
            boolean emailMatch = customer.getEmail().equalsIgnoreCase(booking.getGuestEmail());
            boolean userMatch = booking.getCustomerUserId() != null && booking.getCustomerUserId().equals(customer.getId());
            if (!emailMatch && !userMatch) {
                throw new BusinessException("Booking does not belong to this customer");
            }
        } else {
            boolean hasConfirmed = bookingRepository.existsConfirmedForCustomerAndHotel(
                    customer.getId(), customer.getEmail(), hotel.getId());
            if (!hasConfirmed) {
                throw new BusinessException("A confirmed stay at this hotel is required to leave a review");
            }
        }

        Review review = Review.builder()
                .hotel(hotel)
                .customer(customer)
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);
        auditLogService.log(AuditAction.REVIEW_CREATED, "REVIEW", String.valueOf(review.getId()),
                "hotelId=" + hotel.getId());
        return reviewMapper.toResponse(review);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> listByHotel(
            Long hotelId,
            Pageable pageable) {

        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }

        Page<Review> page = reviewRepository
                .findByHotelId(hotelId, pageable);

        List<ReviewResponse> content = page.getContent()
                .stream()
                .map(reviewMapper::toResponse)
                .toList();

        return PagedResponse.from(page, content);
    }



    @Transactional(readOnly = true)
    public ReviewResponse getById(Long reviewId) {

        // 1️⃣ جلب المستخدم الحالي
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        AppUser current = customUserDetailsService
                .loadAppUserByUsername(username);

        boolean admin = current.getRole().name().equals("ADMIN");

        // 2️⃣ جلب الريفيو
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        // 3️⃣ التحقق من الصلاحية
        if (!admin && !review.getCustomer().getId().equals(current.getId())) {
            throw new BusinessException("You can only view your own review");
        }

        // 4️⃣ تحويل إلى Response
        return reviewMapper.toResponse(review);
    }


    @Transactional
    public void delete(Long reviewId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser current = customUserDetailsService.loadAppUserByUsername(username);
        boolean admin = current.getRole().name().equals("ADMIN");

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        if (!admin && !review.getCustomer().getId().equals(current.getId())) {
            throw new BusinessException("You can only delete your own review");
        }

        reviewRepository.delete(review);
        auditLogService.log(AuditAction.REVIEW_DELETED, "REVIEW", String.valueOf(reviewId), "by=" + username);
    }
}
