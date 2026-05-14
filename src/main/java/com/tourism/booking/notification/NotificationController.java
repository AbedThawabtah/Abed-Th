package com.tourism.booking.notification;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.notification.dto.NotificationResponse;
import com.tourism.booking.notification.entity.Notification;
import com.tourism.booking.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Notifications", description = "Manager notifications dashboard")
@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Browse notifications with filters and pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PagedResponse<NotificationResponse>> browseNotifications(

            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Notification.NotificationType type,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,

            @PageableDefault(
                    size = 20,
                    sort = "sentAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                notificationService.browseNotifications(
                        bookingId,
                        email,
                        type,
                        fromDate,
                        toDate,
                        pageable
                )
        );
    }
}