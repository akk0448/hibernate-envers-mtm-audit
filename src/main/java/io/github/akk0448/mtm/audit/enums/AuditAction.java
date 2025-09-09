package io.github.akk0448.mtm.audit.enums;

/**
 * Audit actions for Many-to-Many association changes.
 *
 * @author Aniket Kumar
 * @since 1.0.0
 */
public enum AuditAction {

    /**
     * Many-to-Many relationship created.
     */
    INSERT,

    /**
     * Many-to-Many relationship updated.
     */
    UPDATE,

    /**
     * Many-to-Many relationship removed.
     */
    DELETE
}