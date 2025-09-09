package io.github.akk0448.mtm.audit.utils;

import io.github.akk0448.mtm.audit.enums.AuditAction;
import io.github.akk0448.mtm.audit.events.ManyToManyJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditJoinColumn;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

import java.util.Map;

/**
 * Utility methods for Many-to-Many audit data processing and conversion.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
public class AuditUtils {

    /**
     * Retrieves a value from a map using case-insensitive key matching.
     *
     * @param map the map containing the data
     * @param logicalKey the key to search for (case-insensitive)
     * @return the value associated with the key, or null if not found
     */
    public static Object getValueIgnoreCase(Map<String, Object> map, String logicalKey) {
        return map.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(logicalKey))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Converts AuditJoinColumn metadata to ManyToManyJoinColumn data.
     *
     * @param map the data map containing column values
     * @param columns the metadata columns to extract
     * @return array of ManyToManyJoinColumn with extracted values
     */
    public static ManyToManyJoinColumn[] getColumnData(Map<String, Object> map, AuditJoinColumn[] columns) {
        int n = columns.length;
        ManyToManyJoinColumn[] joinColumns = new ManyToManyJoinColumn[n];

        for (int i = 0; i < n; i++) {
            AuditJoinColumn column = columns[i];
            joinColumns[i] = new ManyToManyJoinColumn(column.columnName(), getValueIgnoreCase(map, column.auditColumnName()));
        }

        return joinColumns;
    }

    /**
     * Maps Hibernate Envers RevisionType to AuditAction.
     *
     * @param revisionType the Hibernate Envers revision type
     * @return the corresponding AuditAction
     */
    public static AuditAction resolveAuditAction(RevisionType revisionType) {
        return switch (revisionType) {
            case ADD -> AuditAction.INSERT;
            case MOD -> AuditAction.UPDATE;
            case DEL -> AuditAction.DELETE;
        };
    }

    /**
     * Extracts timestamp from Hibernate Envers revision entity.
     *
     * @param defaultRevisionEntity the revision entity
     * @return timestamp of the audited operation
     */
    public static long resolveAuditTimestamp(DefaultRevisionEntity defaultRevisionEntity) {
        return defaultRevisionEntity.getTimestamp();
    }
}