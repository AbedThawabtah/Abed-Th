package com.tourism.booking.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "room_types",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"hotel_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer maxGuests;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerNight;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRooms = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
}

