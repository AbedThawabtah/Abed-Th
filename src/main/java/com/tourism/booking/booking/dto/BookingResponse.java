package com.tourism.booking.booking.dto;


import com.tourism.booking.booking.entity.Booking;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Booking response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long hotelId;
    private Long roomTypeId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;
    private Booking.BookingStatus status;
    private String guestEmail;
    private String guestName;
    private BigDecimal totalAmount;

    @Schema(description = "Subtotal before promo discount")
    private BigDecimal originalAmount;

    @Schema(description = "Discount from promo")
    private BigDecimal discountAmount;

    @Schema(description = "Applied promo code, if any")
    private String appliedPromoCode;

    private Long customerUserId;

    private Instant createdAt;
    private Instant updatedAt;
}

// هنا يتم الرد مع الدفع
// هنا يتم الرد ولكن لم تكمل الإجراءات أي يجب الدفع بعد ذلك حتى تصبح الحالة كونفيرمد