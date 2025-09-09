package io.github.akk0448.mtm.audit.ddl;

import io.github.akk0448.mtm.audit.metadata.AuditJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditManyToManyAssociationMetadata;

/**
 * Strategy for database-specific DDL generation for Many-to-Many audit tables.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
public interface DdlGenerationStrategy {

    /**
     * Generates CREATE TABLE query for Many-to-Many audit table.
     */
    String generateCreateTableQuery(AuditManyToManyAssociationMetadata metadata);

    /**
     * Generates query to check if audit table exists.
     */
    String generateTableExistsQuery(String tableName);

    /**
     * Generates query to fetch column names from audit table.
     */
    String fetchColumnNameQuery(String tableName);

    /**
     * Generates ALTER TABLE query to add column to audit table.
     */
    String generateAlterTableQuery(String tableName, AuditJoinColumn col);
}