package com.tourism.booking.catalog.repository;

import com.tourism.booking.catalog.entity.Hotel;
import org.springframework.data.jpa.domain.Specification;

public class HotelSpecifications {

    public static Specification<Hotel> cityEquals(String city) {
        return (root, query, cb) ->
                city == null ? null :
                        cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Hotel> minStars(Integer minStars) {
        return (root, query, cb) ->
                minStars == null ? null :
                        cb.greaterThanOrEqualTo(root.get("starRating"), minStars);
    }

    public static Specification<Hotel> nameContains(String name) {
        return (root, query, cb) ->
                name == null ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    public static Specification<Hotel> starsBetween(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null)
                return cb.between(root.get("starRating"), min, max);
            if (min != null)
                return cb.greaterThanOrEqualTo(root.get("starRating"), min);
            return cb.lessThanOrEqualTo(root.get("starRating"), max);
        };
    }
}