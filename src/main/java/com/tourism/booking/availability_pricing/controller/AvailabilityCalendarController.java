package com.tourism.booking.availability_pricing.controller;

import com.tourism.booking.availability_pricing.dto.AvailabilityCalendarRequest;
import com.tourism.booking.availability_pricing.dto.AvailabilityCalendarResponse;
import com.tourism.booking.availability_pricing.service.AvailabilityCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Availability Calendar", description = "Calendar view of room availability")
@RestController
@RequestMapping("/api/availability/calendar")
@RequiredArgsConstructor
public class AvailabilityCalendarController {

    private final AvailabilityCalendarService availabilityCalendarService;

    @Operation(summary = "Get availability calendar for a room type")
    @PostMapping
    public ResponseEntity<AvailabilityCalendarResponse> getCalendar(
            @Valid @RequestBody AvailabilityCalendarRequest request) {

        AvailabilityCalendarResponse response =
                availabilityCalendarService.getCalendar(request);

        return ResponseEntity.ok(response);
    }
}