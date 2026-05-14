package com.tourism.booking.catalog.dto;



import com.tourism.booking.catalog.entity.Hotel;
import com.tourism.booking.catalog.entity.RoomType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CatalogMapper {

    public Hotel toEntity(HotelRequest request) {
        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setCity(request.getCity());
        hotel.setAddress(request.getAddress());
        hotel.setStarRating(request.getStarRating());
        hotel.setDescription(request.getDescription());
        return hotel;
    }

    public void updateEntity(Hotel hotel, HotelRequest request) {
        hotel.setName(request.getName());
        hotel.setCity(request.getCity());
        hotel.setAddress(request.getAddress());
        hotel.setStarRating(request.getStarRating());
        hotel.setDescription(request.getDescription());
    }

    public HotelResponse toHotelResponse(Hotel hotel, boolean includeRoomTypes) {
        List<RoomTypeResponse> roomTypes = includeRoomTypes && hotel.getRoomTypes() != null
                ? hotel.getRoomTypes().stream().map(this::toRoomTypeResponse).collect(Collectors.toList())
                : Collections.emptyList();
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .address(hotel.getAddress())
                .starRating(hotel.getStarRating())
                .description(hotel.getDescription())
                .imageUrl(null)
                .roomTypes(roomTypes)
                .build();
    }

    public RoomType toEntity(RoomTypeRequest request, Hotel hotel) {
        RoomType rt = new RoomType();
        rt.setName(request.getName());
        rt.setDescription(request.getDescription());
        rt.setMaxGuests(request.getMaxGuests());
        rt.setBasePricePerNight(request.getBasePricePerNight());
        rt.setTotalRooms(request.getTotalRooms());
        rt.setHotel(hotel);
        // بما إنو عدني علاقة فمش مشكلة من أي جهة بضيف لأن الجهتان سوف يعلمان أو يصلهما التعديل أو الإضافة
        return rt;
    }

    public void updateEntity(RoomType roomType, RoomTypeRequest request) {
        roomType.setName(request.getName());
        roomType.setDescription(request.getDescription());
        roomType.setMaxGuests(request.getMaxGuests());
        roomType.setBasePricePerNight(request.getBasePricePerNight());
        roomType.setTotalRooms(request.getTotalRooms());
    }

    public RoomTypeResponse toRoomTypeResponse(RoomType roomType) {
        return RoomTypeResponse.builder()
                .id(roomType.getId())
                .hotelId(roomType.getHotel().getId())  // منطقيا بما إنو انت مستأجر غرفة أو حاجز وحدة أكيد بتكون عارف في أي أوتبل فبالتالي بلزمكش تعرف معلومات عن هذا الأوتيل ولو بدك مهو برجعلك الأي دي الخاص بالأوتيل هنا .. تستطيع أن تأخذه وتبحث من خلاله
                .name(roomType.getName())
                .description(roomType.getDescription())
                .maxGuests(roomType.getMaxGuests())
                .basePricePerNight(roomType.getBasePricePerNight())
                .totalRooms(roomType.getTotalRooms())
                .build();
    }
}
