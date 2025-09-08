package io.github.akk0448.mtm.audit.ddl;

import io.github.akk0448.mtm.audit.metadata.AuditJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditManyToManyAssociationMetadata;

/**
 * Strategy interface for generating database-specific DDL statements
 * for Many-to-Many association audit tables.
 *
 * <p>Implementations of this interface provide database-specific SQL
 * for creating, checking existence, and altering audit tables used
 * to track changes in Many-to-Many relationships.
 * </p>
 *
 * @since 1.0.0
 * @author Aniket Kumar
 * @see AuditManyToManyAssociationMetadata
 * @see AuditJoinColumn
 */
public interface DdlGenerationStrategy {

    /**
     * Generates a CREATE TABLE query for a Many-to-Many audit table.
     * The query should include all necessary columns for tracking
     * association changes, including foreign keys, audit metadata,
     * and action tracking.
     *
     * @param metadata the metadata describing the Many-to-Many association
     * @return the complete CREATE TABLE SQL statement
     */
    String generateCreateTableQuery(AuditManyToManyAssociationMetadata metadata);

    /**
     * Generates a query to check if an audit table already exists
     * in the database.
     *
     * @param tableName the name of the audit table to check
     * @return a SQL query that returns a count of tables with the given name
     */
    String generateTableExistsQuery(String tableName);

    /**
     * Generates a query to fetch all column names from an existing
     * audit table. Used for detecting schema changes and performing
     * table alterations when needed.
     *
     * @param tableName the name of the audit table
     * @return a SQL query that returns column names from the table
     */
    String fetchColumnNameQuery(String tableName);

    /**
     * Generates an ALTER TABLE query to add a new column to an
     * existing audit table. Used when new columns need to be
     * added to existing audit tables.
     *
     * @param tableName the name of the audit table to alter
     * @param col the column metadata to add to the table
     * @return the complete ALTER TABLE ADD COLUMN SQL statement
     */
    String generateAlterTableQuery(String tableName, AuditJoinColumn col);
}