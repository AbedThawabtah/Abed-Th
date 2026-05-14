package com.tourism.booking.catalog.controller;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.catalog.Service.CatalogService;
import com.tourism.booking.catalog.dto.HotelRequest;
import com.tourism.booking.catalog.dto.HotelResponse;
import com.tourism.booking.catalog.dto.RoomTypeRequest;
import com.tourism.booking.catalog.dto.RoomTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Catalog", description = "Hotels and room types")
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService; // Dependency Inversion Principle
    // إنو خليت الكونترولر يعتمد على الانترفيس مش على الامبليمينتيشن عشان يكون سهل نبدل الانترفيس بدون ما نغير الكونترولر
    // إنو بقدر أضيف أي كلاس عامل إمبلمنت لهذا الإنترفيس بسهولة بدون ما أغير الكونترولر
    // وكذلك هنا لو غير إسم الكلاس الذي عامل إمبلمنتيشن مش راح يأثر على الكونترولر طالما الإسم نفسه في الإنترفيس أي لا أحتاج أن أعدل الكونترولر وهنا أكون قد طبقت المبدأ الثاني في السولد وهو الأوبن كلوز

    @Operation(summary = "Create hotel")
    @PostMapping("/hotels")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody HotelRequest request, UriComponentsBuilder uriBuilder) {
        HotelResponse hotelResponse = catalogService.createHotel(request);
        URI location = uriBuilder.path("/api/catalog/hotels/{id}").buildAndExpand(hotelResponse.getId()).toUri();
        return ResponseEntity.created(location).body(hotelResponse);
    }

    @Operation(summary = "Browse hotels with filters and pagination")
    @GetMapping("/hotels")
    public ResponseEntity<PagedResponse<HotelResponse>> browseHotels(
            @RequestParam(required = false) String city,  // يمكنك إضافة ديفولت فاليو
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) String nameContains,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(catalogService.browseHotels(city, minStars, nameContains, pageable));
    }

    @Operation(summary = "Get hotel by ID with room types")
    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<HotelResponse> getHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(catalogService.getHotelById(hotelId, true));
    }

    @Operation(summary = "Update hotel")
    @PutMapping("/hotels/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long hotelId, @Valid @RequestBody HotelRequest request) {
        return ResponseEntity.ok(catalogService.updateHotel(hotelId, request));
    }

    @Operation(summary = "Delete hotel")
    @DeleteMapping("/hotels/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long hotelId) {
        catalogService.deleteHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create room type for hotel")
    @PostMapping("/hotels/{hotelId}/room-types")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<RoomTypeResponse> createRoomType(
            @PathVariable Long hotelId,
            @Valid @RequestBody RoomTypeRequest request,
            UriComponentsBuilder uriBuilder) {
        RoomTypeResponse roomTypeResponse = catalogService.createRoomType(hotelId, request);
        URI location = uriBuilder.path("/api/catalog/room-types/{id}").buildAndExpand(roomTypeResponse.getId()).toUri();
        return ResponseEntity.created(location).body(roomTypeResponse);
    }

    @Operation(summary = "List room types for hotel")
    @GetMapping("/hotels/{hotelId}/room-types")
    public ResponseEntity<List<RoomTypeResponse>> getRoomTypes(@PathVariable Long hotelId) {
        return ResponseEntity.ok(catalogService.getRoomTypesByHotel(hotelId));
    }

    // يمكن إضافة أي أوتيل تريد أن تعدل وتحذف عليه للتأكد من الأوتيل أولا ثم الغرفة

    @Operation(summary = "Update room type")
    @PutMapping("/room-types/{roomTypeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<RoomTypeResponse> updateRoomType(
            @PathVariable Long roomTypeId,
            @Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.ok(catalogService.updateRoomType(roomTypeId, request));
    }

    @Operation(summary = "Delete room type")
    @DeleteMapping("/room-types/{roomTypeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long roomTypeId) {
        catalogService.deleteRoomType(roomTypeId);
        return ResponseEntity.noContent().build();
    }
}