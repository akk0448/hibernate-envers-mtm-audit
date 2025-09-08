package io.github.akk0448.mtm.audit.listeners;

import io.github.akk0448.mtm.audit.enums.AuditAction;
import io.github.akk0448.mtm.audit.events.AuditEventPublisher;
import io.github.akk0448.mtm.audit.events.ManyToManyJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditManyToManyAssociationMetadata;
import io.github.akk0448.mtm.audit.scan.ManyToManyAuditScanner;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

import java.util.Map;

import static io.github.akk0448.mtm.audit.events.ManyToManyAuditEvent.createAuditEvent;
import static io.github.akk0448.mtm.audit.utils.AuditUtils.*;

@Slf4j
public class ManyToManyAssociationListener implements PostInsertEventListener {

    private final ManyToManyAuditScanner auditScanner;
    private final AuditEventPublisher auditEventPublisher;

    public ManyToManyAssociationListener(ManyToManyAuditScanner auditScanner, AuditEventPublisher auditEventPublisher) {
        this.auditScanner = auditScanner;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        Map<String, AuditManyToManyAssociationMetadata> auditAssociationMetadata =
                auditScanner.getAuditAssociationMetadata();

        if (!auditAssociationMetadata.containsKey(event.getPersister().getEntityName())) return;

        String entityName = event.getPersister().getEntityName();
        AuditManyToManyAssociationMetadata metadata = auditAssociationMetadata.get(entityName);

        Object rawEntity = event.getEntity();
        if (!(rawEntity instanceof Map<?, ?> rawMap)) {
            log.warn("Unexpected entity type: {}", rawEntity.getClass());
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) rawMap;
        @SuppressWarnings("unchecked")
        Map<String, Object> record = (Map<String, Object>) entity.get("originalId");

        ManyToManyJoinColumn[] joinColumns = getColumnData(record, metadata.joinColumns());
        ManyToManyJoinColumn[] inverseJoinColumns = getColumnData(record, metadata.inverseJoinColumns());

        if (joinColumns.length == 0 || inverseJoinColumns.length == 0) {
            log.warn("Missing join/inverse column in record [{}] for table [{}]", record, entityName);
            return;
        }

        AuditAction auditAction = resolveAuditAction((RevisionType) entity.get("REV_TYPE"));
        long auditTimestamp = resolveAuditTimestamp((DefaultRevisionEntity) record.get("REV_ID"));

        auditEventPublisher.publish(createAuditEvent(
                joinColumns,
                inverseJoinColumns,
                auditAction,
                metadata.tableName(),
                auditTimestamp
        ));
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}