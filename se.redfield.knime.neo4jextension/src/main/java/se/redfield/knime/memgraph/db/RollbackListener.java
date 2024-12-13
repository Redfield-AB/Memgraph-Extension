/**
 *
 */
package se.redfield.knime.memgraph.db;

/**
 * @author redfield.ai
 *
 */
@FunctionalInterface
public interface RollbackListener {
    /**
     */
    void isRolledBack();
}
