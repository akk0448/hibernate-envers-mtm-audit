package io.github.akk0448.mtm.audit.config;

import io.github.akk0448.mtm.audit.ddl.AuditTableCreator;
import io.github.akk0448.mtm.audit.ddl.DdlGenerationStrategy;
import io.github.akk0448.mtm.audit.events.AuditEventPublisher;
import io.github.akk0448.mtm.audit.listeners.ManyToManyAssociationListener;
import io.github.akk0448.mtm.audit.listeners.ManyToManyAuditEventListener;
import io.github.akk0448.mtm.audit.scan.ManyToManyAuditScanner;
import io.github.akk0448.mtm.audit.service.ManyToManyAuditService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Central configuration class for Many-to-Many audit library beans.
 * Defines all necessary components for auditing Many-to-Many association changes.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
@Configuration
public class MtmAuditConfiguration {

    @Bean
    public ManyToManyAuditScanner manyToManyAuditScanner() {
        return new ManyToManyAuditScanner();
    }

    @Bean
    public ManyToManyAssociationListener manyToManyAssociationListener(
            ManyToManyAuditScanner auditScanner,
            AuditEventPublisher auditEventPublisher) {
        return new ManyToManyAssociationListener(auditScanner, auditEventPublisher);
    }

    @Bean
    public AuditEventPublisher auditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new AuditEventPublisher(applicationEventPublisher);
    }

    @Bean
    public AuditTableCreator auditTableCreator(
            ManyToManyAuditScanner manyToManyAuditScanner,
            DdlGenerationStrategy ddlGenerationStrategy,
            JdbcTemplate jdbcTemplate) {
        return new AuditTableCreator(manyToManyAuditScanner, ddlGenerationStrategy, jdbcTemplate);
    }

    @Bean
    public ManyToManyAuditService manyToManyAuditService() {
        return new ManyToManyAuditService();
    }

    @Bean
    public ManyToManyAuditEventListener manyToManyAuditEventListener(ManyToManyAuditService service) {
        return new ManyToManyAuditEventListener(service);
    }
}