package io.github.akk0448.mtm.audit.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Publishes Many-to-Many audit events to Spring application context.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher publisher;

    /**
     * Publishes Many-to-Many audit event.
     */
    public void publish(ManyToManyAuditEvent event) {
        publisher.publishEvent(event);
    }
}