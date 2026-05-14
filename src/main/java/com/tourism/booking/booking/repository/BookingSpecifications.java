package com.tourism.booking.booking.repository;

import com.tourism.booking.booking.entity.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BookingSpecifications {

    public static Specification<Booking> hotelEquals(Long hotelId) {
        return (root, query, cb) ->
                hotelId == null ? null :
                        cb.equal(root.get("hotelId"), hotelId);
    }

    public static Specification<Booking> roomTypeEquals(Long roomTypeId) {
        return (root, query, cb) ->
                roomTypeId == null ? null :
                        cb.equal(root.get("roomTypeId"), roomTypeId);
    }

    public static Specification<Booking> emailEquals(String email) {
        return (root, query, cb) ->
                email == null ? null :
                        cb.equal(cb.lower(root.get("guestEmail")), email.toLowerCase());
    }

    public static Specification<Booking> statusEquals(Booking.BookingStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> checkInBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null)
                return cb.between(root.get("checkIn"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("checkIn"), from);
            return cb.lessThanOrEqualTo(root.get("checkIn"), to);
        };
    }
}