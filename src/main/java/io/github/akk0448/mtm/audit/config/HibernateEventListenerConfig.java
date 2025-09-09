package io.github.akk0448.mtm.audit.config;

import io.github.akk0448.mtm.audit.listeners.ManyToManyAssociationListener;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

/**
 * Registers Hibernate event listeners for Many-to-Many association auditing.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
@Configuration
public class HibernateEventListenerConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ManyToManyAssociationListener manyToManyAssociationListener;

    /**
     * Registers Many-to-Many association listener with Hibernate event system.
     */
    @PostConstruct
    public void registerListeners() {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory
                .getServiceRegistry()
                .getService(EventListenerRegistry.class);

        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(manyToManyAssociationListener);
    }
}