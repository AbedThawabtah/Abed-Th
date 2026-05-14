package com.tourism.booking.catalog.Service;


import com.tourism.booking.PagedResponse;
import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.audit.AuditAction;
import com.tourism.booking.audit.AuditLogService;
import com.tourism.booking.catalog.dto.*;
import com.tourism.booking.catalog.entity.Hotel;
import com.tourism.booking.catalog.entity.RoomType;
import com.tourism.booking.catalog.repository.HotelRepository;
import com.tourism.booking.catalog.repository.HotelSpecifications;
import com.tourism.booking.catalog.repository.RoomTypeRepository;
import com.tourism.booking.review.ReviewMapper;
import com.tourism.booking.review.ReviewRepository;
import com.tourism.booking.review.ReviewResponse;
import com.tourism.booking.security.AppUser;
import com.tourism.booking.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final CatalogMapper catalogMapper;
    private final AuditLogService auditLogService;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final HotelImageService hotelImageService;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public HotelResponse createHotel(HotelRequest request) {
        Hotel hotel = catalogMapper.toEntity(request);
        hotel = hotelRepository.save(hotel);
        auditLogService.log(AuditAction.HOTEL_CREATED, "HOTEL", String.valueOf(hotel.getId()), hotel.getName());
        return enrichHotel(catalogMapper.toHotelResponse(hotel, false), false, hotel);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HotelResponse> browseHotels(
            String city,
            Integer minStars,
            String nameContains,
            Pageable pageable) {

        Specification<Hotel> spec = Specification
                .where(HotelSpecifications.cityEquals(city))
                .and(HotelSpecifications.minStars(minStars))
                .and(HotelSpecifications.nameContains(nameContains));

        Page<Hotel> hotelPage = hotelRepository.findAll(spec, pageable);
        List<HotelResponse> content = hotelPage.getContent().stream()
                .map(hotel -> enrichHotel(catalogMapper.toHotelResponse(hotel, false), false, hotel))
                .collect(Collectors.toList());

        return PagedResponse.from(hotelPage, content);
    }

    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long hotelId, boolean includeRoomTypes) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        HotelResponse base = catalogMapper.toHotelResponse(hotel, includeRoomTypes);
        return enrichHotel(base, includeRoomTypes, hotel);
    }

    @Transactional
    public HotelResponse updateHotel(Long hotelId, HotelRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser current = customUserDetailsService.loadAppUserByUsername(username);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        if (hotel.getOwners() == null || hotel.getOwners().stream().noneMatch(owner -> owner.getId().equals(current.getId()))) {
            throw new ResourceNotFoundException("Hotel", hotelId); // Hide existence if not owner
        }

        catalogMapper.updateEntity(hotel, request);
        hotel = hotelRepository.save(hotel);
        auditLogService.log(AuditAction.HOTEL_UPDATED, "HOTEL", String.valueOf(hotelId), hotel.getName());
        return enrichHotel(catalogMapper.toHotelResponse(hotel, false), false, hotel);
    }

    @Transactional
    public HotelResponse uploadHotelImage(Long hotelId, MultipartFile image) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        String relativePath = hotelImageService.storeHotelImage(hotelId, image);
        hotel.setImagePath(relativePath);
        hotel = hotelRepository.save(hotel);
        auditLogService.log(AuditAction.HOTEL_UPDATED, "HOTEL", String.valueOf(hotelId), "imageUploaded=true");

        HotelResponse base = catalogMapper.toHotelResponse(hotel, false);
        return enrichHotel(base, false, hotel);
    }

    @Transactional
    public void deleteHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
        auditLogService.log(AuditAction.HOTEL_DELETED, "HOTEL", String.valueOf(hotelId), null);
        hotelRepository.deleteById(hotelId);
    }

    @Transactional
    public RoomTypeResponse createRoomType(Long hotelId, RoomTypeRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        RoomType roomType = catalogMapper.toEntity(request, hotel);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        AppUser current = customUserDetailsService.loadAppUserByUsername(username);
        if (hotel.getOwners() == null || hotel.getOwners().stream().noneMatch(owner -> owner.getId().equals(current.getId()))) {
            throw new ResourceNotFoundException("Hotel", hotelId); // Hide existence if not owner
        }
        roomType = roomTypeRepository.save(roomType);
        auditLogService.log(AuditAction.ROOM_TYPE_CREATED, "ROOM_TYPE", String.valueOf(roomType.getId()), roomType.getName());
        return catalogMapper.toRoomTypeResponse(roomType);
    }

    @Transactional(readOnly = true)
    public List<RoomTypeResponse> getRoomTypesByHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
        return roomTypeRepository.findByHotelIdOrderById(hotelId).stream()
                .map(catalogMapper::toRoomTypeResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomTypeResponse updateRoomType(Long roomTypeId, RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", roomTypeId));
        catalogMapper.updateEntity(roomType, request);
        roomType = roomTypeRepository.save(roomType);
        auditLogService.log(AuditAction.ROOM_TYPE_UPDATED, "ROOM_TYPE", String.valueOf(roomTypeId), roomType.getName());
        return catalogMapper.toRoomTypeResponse(roomType);
    }

    @Transactional
    public void deleteRoomType(Long roomTypeId) {
        if (!roomTypeRepository.existsById(roomTypeId)) {
            throw new ResourceNotFoundException("RoomType", roomTypeId);
        }
        auditLogService.log(AuditAction.ROOM_TYPE_DELETED, "ROOM_TYPE", String.valueOf(roomTypeId), null);
        roomTypeRepository.deleteById(roomTypeId);
    }

    private HotelResponse enrichHotel(HotelResponse base, boolean includeRecent, Hotel hotel) {
        Long id = base.getId();
        Double avg = reviewRepository.averageRatingByHotelId(id).orElse(null);
        long count = reviewRepository.countByHotelId(id);
        var builder = base.toBuilder()
                .averageRating(avg)
                .reviewsCount(count);
        if (hotel != null && hotel.getImagePath() != null && !hotel.getImagePath().isBlank()) {
            builder.imageUrl("/api/catalog/hotels/" + id + "/image");
        } else {
            builder.imageUrl(null);
        }
        if (includeRecent) {
            List<ReviewResponse> recent = reviewRepository
                    .findByHotelIdOrderByCreatedAtDesc(id).stream()
                    .limit(5)
                    .map(reviewMapper::toResponse)
                    .toList();
            builder.recentReviews(recent);
        }
        return builder.build();
    }
}
