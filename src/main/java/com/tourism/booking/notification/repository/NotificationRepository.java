package com.tourism.booking.notification.repository;

import com.tourism.booking.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    List<Notification> findByBookingIdOrderBySentAtDesc(Long bookingId);

//    List<Notification> findByRecipientEmailOrderBySentAtDesc(String recipientEmail);
}
