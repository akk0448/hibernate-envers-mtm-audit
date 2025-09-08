package io.github.akk0448.mtm.audit.config;

import io.github.akk0448.mtm.audit.ddl.DdlGenerationStrategy;
import io.github.akk0448.mtm.audit.ddl.H2DdlGenerationStrategy;
import io.github.akk0448.mtm.audit.ddl.MySqlDdlGenerationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * Configuration class for DDL generation strategy beans.
 * Automatically detects the database dialect and provides the appropriate
 * DDL generation strategy for audit table creation.
 *
 * <p>This configuration supports multiple database dialects and returns
 * the appropriate strategy implementation based on the detected Hibernate dialect.
 * </p>
 *
 * @since 1.0.0
 * @author Aniket Kumar
 * @see DdlGenerationStrategy
 * @see MySqlDdlGenerationStrategy
 */
@Configuration
@Slf4j
public class DdlStrategyConfig {

    /**
     * Creates a DDL generation strategy bean based on the detected database dialect.
     * The strategy provides database-specific SQL for creating and managing audit tables.
     *
     * @param emf the EntityManagerFactory used to detect the database dialect
     * @return the appropriate DDLGenerationStrategy for the current database
     * @throws UnsupportedOperationException if the database dialect is not supported
     */
    @Bean
    public DdlGenerationStrategy ddlGenerationStrategy(EntityManagerFactory emf) {
        SessionFactoryImplementor sessionFactory = emf.unwrap(SessionFactoryImplementor.class);
        Dialect dialect = sessionFactory.getJdbcServices().getDialect();
        String dialectName = dialect.getClass().getSimpleName().toLowerCase();

        Map<String, DdlGenerationStrategy> strategies = Map.of(
                "mysql", new MySqlDdlGenerationStrategy(),
                "h2", new H2DdlGenerationStrategy()
        );

        return strategies.entrySet().stream()
                .filter(entry -> dialectName.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Unsupported DB dialect: " + dialectName));
    }
}