package io.github.akk0448.mtm.audit.events;

/**
 * Column name and value for Many-to-Many audit event.
 *
 * @param columnName database column name
 * @param columnValue column value inserted or deleted
 * @author Aniket Kumar
 * @since 1.0.0
 */
public record ManyToManyJoinColumn(String columnName, Object columnValue) {
}