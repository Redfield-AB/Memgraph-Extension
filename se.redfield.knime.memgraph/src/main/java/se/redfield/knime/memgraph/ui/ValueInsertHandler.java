/**
 *
 */
package se.redfield.knime.memgraph.ui;

/**
 * @author redfield.ai
 *
 */
@FunctionalInterface
public interface ValueInsertHandler<T> {
    void insert(T value);
}
