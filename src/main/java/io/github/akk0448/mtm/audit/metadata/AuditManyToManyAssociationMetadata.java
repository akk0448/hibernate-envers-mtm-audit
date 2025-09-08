package io.github.akk0448.mtm.audit.metadata;

public record AuditManyToManyAssociationMetadata(
        String tableName,
        AuditJoinColumn[] joinColumns,
        AuditJoinColumn[] inverseJoinColumns
) {
}