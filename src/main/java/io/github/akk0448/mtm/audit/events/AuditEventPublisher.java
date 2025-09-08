package io.github.akk0448.mtm.audit.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(ManyToManyAuditEvent event) {
        publisher.publishEvent(event);
    }
}