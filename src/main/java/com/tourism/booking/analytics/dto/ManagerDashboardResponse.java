package com.tourism.booking.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponse {

    private Long hotelId;
    private String from;
    private String to;

    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long pendingBookings;

    private BigDecimal totalRevenue;
    private BigDecimal refundedAmount;

    private BigDecimal cancellationRatePercent;
    private BigDecimal averageBookingValue;

    private Long mostBookedRoomTypeId;
    private BigDecimal occupancyRatePercent;
}

