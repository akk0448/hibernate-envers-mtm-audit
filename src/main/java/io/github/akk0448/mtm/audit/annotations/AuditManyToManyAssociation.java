package io.github.akk0448.mtm.audit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark entity classes that contain Many-to-Many associations
 * which should be audited for changes.
 *
 * <p>
 * When applied to an entity class, the audit library will automatically:
 * <ul>
 *   <li>Create audit tables for Many-to-Many join tables</li>
 *   <li>Track INSERT and DELETE operations on associations</li>
 *   <li>Maintain historical records of relationship changes</li>
 *   <li>Capture audit metadata (timestamps, user information)</li>
 * </ul>
 * </p>
 *
 * <p>
 * This annotation should be placed on the owning side of the Many-to-Many
 * relationship (the entity that defines the {@code @JoinTable} annotation).
 * </p>
 *
 * @since 1.0.0
 * @author Aniket Kumar
 * @see javax.persistence.ManyToMany
 * @see javax.persistence.JoinTable
 * @see org.hibernate.envers.Audited
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditManyToManyAssociation {
}