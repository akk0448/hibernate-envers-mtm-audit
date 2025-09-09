package io.github.akk0448.mtm.audit.metadata;

/**
 * Metadata container for Many-to-Many association audit configuration.
 * Defines the structure and mapping requirements for audit table generation
 * and event processing.
 *
 * @param tableName the target audit table name
 * @param joinColumns columns from the owning entity side
 * @param inverseJoinColumns columns from the inverse entity side
 * @author Aniket Kumar
 * @since 1.0.0
 */
public record AuditManyToManyAssociationMetadata(
        String tableName,
        AuditJoinColumn[] joinColumns,
        AuditJoinColumn[] inverseJoinColumns
) {
}