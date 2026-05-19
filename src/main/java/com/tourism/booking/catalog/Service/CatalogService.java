package com.tourism.booking.catalog.Service;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.catalog.dto.HotelRequest;
import com.tourism.booking.catalog.dto.HotelResponse;
import com.tourism.booking.catalog.dto.RoomTypeRequest;
import com.tourism.booking.catalog.dto.RoomTypeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CatalogService {
    HotelResponse createHotel(HotelRequest request);

    PagedResponse<HotelResponse> browseHotels(String city, Integer minStars, String nameContains, Pageable pageable);

    HotelResponse getHotelById(Long hotelId, boolean includeRoomTypes);

    HotelResponse updateHotel(Long hotelId, HotelRequest request);

    void deleteHotel(Long hotelId);

    HotelResponse uploadHotelImage(Long hotelId, org.springframework.web.multipart.MultipartFile image);

    RoomTypeResponse createRoomType(Long hotelId, RoomTypeRequest request);

    List<RoomTypeResponse> getRoomTypesByHotel(Long hotelId);

    RoomTypeResponse updateRoomType(Long roomTypeId, RoomTypeRequest request);

    void deleteRoomType(Long roomTypeId);

}
