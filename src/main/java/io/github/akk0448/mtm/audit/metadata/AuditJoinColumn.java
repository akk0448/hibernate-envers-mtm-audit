package io.github.akk0448.mtm.audit.metadata;

public record AuditJoinColumn(
        String columnName,
        String auditColumnName,
        Class<?> columnClassType
) {
}