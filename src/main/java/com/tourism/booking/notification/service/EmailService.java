package com.tourism.booking.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Sends plain-text messages with {@link SimpleMailMessage}. Failures are logged and not rethrown so
 * notification persistence in the same transaction is not rolled back when SMTP fails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.from:}")
    private String configuredFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * Sends a plain-text email. Swallows mail failures after logging so callers (e.g. transactional
     * notification flows) are not rolled back.
     */
    public void sendPlainText(String to, String subject, String text) {
        if (!StringUtils.hasText(to)) {
            log.warn("Skipping email send: recipient address is empty | subject: {}", subject);
            return;
        }

        String from = StringUtils.hasText(configuredFrom) ? configuredFrom : mailUsername;
        if (!StringUtils.hasText(from)) {
            log.warn(
                    "Skipping email send: no From address (set MAIL_FROM or MAIL_USER) | to: {} | subject: {}",
                    to,
                    subject
            );
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent | to: {} | subject: {}", to, subject);
        } catch (MailException ex) {
            log.error("Failed to send email | to: {} | subject: {}", to, subject, ex);
        } catch (Exception ex) {
            log.error("Unexpected error while sending email | to: {} | subject: {}", to, subject, ex);
        }
    }
}
