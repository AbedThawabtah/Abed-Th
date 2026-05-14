package com.tourism.booking.promo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promo_codes", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validTo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minBookingAmount; // شرط حيث لا يعمل الكود إلا إذا السعر ≥ هذا الرقم

    private Integer maxUses;  // عدد مرات الاستخدام الكلي

    @Column(nullable = false)
    @Builder.Default
    private int currentUses = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
