package com.tourism.booking.audit;

import com.tourism.booking.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(AuditAction action, String entityType, String entityId, String details) {
        String username = null;
        String role = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
            role = auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse(null);
        }
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .actorUsername(username)
                .actorRole(role)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> browse(
            AuditAction action,
            String entityType,
            String actorUsername,
            Pageable pageable) {

        Specification<AuditLog> spec = Specification
                .where(AuditLogSpecifications.actionEquals(action))
                .and(AuditLogSpecifications.entityTypeEquals(entityType))
                .and(AuditLogSpecifications.actorUsernameEquals(actorUsername));

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        List<AuditLogResponse> content = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.from(page, content);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .actorUsername(log.getActorUsername())
                .actorRole(log.getActorRole())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
