package io.github.akk0448.mtm.audit.scan;

import io.github.akk0448.mtm.audit.annotations.AuditManyToManyAssociation;
import io.github.akk0448.mtm.audit.metadata.AuditJoinColumn;
import io.github.akk0448.mtm.audit.metadata.AuditManyToManyAssociationMetadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Scans entities for Many-to-Many associations requiring audit tracking.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
@Slf4j
public class ManyToManyAuditScanner {

    private static final String CUSTOM_AUDIT_SUFFIX = "_audit";

    @Getter
    private Map<String, AuditManyToManyAssociationMetadata> auditAssociationMetadata = new HashMap<>();

    @Value("${spring.jpa.properties.org.hibernate.envers.audit_table_suffix:_aud}")
    private String audSuffix;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    /**
     * Scans JPA entities for Many-to-Many associations with audit annotation.
     */
    @PostConstruct
    public void scanEntities() {
        Map<String, AuditManyToManyAssociationMetadata> tempMap = new HashMap<>();

        Metamodel metamodel = entityManagerFactory.getMetamodel();

        for (EntityType<?> entity : metamodel.getEntities()) {
            Class<?> clazz = entity.getJavaType();

            if (!clazz.isAnnotationPresent(AuditManyToManyAssociation.class)) continue;

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(ManyToMany.class) && field.isAnnotationPresent(JoinTable.class)) {
                    try {
                        JoinTable joinTable = field.getAnnotation(JoinTable.class);
                        JoinColumn[] joinColAnns = joinTable.joinColumns();
                        JoinColumn[] inverseColAnns = joinTable.inverseJoinColumns();

                        String tableName = joinTable.name();
                        Class<?> inverseEntityClass = getGenericType(field);

                        AuditManyToManyAssociationMetadata metadata = new AuditManyToManyAssociationMetadata(tableName + CUSTOM_AUDIT_SUFFIX, createAuditJoinColumn(joinColAnns, clazz), createAuditInverseJoinColumn(inverseColAnns, inverseEntityClass, field));

                        tempMap.put(tableName + audSuffix, metadata);
                        log.debug("Registered metadata for [{}]: {}", clazz.getSimpleName(), metadata);

                    } catch (Exception e) {
                        log.warn("Failed to process field [{}] in [{}]: {}", field.getName(), clazz.getSimpleName(), e.getMessage(), e);
                    }
                }
            }
        }

        this.auditAssociationMetadata = Collections.unmodifiableMap(tempMap);
    }

    private AuditJoinColumn[] createAuditJoinColumn(JoinColumn[] joinColAnns, Class<?> clazz) {
        int n = joinColAnns.length;
        AuditJoinColumn[] auditJoinColumns = new AuditJoinColumn[n];

        for (int i = 0; i < n; i++) {
            JoinColumn joinColumn = joinColAnns[i];

            String columnName = joinColumn.name();

            String referencedColumnName = StringUtils.isBlank(joinColumn.referencedColumnName())
                    ? getPrimaryKeyColumnName(clazz)
                    : joinColumn.referencedColumnName();

            Field joinField = findFieldByReferencedColumnName(clazz, referencedColumnName);
            if (joinField != null) {
                String auditColumnName = String.format("%s_%s", clazz.getSimpleName(), joinField.getName());
                Class<?> columnClassType = joinField.getType();
                auditJoinColumns[i] = new AuditJoinColumn(columnName, auditColumnName, columnClassType);
            } else {
                throw new RuntimeException("Could not resolve field for referencedColumnName=" + referencedColumnName + " in class=" + clazz.getName());
            }
        }

        return auditJoinColumns;
    }

    private AuditJoinColumn[] createAuditInverseJoinColumn(JoinColumn[] inverseColAnns, Class<?> inverseEntityClass, Field field) {
        int n = inverseColAnns.length;
        AuditJoinColumn[] auditInverseJoinColumns = new AuditJoinColumn[n];

        for (int i = 0; i < n; i++) {
            JoinColumn joinColumn = inverseColAnns[i];

            String columnName = joinColumn.name();

            String referencedColumnName = StringUtils.isBlank(joinColumn.referencedColumnName())
                    ? getPrimaryKeyColumnName(inverseEntityClass)
                    : joinColumn.referencedColumnName();

            Field joinField = findFieldByReferencedColumnName(inverseEntityClass, referencedColumnName);
            if (joinField != null) {
                String auditColumnName = String.format("%s_%s", field.getName(), joinField.getName());
                Class<?> columnClassType = joinField.getType();
                auditInverseJoinColumns[i] = new AuditJoinColumn(columnName, auditColumnName, columnClassType);
            } else {
                throw new RuntimeException("Could not resolve field for referencedColumnName=" + referencedColumnName + " in class=" + inverseEntityClass.getName());
            }
        }

        return auditInverseJoinColumns;
    }

    private String getPrimaryKeyColumnName(Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                Column colAnn = f.getAnnotation(Column.class);
                return (colAnn != null && StringUtils.isNotBlank(colAnn.name())) ? colAnn.name() : f.getName();
            }
            if (f.isAnnotationPresent(EmbeddedId.class)) {
                return f.getName();
            }
        }
        throw new RuntimeException("No @Id field found in " + clazz.getName());
    }

    private Field findFieldByReferencedColumnName(Class<?> clazz, String referencedColumnName) {
        // 1. Direct fields
        for (Field f : clazz.getDeclaredFields()) {
            Field match = matchField(f, referencedColumnName);
            if (match != null) return match;
        }

        // 2. EmbeddedId
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(EmbeddedId.class)) {
                for (Field idField : f.getType().getDeclaredFields()) {
                    Field match = matchField(idField, referencedColumnName);
                    if (match != null) return match;
                }
            }
        }

        // 3. IdClass
        IdClass idClassAnn = clazz.getAnnotation(IdClass.class);
        if (idClassAnn != null) {
            for (Field idField : idClassAnn.value().getDeclaredFields()) {
                Field match = matchField(idField, referencedColumnName);
                if (match != null) return match;
            }
        }

        return null;
    }

    private Field matchField(Field f, String referencedColumnName) {
        Column colAnn = f.getAnnotation(Column.class);
        if (colAnn != null && referencedColumnName.equalsIgnoreCase(colAnn.name())) {
            f.trySetAccessible();
            return f;
        }
        if (referencedColumnName.equalsIgnoreCase(f.getName())) {
            f.trySetAccessible();
            return f;
        }
        return null;
    }

    private Class<?> getGenericType(Field field) {
        if (!(field.getGenericType() instanceof ParameterizedType parameterizedType)) {
            log.warn("Unable to resolve generic type for field: {}", field.getName());
            return Object.class;
        }
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }
}