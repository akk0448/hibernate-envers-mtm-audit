package io.github.akk0448.mtm.audit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks entities with Many-to-Many associations for automated audit tracking.
 * Applied to the owning side of Many-to-Many relationships.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditManyToManyAssociation {
}