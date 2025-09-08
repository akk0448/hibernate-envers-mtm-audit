package io.github.akk0448.mtm.audit.ddl;

import io.github.akk0448.mtm.audit.metadata.AuditJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditManyToManyAssociationMetadata;
import io.github.akk0448.mtm.audit.scan.ManyToManyAuditScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class AuditTableCreator {

    private final ManyToManyAuditScanner auditScanner;
    private final DdlGenerationStrategy ddlStrategy;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void createAuditTables() {
        Map<String, AuditManyToManyAssociationMetadata> metadataMap = auditScanner.getAuditAssociationMetadata();

        for (AuditManyToManyAssociationMetadata metadata : metadataMap.values()) {
            String existsQuery = ddlStrategy.generateTableExistsQuery(metadata.tableName());

            boolean tableExists = Boolean.TRUE.equals(
                    jdbcTemplate.query(existsQuery, ps -> ps.setString(1, metadata.tableName()),
                            rs -> rs.next() && rs.getInt(1) > 0
                    )
            );

            if (!tableExists) {
                String ddl = ddlStrategy.generateCreateTableQuery(metadata);
                try {
                    log.info("\n{}", ddl);
                    jdbcTemplate.execute(ddl);
                } catch (Exception e) {
                    log.error("Failed to create audit table [{}]: {}", metadata.tableName(), e.getMessage(), e);
                }
            } else {
                try {
                    Set<String> existingColumns = getExistingColumns(metadata.tableName());

                    List<AuditJoinColumn> allColumns = new ArrayList<>();
                    allColumns.addAll(Arrays.asList(metadata.joinColumns()));
                    allColumns.addAll(Arrays.asList(metadata.inverseJoinColumns()));

                    for (AuditJoinColumn col : allColumns) {
                        if (!existingColumns.contains(col.columnName().toLowerCase())) {
                            String alterQuery = ddlStrategy.generateAlterTableQuery(metadata.tableName(), col);
                            log.info("\n{}", alterQuery);
                            jdbcTemplate.execute(alterQuery);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to update audit table [{}]: {}", metadata.tableName(), e.getMessage(), e);
                }
            }
        }
    }

    private Set<String> getExistingColumns(String tableName) {
        return new HashSet<>(jdbcTemplate.query(
                ddlStrategy.fetchColumnNameQuery(tableName),
                ps -> ps.setString(1, tableName),
                (rs, rowNum) -> rs.getString(1).toLowerCase()
        ));
    }
}