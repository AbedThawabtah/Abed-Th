package com.tourism.booking.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Request to create a payment intent")
@Data
public class PaymentIntentRequest {

    @Schema(description = "Booking ID related to this payment")
    @NotNull
    private Long bookingId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;
}