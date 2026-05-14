package com.tourism.booking.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String actorUsername;
    private String actorRole;
    private String details;
    private Instant createdAt;
}
