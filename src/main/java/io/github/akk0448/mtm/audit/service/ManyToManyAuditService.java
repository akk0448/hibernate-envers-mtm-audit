package io.github.akk0448.mtm.audit.service;

import io.github.akk0448.mtm.audit.events.ManyToManyAuditEvent;
import io.github.akk0448.mtm.audit.events.ManyToManyJoinColumn;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for persisting audit records for Many-to-Many association changes.
 * Operates within isolated transactions to ensure audit data integrity regardless of
 * the outcome of main business transactions.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
public class ManyToManyAuditService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Persists an audit event to the corresponding audit table.
     * Executes in a new transaction context to maintain data consistency.
     *
     * @param event the audit event to persist
     * @throws RuntimeException if persistence fails
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistAuditEntity(ManyToManyAuditEvent event) {
        try {
            HashMap<String, Object> columnValueMap = new LinkedHashMap<>();

            for (ManyToManyJoinColumn col : event.joinColumns()) {
                columnValueMap.put(col.columnName(), col.columnValue());
            }

            for (ManyToManyJoinColumn col : event.inverseJoinColumns()) {
                columnValueMap.put(col.columnName(), col.columnValue());
            }

            columnValueMap.put("action", event.action().name());
            columnValueMap.put("created_by", event.createdBy());
            columnValueMap.put("created_on", event.createdOn());
            columnValueMap.put("updated_by", event.updatedBy());
            columnValueMap.put("updated_on", event.updatedOn());

            String columnsPart = String.join(", ", columnValueMap.keySet());
            String placeholders = columnValueMap.keySet().stream()
                    .map(k -> "?")
                    .collect(Collectors.joining(", "));

            String insertQuery = String.format(
                    "INSERT INTO %s (%s) VALUES (%s)",
                    event.tableName(),
                    columnsPart,
                    placeholders
            );

            Query query = entityManager.createNativeQuery(insertQuery);
            int index = 1;
            for (Object value : columnValueMap.values()) {
                query.setParameter(index++, value);
            }

            query.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error persisting audit entity", e);
        }
    }
}