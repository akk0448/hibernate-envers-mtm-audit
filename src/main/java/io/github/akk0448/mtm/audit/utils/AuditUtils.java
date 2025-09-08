package io.github.akk0448.mtm.audit.utils;

import io.github.akk0448.mtm.audit.enums.AuditAction;
import io.github.akk0448.mtm.audit.events.ManyToManyJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditJoinColumn;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

import java.util.Map;

public class AuditUtils {

    public static Object getValueIgnoreCase(Map<String, Object> map, String logicalKey) {
        return map.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(logicalKey))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static ManyToManyJoinColumn[] getColumnData(Map<String, Object> map, AuditJoinColumn[] columns) {
        int n = columns.length;
        ManyToManyJoinColumn[] joinColumns = new ManyToManyJoinColumn[n];

        for (int i = 0; i < n; i++) {
            AuditJoinColumn column = columns[i];
            joinColumns[i] = new ManyToManyJoinColumn(column.columnName(), getValueIgnoreCase(map, column.auditColumnName()));
        }

        return joinColumns;
    }

    public static AuditAction resolveAuditAction(RevisionType revisionType) {
        return switch (revisionType) {
            case ADD -> AuditAction.INSERT;
            case MOD -> AuditAction.UPDATE;
            case DEL -> AuditAction.DELETE;
        };
    }

    public static long resolveAuditTimestamp(DefaultRevisionEntity defaultRevisionEntity) {
        return defaultRevisionEntity.getTimestamp();
    }
}