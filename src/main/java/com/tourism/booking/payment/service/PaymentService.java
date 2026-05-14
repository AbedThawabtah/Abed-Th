package com.tourism.booking.payment.service;

import com.tourism.booking.exception.BusinessException;
import com.tourism.booking.PagedResponse;
import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.audit.AuditAction;
import com.tourism.booking.audit.AuditLogService;
import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.repository.BookingRepository;
import com.tourism.booking.payment.dto.PaymentIntentRequest;
import com.tourism.booking.payment.dto.PaymentIntentResponse;
import com.tourism.booking.payment.dto.PaymentMapper;
import com.tourism.booking.payment.entity.PaymentIntent;
import com.tourism.booking.payment.repository.PaymentIntentRepository;
import com.tourism.booking.payment.repository.PaymentSpecifications;
import com.tourism.booking.security.CustomUserDetailsService;
import com.tourism.booking.security.Role;
import com.tourism.booking.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentServiceInterface{

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentMapper paymentMapper;
    private final AuditLogService auditLogService;
    private final BookingRepository bookingRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public PaymentIntentResponse createIntent(PaymentIntentRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));
        var actor = customUserDetailsService.loadAppUserByUsername(SecurityUtils.currentUsername());
        if (actor.getRole() == Role.CUSTOMER) {
            boolean owns = booking.getCustomerUserId() != null && booking.getCustomerUserId().equals(actor.getId())
                    || actor.getEmail().equalsIgnoreCase(booking.getGuestEmail());
            if (!owns) {
                throw new BusinessException("You can only pay for your own bookings");
            }
        }

        boolean alreadyPaid =
                paymentIntentRepository.existsByBookingIdAndStatus(
                        request.getBookingId(),
                        PaymentIntent.PaymentStatus.SUCCESS
                );

        if (alreadyPaid) {
            throw new BusinessException("Booking already paid");
        }

        if(!request.getAmount().equals(booking.getTotalAmount())){
            throw new BusinessException("Payment amount must match booking total price");
        }

        PaymentIntent intent = PaymentIntent.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .status(PaymentIntent.PaymentStatus.CREATED)
                .build();

        intent = paymentIntentRepository.save(intent);

        auditLogService.log(AuditAction.PAYMENT_INTENT_CREATED, "PAYMENT_INTENT", String.valueOf(intent.getId()),
                "bookingId=" + request.getBookingId());

        return paymentMapper.toResponse(intent);
    }

    @Transactional
    public PaymentIntentResponse simulateSuccess(Long id) {

        PaymentIntent intent = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentIntent", id));

        assertCustomerOwnsBooking(intent.getBookingId());

        if (intent.getStatus() != PaymentIntent.PaymentStatus.CREATED) {
            throw new BusinessException("Only CREATED intents can be simulated");
        }

        intent.setStatus(PaymentIntent.PaymentStatus.SUCCESS);

        intent = paymentIntentRepository.save(intent);

        auditLogService.log(AuditAction.PAYMENT_SUCCESS, "PAYMENT_INTENT", String.valueOf(id), null);

        return paymentMapper.toResponse(intent);
    }

    @Transactional
    public PaymentIntentResponse simulateFailure(Long id) {

        PaymentIntent intent = paymentIntentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentIntent", id));

        assertCustomerOwnsBooking(intent.getBookingId());

        if (intent.getStatus() != PaymentIntent.PaymentStatus.CREATED) {
            throw new BusinessException("Only CREATED intents can be simulated");
        }

        intent.setStatus(PaymentIntent.PaymentStatus.FAILED);

        intent = paymentIntentRepository.save(intent);

        auditLogService.log(AuditAction.PAYMENT_FAILURE, "PAYMENT_INTENT", String.valueOf(id), null);

        return paymentMapper.toResponse(intent);
    }

    @Transactional
    public PaymentIntentResponse refund(Long paymentIntentId) {

        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentIntent", paymentIntentId));

        if (intent.getStatus() != PaymentIntent.PaymentStatus.SUCCESS) {
            throw new BusinessException("Only SUCCESS payments can be refunded");
        }

        intent.setStatus(PaymentIntent.PaymentStatus.REFUNDED);

        intent = paymentIntentRepository.save(intent);

        auditLogService.log(AuditAction.REFUND_CREATED, "PAYMENT_INTENT", String.valueOf(paymentIntentId), null);

        return paymentMapper.toResponse(intent);
    }

    // -----------------------------
    // Browse Payments (Manager)
    // -----------------------------

    @Transactional(readOnly = true)
    public PagedResponse<PaymentIntentResponse> browsePayments(
            Long bookingId,
            PaymentIntent.PaymentStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant fromDate,
            Instant toDate,
            Pageable pageable) {

        Specification<PaymentIntent> spec = Specification
                .where(PaymentSpecifications.bookingIdEquals(bookingId))
                .and(PaymentSpecifications.statusEquals(status))
                .and(PaymentSpecifications.amountBetween(minAmount, maxAmount))
                .and(PaymentSpecifications.createdBetween(fromDate, toDate));

        Page<PaymentIntent> page = paymentIntentRepository.findAll(spec, pageable);

        List<PaymentIntentResponse> content = page.getContent()
                .stream()
                .map(paymentMapper::toResponse)
                .toList();

        return PagedResponse.from(page, content);
    }

    private void assertCustomerOwnsBooking(Long bookingId) {
        var actor = customUserDetailsService.loadAppUserByUsername(SecurityUtils.currentUsername());
        if (actor.getRole() != Role.CUSTOMER) {
            return;
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        boolean owns = booking.getCustomerUserId() != null && booking.getCustomerUserId().equals(actor.getId())
                || actor.getEmail().equalsIgnoreCase(booking.getGuestEmail());
        if (!owns) {
            throw new BusinessException("You can only operate on payments for your own bookings");
        }
    }
}