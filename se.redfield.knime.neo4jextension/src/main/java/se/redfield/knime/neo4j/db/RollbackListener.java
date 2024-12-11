/**
 *
 */
package se.redfield.knime.neo4j.db;

/**
 * @author Vyacheslav Soldatov
 *
 */
@FunctionalInterface
public interface RollbackListener {
    /**
     */
    void isRolledBack();
}
