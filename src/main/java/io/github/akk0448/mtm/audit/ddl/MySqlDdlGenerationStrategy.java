package io.github.akk0448.mtm.audit.ddl;

import io.github.akk0448.mtm.audit.metadata.AuditJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditManyToManyAssociationMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MySqlDdlGenerationStrategy implements DdlGenerationStrategy {

    private static final String CREATE_TABLE_TEMPLATE = """
            CREATE TABLE IF NOT EXISTS `%s` (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                %s,
                action VARCHAR(20) NOT NULL,
                created_by VARCHAR(255),
                created_on DATETIME(6),
                updated_by VARCHAR(255),
                updated_on DATETIME(6)
            )
            """;

    @Override
    public String generateCreateTableQuery(AuditManyToManyAssociationMetadata metadata) {
        List<String> columnDefs = new ArrayList<>();

        for (AuditJoinColumn col : metadata.joinColumns()) {
            String sqlType = mapJavaTypeToSqlType(col.columnClassType());
            columnDefs.add(String.format("`%s` %s NOT NULL", col.columnName(), sqlType));
        }

        for (AuditJoinColumn col : metadata.inverseJoinColumns()) {
            String sqlType = mapJavaTypeToSqlType(col.columnClassType());
            columnDefs.add(String.format("`%s` %s NOT NULL", col.columnName(), sqlType));
        }

        String columnsPart = String.join(",\n    ", columnDefs);

        return String.format(
                CREATE_TABLE_TEMPLATE,
                metadata.tableName(),
                columnsPart
        );
    }

    @Override
    public String generateTableExistsQuery(String tableName) {
        return """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                AND table_name = ?
                """;
    }

    @Override
    public String fetchColumnNameQuery(String tableName) {
        return """
                SELECT COLUMN_NAME
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                AND table_name = ?
                """;
    }

    @Override
    public String generateAlterTableQuery(String tableName, AuditJoinColumn col) {
        return String.format(
                "ALTER TABLE %s ADD COLUMN %s %s NOT NULL",
                tableName, col.columnName(), mapJavaTypeToSqlType(col.columnClassType())
        );
    }


    private String mapJavaTypeToSqlType(Class<?> javaType) {
        if (javaType == Integer.class || javaType == int.class) {
            return "INT";
        } else if (javaType == Long.class || javaType == long.class) {
            return "BIGINT";
        } else if (javaType == String.class) {
            return "VARCHAR(255)";
        } else if (javaType == java.time.LocalDateTime.class || javaType == java.util.Date.class) {
            return "TIMESTAMP";
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return "BOOLEAN";
        }

        log.warn("Unmapped Java type [{}], defaulting to VARCHAR(255)", javaType.getName());
        return "VARCHAR(255)";
    }
}