package com.tourism.booking.notification.repository;

import com.tourism.booking.notification.entity.Notification;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class NotificationSpecifications {

    public static Specification<Notification> bookingIdEquals(Long bookingId) {
        return (root, query, cb) ->
                bookingId == null ? null :
                        cb.equal(root.get("bookingId"), bookingId);
    }

    public static Specification<Notification> emailEquals(String email) {
        return (root, query, cb) ->
                email == null ? null :
                        cb.equal(cb.lower(root.get("recipientEmail")), email.toLowerCase());
    }

    public static Specification<Notification> typeEquals(Notification.NotificationType type) {
        return (root, query, cb) ->
                type == null ? null :
                        cb.equal(root.get("type"), type);
    }

    public static Specification<Notification> sentBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null)
                return cb.between(root.get("sentAt"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("sentAt"), from);
            return cb.lessThanOrEqualTo(root.get("sentAt"), to);
        };
    }
}