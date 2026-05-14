
package com.tourism.booking.catalog.controller;

import com.tourism.booking.exception.ResourceNotFoundException;
import com.tourism.booking.catalog.Service.HotelImageService;
import com.tourism.booking.catalog.repository.HotelRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@Tag(name = "Catalog Images", description = "Public hotel images")
@RestController
@RequestMapping("/api/catalog/hotels")
@RequiredArgsConstructor
public class HotelImageController {

    private final HotelRepository hotelRepository;
    private final HotelImageService hotelImageService;

    @Operation(summary = "Get hotel image (public)")
    @GetMapping("/{hotelId}/image")
    public ResponseEntity<Resource> getHotelImage(@PathVariable Long hotelId) throws IOException {
        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        if (!StringUtils.hasText(hotel.getImagePath())) {
            throw new ResourceNotFoundException("Hotel image not found");
        }

        Resource resource = hotelImageService.loadAsResource(hotel.getImagePath());
        if (resource == null) {
            throw new ResourceNotFoundException("Hotel image not found");
        }

        String contentType = Files.probeContentType(resource.getFile().toPath());
        MediaType mediaType = StringUtils.hasText(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
    @Operation(summary = "Upload hotel image")
    @PostMapping(value = "/{hotelId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadHotelImage(
            @PathVariable Long hotelId,
            @RequestParam("image") MultipartFile image) {

        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        String imagePath = hotelImageService.storeHotelImage(hotelId, image);

        // حفظ المسار في الداتابيس
        hotel.setImagePath(imagePath);
        hotelRepository.save(hotel);

        return ResponseEntity.ok(imagePath);
    }
}

