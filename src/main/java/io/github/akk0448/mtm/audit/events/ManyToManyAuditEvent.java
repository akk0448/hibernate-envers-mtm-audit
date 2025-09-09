package io.github.akk0448.mtm.audit.events;

import io.github.akk0448.mtm.audit.enums.AuditAction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Audit event for Many-to-Many association changes.
 *
 * @param joinColumns owning side columns and values
 * @param inverseJoinColumns inverse side columns and values
 * @param action audit action performed
 * @param tableName target audit table name
 * @param createdOn when association change occurred
 * @param createdBy user who performed the change
 * @param updatedOn when audit record was created
 * @param updatedBy user who created audit record
 * @author Aniket Kumar
 * @since 1.0.0
 */
public record ManyToManyAuditEvent(ManyToManyJoinColumn[] joinColumns, ManyToManyJoinColumn[] inverseJoinColumns,
                                   AuditAction action, String tableName, LocalDateTime createdOn,
                                   String createdBy, LocalDateTime updatedOn, String updatedBy) {

    /**
     * Creates ManyToManyAuditEvent with automatic timestamp and user extraction.
     */
    public static ManyToManyAuditEvent createAuditEvent(ManyToManyJoinColumn[] joinColumns,
                                                        ManyToManyJoinColumn[] inverseJoinColumns,
                                                        AuditAction action, String tableName, long auditTimestamp) {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(auditTimestamp), ZoneId.of("UTC"));
        String currentUser = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("anonymousUser");

        return new ManyToManyAuditEvent(
                joinColumns, inverseJoinColumns,
                action, tableName,
                now, currentUser, now, currentUser
        );
    }
}