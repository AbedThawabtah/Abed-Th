package com.tourism.booking.payment.repository;

import com.tourism.booking.payment.entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long>, JpaSpecificationExecutor<PaymentIntent> {

    List<PaymentIntent> findByBookingId(Long bookingId);

    Optional<PaymentIntent> findTopByBookingIdOrderByCreatedAtDesc(Long bookingId); // لكي يتم جلب أحدث عملية دفع بناءً على تاريخ الإنشاء

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentIntent.PaymentStatus status); // للتحقق مما إذا كانت هناك عملية دفع بحالة معينة لعملية حجز معينة

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM PaymentIntent p
            WHERE p.status = :status
            AND p.createdAt >= :start AND p.createdAt < :end
            AND (:hotelId IS NULL OR EXISTS (
                SELECT 1 FROM Booking b WHERE b.id = p.bookingId AND b.hotelId = :hotelId))
            """)
    BigDecimal sumAmountInPeriod(
            @Param("status") PaymentIntent.PaymentStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("hotelId") Long hotelId);
}
