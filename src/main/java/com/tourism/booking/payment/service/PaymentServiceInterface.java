package com.tourism.booking.payment.service;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.payment.dto.PaymentIntentRequest;
import com.tourism.booking.payment.dto.PaymentIntentResponse;
import com.tourism.booking.payment.entity.PaymentIntent;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentServiceInterface {

    public PaymentIntentResponse createIntent(PaymentIntentRequest request);
    public PaymentIntentResponse simulateSuccess(Long id);
    public PaymentIntentResponse simulateFailure(Long id);
    public PaymentIntentResponse refund(Long paymentIntentId);
    public PagedResponse<PaymentIntentResponse> browsePayments(
            Long bookingId,
            PaymentIntent.PaymentStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant fromDate,
            Instant toDate,
            Pageable pageable);
}
