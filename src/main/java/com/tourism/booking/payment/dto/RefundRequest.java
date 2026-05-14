package com.tourism.booking.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Request to create a refund")
@Data
public class RefundRequest {

    @NotNull
    @Schema(description = "ID of the payment intent to refund")
    private Long paymentIntentId;
}