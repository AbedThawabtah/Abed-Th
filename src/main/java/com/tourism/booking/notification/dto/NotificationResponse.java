package com.tourism.booking.notification.dto;

import com.tourism.booking.notification.entity.Notification;
import lombok.Builder;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long bookingId;
    private Notification.NotificationType type;
    private String recipientEmail;
    private String subject;
    private String body;
    private Instant sentAt;
}
