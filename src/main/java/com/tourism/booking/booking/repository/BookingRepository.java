package com.tourism.booking.booking.repository;


import com.tourism.booking.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    /**
     * Find CONFIRMED bookings that overlap with the given date range for the same room type.
     * Used to prevent double booking.
     */
    @Query("SELECT b FROM Booking b WHERE b.roomTypeId = :roomTypeId AND b.status = 'CONFIRMED' " +
            "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<Booking> findOverlappingConfirmedBookings(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);


    @Query("""
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.roomTypeId = :roomTypeId
            AND b.status = 'PENDING'
            AND b.checkIn < :checkOut
            AND b.checkOut > :checkIn
            """)
    boolean existsOverlappingPendingBooking(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    List<Booking> findByGuestEmailOrderByCreatedAtDesc(String guestEmail); // كل الحجزات الخاصة بالبريد الإلكتروني للضيف مرتبة حسب تاريخ الإنشاء

    List<Booking> findByCustomerUserIdOrderByCreatedAtDesc(Long customerUserId);

    @Query("SELECT b FROM Booking b WHERE b.hotelId = :hotelId AND b.status = 'CONFIRMED' AND b.checkIn >= :fromDate ORDER BY b.checkIn")
    List<Booking> findUpcomingByHotel(@Param("hotelId") Long hotelId, @Param("fromDate") java.time.LocalDate fromDate); // كل الحجزات القادمة لفندق معين تبدأ من تاريخ معين مرتبة حسب تاريخ الوصول


    @Query("""
            SELECT b FROM Booking b
            WHERE b.roomTypeId = :roomTypeId
            AND b.status = 'CONFIRMED'
            AND b.checkOut > :fromDate
            AND b.checkIn < :toDate
            """)
    List<Booking> findBookingsForCalendar(
            @Param("roomTypeId") Long roomTypeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);


    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = 'PENDING'
            AND b.createdAt < :expirationTime
            """)
    List<Booking> findExpiredPendingBookings(Instant expirationTime);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.roomTypeId = :roomTypeId
            AND b.status IN ('PENDING', 'CONFIRMED')
            AND b.checkIn < :checkOut
            AND b.checkOut > :checkIn
            """)
    long countOverlappingInventoryBookings(
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.roomTypeId = :roomTypeId
            AND b.status IN ('PENDING', 'CONFIRMED')
            AND b.checkIn <= :day
            AND b.checkOut > :day
            """)
    long countBookingsCoveringNight(
            @Param("roomTypeId") Long roomTypeId,
            @Param("day") LocalDate day);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.hotelId = :hotelId
            AND b.status = 'CONFIRMED'
            AND (
                (b.customerUserId IS NOT NULL AND b.customerUserId = :userId)
                OR LOWER(b.guestEmail) = LOWER(:email)
            )
            """)
    boolean existsConfirmedForCustomerAndHotel(
            @Param("userId") Long userId,
            @Param("email") String email,
            @Param("hotelId") Long hotelId);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.createdAt >= :start AND b.createdAt < :end
            AND (:hotelId IS NULL OR b.hotelId = :hotelId)
            """)
    long countCreatedInPeriod(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("hotelId") Long hotelId);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.createdAt >= :start AND b.createdAt < :end
            AND (:hotelId IS NULL OR b.hotelId = :hotelId)
            AND b.status = :status
            """)
    long countByStatusCreatedInPeriod(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("hotelId") Long hotelId,
            @Param("status") Booking.BookingStatus status);

    @Query("""
            SELECT b.roomTypeId FROM Booking b
            WHERE b.createdAt >= :start AND b.createdAt < :end
            AND (:hotelId IS NULL OR b.hotelId = :hotelId)
            GROUP BY b.roomTypeId
            ORDER BY COUNT(b) DESC
            """)
    List<Long> findMostBookedRoomTypeIds(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("hotelId") Long hotelId,
            Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = 'CONFIRMED'
            AND b.checkIn <= :toDate AND b.checkOut > :fromDate
            AND (:hotelId IS NULL OR b.hotelId = :hotelId)
            """)
    List<Booking> findConfirmedOverlappingStay(
            @Param("hotelId") Long hotelId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

}
