package io.github.akk0448.mtm.audit.listeners;

import io.github.akk0448.mtm.audit.events.ManyToManyAuditEvent;
import io.github.akk0448.mtm.audit.service.ManyToManyAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
public class ManyToManyAuditEventListener {

    private final ManyToManyAuditService auditService;

    public ManyToManyAuditEventListener(ManyToManyAuditService auditService) {
        this.auditService = auditService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuditEvent(ManyToManyAuditEvent event) {
        try {
            auditService.persistAuditEntity(event);
        } catch (Exception e) {
            log.error("Failed to persist audit table [{}]: {}",
                    event.tableName(),
                    e.getMessage(), e);
        }
    }
}