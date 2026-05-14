package com.tourism.booking.promo.dto;

import com.tourism.booking.promo.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Create or update promo code")
public class PromoCodeRequest {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal discountValue;

    @NotNull
    private LocalDate validFrom;

    @NotNull
    private LocalDate validTo;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal minBookingAmount;

    @Min(1)
    private Integer maxUses;

    private Boolean active;
}
