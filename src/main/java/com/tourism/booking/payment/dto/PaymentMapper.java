package com.tourism.booking.payment.dto;

import com.tourism.booking.payment.entity.PaymentIntent;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentIntentResponse toResponse(PaymentIntent intent) {
        return PaymentIntentResponse.builder()
                .id(intent.getId())
                .bookingId(intent.getBookingId())
                .amount(intent.getAmount())
                .status(intent.getStatus())
                .createdAt(intent.getCreatedAt())
                .build();
    }
}
