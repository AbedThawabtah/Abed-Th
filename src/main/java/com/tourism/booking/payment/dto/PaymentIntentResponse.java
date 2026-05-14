package com.tourism.booking.payment.dto;

import com.tourism.booking.payment.entity.PaymentIntent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment intent response")
public class PaymentIntentResponse {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentIntent.PaymentStatus status;
    private Instant createdAt;
}