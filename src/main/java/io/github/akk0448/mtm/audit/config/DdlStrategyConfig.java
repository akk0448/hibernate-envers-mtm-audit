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
 * Configures DDL generation strategies based on detected database dialect.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class DdlStrategyConfig {

    /**
     * Provides a DDL generation strategy appropriate for the current database dialect.
     *
     * @param emf the EntityManagerFactory
     * @return configured DDL generation strategy
     * @throws UnsupportedOperationException for unsupported dialects
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