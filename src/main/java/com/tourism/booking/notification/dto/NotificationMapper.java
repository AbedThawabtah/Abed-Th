package com.tourism.booking.notification.dto;

import com.tourism.booking.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .bookingId(n.getBookingId())
                .type(n.getType())
                .recipientEmail(n.getRecipientEmail())
                .subject(n.getSubject())
                .body(n.getBody())
                .sentAt(n.getSentAt())
                .build();
    }
}
