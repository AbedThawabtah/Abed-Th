package com.tourism.booking.catalog.repository;

import com.tourism.booking.catalog.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByHotelIdOrderById(Long hotelId);

    @Query("SELECT COALESCE(SUM(r.totalRooms), 0) FROM RoomType r WHERE r.hotel.id = :hotelId")
    long sumTotalRoomsByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT COALESCE(SUM(r.totalRooms), 0) FROM RoomType r")
    long sumTotalRoomsAllHotels();
}
