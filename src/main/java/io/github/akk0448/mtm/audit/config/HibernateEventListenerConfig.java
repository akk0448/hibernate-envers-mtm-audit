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
 * Configuration class responsible for registering Hibernate event listeners
 * specifically for Many-to-Many association join table auditing.
 *
 * <p>This class registers the {@link ManyToManyAssociationListener} to
 * capture POST-INSERT events on Hibernate Envers-generated audit tables
 * for Many-to-Many relationships, enabling tracking of association changes.
 * </p>
 *
 * @since 1.0.0
 * @author Aniket Kumar
 * @see ManyToManyAssociationListener
 */
@Configuration
public class HibernateEventListenerConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ManyToManyAssociationListener manyToManyAssociationListener;

    /**
     * Registers the Many-to-Many association listener with Hibernate's
     * event system to monitor Envers audit tables.
     *
     * <p>The listener specifically tracks changes in the join table audit tables
     * (suffixed with _aud) that Hibernate Envers automatically creates for
     * Many-to-Many relationships.
     * </p>
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