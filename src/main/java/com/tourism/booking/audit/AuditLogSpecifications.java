package com.tourism.booking.audit;

import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> actionEquals(AuditAction action) {
        return (root, q, cb) -> action == null ? cb.conjunction() : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> entityTypeEquals(String entityType) {
        return (root, q, cb) ->
                entityType == null || entityType.isBlank() ? cb.conjunction() : cb.equal(root.get("entityType"), entityType);
    }

    public static Specification<AuditLog> actorUsernameEquals(String username) {
        return (root, q, cb) ->
                username == null || username.isBlank() ? cb.conjunction() : cb.equal(root.get("actorUsername"), username);
    }
}
