package com.tourism.booking.payment.repository;

import com.tourism.booking.payment.entity.PaymentIntent;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentSpecifications {

    public static Specification<PaymentIntent> bookingIdEquals(Long bookingId) {
        return (root, query, cb) ->
                bookingId == null ? null :
                        cb.equal(root.get("bookingId"), bookingId);
    }

    public static Specification<PaymentIntent> statusEquals(PaymentIntent.PaymentStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    public static Specification<PaymentIntent> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null)
                return cb.between(root.get("amount"), min, max);
            if (min != null)
                return cb.greaterThanOrEqualTo(root.get("amount"), min);
            return cb.lessThanOrEqualTo(root.get("amount"), max);
        };
    }

    public static Specification<PaymentIntent> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null)
                return cb.between(root.get("createdAt"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}