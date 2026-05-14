package com.tourism.booking.notification.service;

import com.tourism.booking.PagedResponse;
import com.tourism.booking.booking.entity.Booking;
import com.tourism.booking.notification.dto.NotificationMapper;
import com.tourism.booking.notification.dto.NotificationResponse;
import com.tourism.booking.notification.entity.Notification;
import com.tourism.booking.notification.repository.NotificationRepository;
import com.tourism.booking.notification.repository.NotificationSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Persists each outbound notification to the database first, then sends the same content by SMTP
 * via {@link EmailService}. Mail failures are logged only and do not roll back the DB record.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    @Transactional
    public void sendBookingConfirmation(Booking booking) {

        String guest = booking.getGuestName() != null ? booking.getGuestName() : "Guest";
        String subject = "Booking confirmed — reference #" + booking.getId();

        String body = """
                Hello %s,

                Your booking has been confirmed.

                Booking reference: #%d
                Check-in:  %s
                Check-out: %s
                Total:     %s

                We look forward to welcoming you.

                — Hotel Booking Team
                """.stripIndent().formatted(
                guest,
                booking.getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getTotalAmount()
        );

        persistNotification(
                booking.getId(),
                booking.getGuestEmail(),
                Notification.NotificationType.BOOKING_CONFIRMATION,
                subject,
                body
        );

        emailService.sendPlainText(booking.getGuestEmail(), subject, body);
    }

    @Transactional
    public void sendBookingCancellation(Booking booking) {

        String guest = booking.getGuestName() != null ? booking.getGuestName() : "Guest";
        String subject = "Booking cancelled — reference #" + booking.getId();

        String body = """
                Hello %s,

                Your booking #%d has been cancelled.

                Original stay: %s to %s

                If you did not request this change, please contact us.

                — Hotel Booking Team
                """.stripIndent().formatted(
                guest,
                booking.getId(),
                booking.getCheckIn(),
                booking.getCheckOut()
        );

        persistNotification(
                booking.getId(),
                booking.getGuestEmail(),
                Notification.NotificationType.BOOKING_CANCELLATION,
                subject,
                body
        );

        emailService.sendPlainText(booking.getGuestEmail(), subject, body);
    }

    private void persistNotification(
            Long bookingId,
            String recipient,
            Notification.NotificationType type,
            String subject,
            String body) {

        Notification n = Notification.builder()
                .bookingId(bookingId)
                .type(type)
                .recipientEmail(recipient)
                .subject(subject)
                .body(body)
                .build();

        notificationRepository.save(n);
    }

    // ---------------------------------------------------
    // Browse Notifications (Manager Dashboard)
    // ---------------------------------------------------

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> browseNotifications(

            Long bookingId,
            String email,
            Notification.NotificationType type,
            Instant fromDate,
            Instant toDate,
            Pageable pageable) {

        Specification<Notification> spec = Specification
                .where(NotificationSpecifications.bookingIdEquals(bookingId))
                .and(NotificationSpecifications.emailEquals(email))
                .and(NotificationSpecifications.typeEquals(type))
                .and(NotificationSpecifications.sentBetween(fromDate, toDate));

        Page<Notification> page = notificationRepository.findAll(spec, pageable);

        List<NotificationResponse> content = page.getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();

        return PagedResponse.from(page, content);
    }
}
