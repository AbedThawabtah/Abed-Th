package com.tourism.booking.availability_pricing.controller;

import com.tourism.booking.availability_pricing.dto.AvailabilityPricingRequest;
import com.tourism.booking.availability_pricing.dto.AvailabilityPricingResponse;
import com.tourism.booking.availability_pricing.service.AvailabilityPricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Availability & Pricing", description = "Check room availability and get price quote")
@RestController
@RequestMapping("/api/availability-pricing")
@RequiredArgsConstructor
public class AvailabilityPricingController {

    private final AvailabilityPricingService availabilityPricingService;

    @Operation(summary = "Check availability and get quote using query params")
    @GetMapping("/quote")
    public ResponseEntity<AvailabilityPricingResponse> quote(
            @RequestParam Long hotelId,
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam Integer guests,
            @RequestParam(required = false) String promoCode) {

        AvailabilityPricingRequest request = new AvailabilityPricingRequest();
        request.setHotelId(hotelId);
        request.setRoomTypeId(roomTypeId);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        request.setGuests(guests);
        request.setPromoCode(promoCode);

        AvailabilityPricingResponse response =
                availabilityPricingService.getQuote(request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check availability and get quote using request body")
    @PostMapping("/quote")
    public ResponseEntity<AvailabilityPricingResponse> quotePost(
            @Valid @RequestBody AvailabilityPricingRequest request) {

        AvailabilityPricingResponse response =
                availabilityPricingService.getQuote(request);

        return ResponseEntity.ok(response);
    }
}