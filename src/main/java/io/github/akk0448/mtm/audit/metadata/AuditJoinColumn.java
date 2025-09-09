package io.github.akk0448.mtm.audit.metadata;

/**
 * Column metadata for Many-to-Many association audit mapping.
 * Defines the mapping between database columns and application field names.
 *
 * @param columnName the database column name
 * @param auditColumnName the application field name for audit events
 * @param columnClassType the Java type of the column values
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
public record AuditJoinColumn(
        String columnName,
        String auditColumnName,
        Class<?> columnClassType
) {
}