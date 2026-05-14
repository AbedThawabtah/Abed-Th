package com.tourism.booking.booking;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.booking.dto.BookingRequest;
import com.tourism.booking.booking.dto.BookingResponse;
import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.booking.service.BookingServiceInterface;
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
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Booking", description = "Booking management")
@RestController
@RequestMapping("/api/bookings")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class BookingController {

    private final BookingServiceInterface bookingService;


    // ---------------------------------------------------
    // Create Booking
    // ---------------------------------------------------

    @Operation(summary = "Create a booking")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            UriComponentsBuilder uriBuilder) {

        BookingResponse response = bookingService.createBooking(request);

        URI location = uriBuilder
                .path("/api/bookings/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }


    // ---------------------------------------------------
    // Confirm Booking
    // ---------------------------------------------------

    @Operation(summary = "Confirm booking after payment")
    @PostMapping("/{bookingId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'COUSTOMER')")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.confirmBooking(bookingId)
        );
    }


    // ---------------------------------------------------
    // Cancel Booking
    // ---------------------------------------------------

    @Operation(summary = "Cancel booking")
    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or (hasRole('CUSTOMER') and @authorizationService.ownsBooking(#bookingId))")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(bookingId)
        );
    }


    // ---------------------------------------------------
    // Guest Booking History
    // ---------------------------------------------------

    @Operation(summary = "Get guest booking history by email (admin/manager or self)")
    @GetMapping("/guest-history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or (hasRole('CUSTOMER') and @authorizationService.isSelfEmail(#guestEmail))")
    public ResponseEntity<List<BookingResponse>> getGuestHistory(
            @RequestParam String guestEmail) {

        return ResponseEntity.ok(
                bookingService.getGuestHistory(guestEmail)
        );
    }

    @Operation(summary = "Current customer's bookings")
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> myBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }


    // ---------------------------------------------------
    // Upcoming Bookings
    // ---------------------------------------------------

    @Operation(summary = "Get upcoming bookings for a hotel")
    @GetMapping("/manager-upcoming")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<BookingResponse>> getManagerUpcoming(
            @RequestParam Long hotelId) {

        return ResponseEntity.ok(
                bookingService.getManagerUpcoming(hotelId)
        );
    }


    // ---------------------------------------------------
    // Browse Bookings
    // ---------------------------------------------------

    @Operation(summary = "Browse bookings with filters and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PagedResponse<BookingResponse>> browseBookings(

            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,

            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                bookingService.browseBookings(
                        hotelId,
                        roomTypeId,
                        email,
                        status,
                        fromDate,
                        toDate,
                        pageable
                )
        );
    }
}
