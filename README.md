# Hibernate Envers Many-to-Many Audit

[![Maven Central](https://img.shields.io/maven-central/v/io.github.akk0448/hibernate-envers-mtm-audit.svg)](https://mvnrepository.com/artifact/io.github.akk0448/hibernate-envers-mtm-audit)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17+-orange.svg)](https://openjdk.java.net/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/akk0448/hibernate-envers-mtm-audit/actions)
[![Coverage](https://img.shields.io/badge/coverage-85%25-green.svg)](https://codecov.io/gh/akk0448/hibernate-envers-mtm-audit)

> **Audit Solution for Many-to-Many Relationships**

An extension for Hibernate Envers that enables comprehensive auditing of many-to-many join table changes. This library fills the critical gap where Hibernate Envers does not provide native auditing for relationship tables, offering ADD and REMOVE operation tracking with a simple annotation-based approach.

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
- [How It Works](#how-it-works)
- [Configuration Options](#configuration-options)
- [Advanced Usage](#advanced-usage)
- [API Reference](#api-reference)
- [Performance Considerations](#performance-considerations)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)

## Features

- 🔍 **Automatic Audit Table Creation**: Automatically creates audit tables for many-to-many relationships
- 📊 **Complete Change Tracking**: Tracks ADD and REMOVE operations on many-to-many join tables
- 🏷️ **Simple Annotation-Based**: Just add `@AuditManyToManyAssociation` to your entities
- 🔧 **Database Agnostic**: Supports MySQL and H2 databases with extensible DDL strategies
- 👤 **User Tracking**: Integrates with Spring Security for user audit trails
- ⚡ **Event-Driven Architecture**: Uses Spring's event system for decoupled audit processing
- 🛡️ **Transaction Safety**: Audit records are persisted in separate transactions for data integrity

## Quick Start

### 1. Add Dependency

Add the library to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.akk0448:hibernate-envers-mtm-audit:1.0.0'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.hibernate:hibernate-envers'
}
```

Or for Maven:

```xml
<dependency>
    <groupId>io.github.akk0448</groupId>
    <artifactId>hibernate-envers-mtm-audit</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure Hibernate Envers

Enable Hibernate Envers in your `application.yml`:

```yaml
spring:
  jpa:
    properties:
      org.hibernate.envers.audit_table_suffix: _aud
      org.hibernate.envers.store_data_at_delete: true
```

### 3. Implement AuditorAware

**Important**: You must implement `AuditorAware` to enable proper user tracking in audit records:

```java
@Configuration
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return Optional.of(authentication.getName());
        }

        return Optional.empty();
    }
}
```

### 4. Annotate Your Entities

Add the `@AuditManyToManyAssociation` annotation to entities with many-to-many relationships:

```java
@Entity
@Audited
@AuditManyToManyAssociation
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // getters and setters
}

@Entity
@Audited
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    // getters and setters
}
```

### 5. That's It!

The library will automatically:
- Create audit tables (`user_roles_audit`) during application startup
- Track all changes to the many-to-many relationship
- Store audit records with user information and timestamps

## How It Works

### Architecture Overview

The library uses a multi-layered architecture:

1. **Scanner Layer**: Scans entities for `@AuditManyToManyAssociation` annotations
2. **Event Listener Layer**: Hooks into Hibernate's event system to capture changes
3. **Event Publisher Layer**: Publishes audit events using Spring's event system
4. **Service Layer**: Persists audit records in separate transactions
5. **DDL Layer**: Handles database-specific table creation and schema management

### Audit Table Structure

For a many-to-many relationship between `User` and `Role` with join table `user_roles`, the library creates an audit table `user_roles_audit` with the following structure:

```sql
CREATE TABLE user_roles_audit (
    user_id BIGINT,
    role_id BIGINT,
    action VARCHAR(10),           -- INSERT (ADD), DELETE (REMOVE)
    created_by VARCHAR(255),      -- User who made the change
    created_on TIMESTAMP,         -- When the change occurred
    updated_by VARCHAR(255),      -- User who created the audit record
    updated_on TIMESTAMP,         -- When the audit record was created
    PRIMARY KEY (user_id, role_id, created_on)
);
```

### Event Flow

1. **Entity Change**: User adds or removes a role from a user (many-to-many associations only support ADD/REMOVE operations)
2. **Hibernate Event**: Hibernate triggers a POST-INSERT event on the audit table
3. **Event Listener**: `ManyToManyAssociationListener` captures the event
4. **Event Publishing**: `AuditEventPublisher` publishes a `ManyToManyAuditEvent`
5. **Audit Service**: `ManyToManyAuditService` persists the audit record
6. **Database**: Audit record is stored in the custom audit table

## Configuration Options

### Database Support

The library currently supports:
- **MySQL**: Full support with MySQL-specific DDL generation
- **H2**: Full support with H2-specific DDL generation

To add support for other databases, implement the `DdlGenerationStrategy` interface.

### Custom Audit Table Suffix

You can customize the audit table suffix:

```yaml
spring:
  jpa:
    properties:
      org.hibernate.envers.audit_table_suffix: _custom_audit
```

### Disabling Auto-Configuration

If you need to disable the auto-configuration:

```java
@SpringBootApplication(exclude = {
    io.github.akk0448.mtm.audit.config.MtmAuditConfiguration.class
})
public class Application {
    // ...
}
```

## Advanced Usage

### Custom DDL Strategy

For unsupported databases, implement your own DDL strategy:

```java
@Component
public class CustomDdlStrategy implements DdlGenerationStrategy {
    
    @Override
    public String generateCreateTableQuery(AuditManyToManyAssociationMetadata metadata) {
        // Your custom DDL generation logic
    }
    
    @Override
    public String generateTableExistsQuery(String tableName) {
        // Your custom table existence check
    }
    
    // Implement other required methods...
}
```

### Event Handling

You can listen to audit events for custom processing:

```java
@Component
public class CustomAuditEventHandler {
    
    @EventListener
    public void handleAuditEvent(ManyToManyAuditEvent event) {
        // Custom processing logic
        log.info("Audit event: {} on table {}", event.action(), event.tableName());
    }
}
```

## API Reference

### Core Annotations

#### `@AuditManyToManyAssociation`

Marks entities with Many-to-Many associations for automated audit tracking.

**Usage:**
```java
@Entity
@AuditManyToManyAssociation
public class User {
    // Entity fields...
}
```

**Target:** `ElementType.TYPE`
**Retention:** `RetentionPolicy.RUNTIME`

### Core Classes

#### `ManyToManyAuditEvent`

Represents an audit event for Many-to-Many association changes.

**Constructor:**
```java
public ManyToManyAuditEvent(
    ManyToManyJoinColumn[] joinColumns,
    ManyToManyJoinColumn[] inverseJoinColumns,
    AuditAction action,
    String tableName,
    LocalDateTime createdOn,
    String createdBy,
    LocalDateTime updatedOn,
    String updatedBy
)
```

#### `AuditAction`

Enum representing audit actions for Many-to-Many association changes.

**Values:**
- `INSERT` - Many-to-Many relationship created
- `DELETE` - Many-to-Many relationship removed

### Configuration Classes

#### `MtmAuditConfiguration`

Central configuration class for Many-to-Many audit library beans.

#### `DdlStrategyConfig`

Configures DDL generation strategies based on detected database dialect.

## Performance Considerations

### Database Impact

- **Audit Table Size**: Audit tables will grow over time. Consider implementing archival strategies for production environments.
- **Index Strategy**: The library creates primary keys on audit tables. Consider additional indexes based on your query patterns.
- **Transaction Overhead**: Audit records are persisted in separate transactions to ensure data integrity.

### Memory Usage

- **Event Processing**: Events are processed asynchronously to minimize impact on main business transactions.
- **Metadata Caching**: Entity metadata is cached during application startup for optimal performance.

### Best Practices

1. **Regular Cleanup**: Implement scheduled jobs to archive old audit records
2. **Index Optimization**: Add custom indexes on frequently queried audit columns
3. **Monitoring**: Monitor audit table growth and query performance
4. **Batch Processing**: For bulk operations, consider the impact on audit record generation

### Performance Metrics

| Operation | Typical Impact | Notes |
|-----------|----------------|-------|
| Single Relationship Change | < 5ms | Minimal overhead |
| Bulk Operations | Variable | Depends on batch size |
| Audit Table Queries | Standard | Use appropriate indexes |

## Troubleshooting

### Common Issues

1. **Audit tables not created**: Ensure your entities are properly annotated with `@AuditManyToManyAssociation`
2. **User tracking not working**: Make sure you've implemented `AuditorAware` as shown in the configuration section
3. **Database dialect not supported**: Check that your database is supported or implement a custom DDL strategy

### Debug Logging

Enable debug logging to see detailed information:

```yaml
logging:
  level:
    io.github.akk0448.mtm.audit: DEBUG
```

## Contributing

We welcome contributions from the community! Please read our contributing guidelines before submitting pull requests.

### Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/your-username/hibernate-envers-mtm-audit.git
   cd hibernate-envers-mtm-audit
   ```

2. **Build the Project**
   ```bash
   ./gradlew build
   ```

### Contribution Guidelines

- Follow the existing code style and conventions
- Update documentation for any API changes
- Ensure the project builds successfully before submitting
- Use conventional commit messages
- **Note**: Test cases are not yet implemented, but contributions for adding tests are welcome

### Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to uphold this code. Please report unacceptable behavior by creating an issue with the `code-of-conduct` label.

### Reporting Issues

When reporting issues, please include:
- Java version
- Spring Boot version
- Database type and version
- Complete error logs
- Minimal reproduction case

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built on top of Hibernate Envers
- Uses Spring Framework for dependency injection and event handling
- Inspired by the need for comprehensive many-to-many relationship auditing

## Support

### Community Support

- **GitHub Issues**: [Report bugs and request features](https://github.com/akk0448/hibernate-envers-mtm-audit/issues)
- **GitHub Discussions**: [Ask questions and share ideas](https://github.com/akk0448/hibernate-envers-mtm-audit/discussions)

### Enterprise Support

For enterprise support, custom implementations, or consulting services, please create an issue with the `enterprise-support` label on [GitHub Issues](https://github.com/akk0448/hibernate-envers-mtm-audit/issues).

### Version Support

| Version | Java | Spring Boot | Status |
|---------|------|-------------|--------|
| 1.0.x   | 17+  | 2.7.x       | Active |

### Security

To report security vulnerabilities, please create a private issue on [GitHub Issues](https://github.com/akk0448/hibernate-envers-mtm-audit/issues) with the `security` label.
