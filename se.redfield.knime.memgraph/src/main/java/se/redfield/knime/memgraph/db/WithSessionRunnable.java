/**
 *
 */
package se.redfield.knime.memgraph.db;

import org.neo4j.driver.Session;

/**
 * @author redfield.ai
 *
 */
@FunctionalInterface
public interface WithSessionRunnable<R> {
    R run(Session session);
}
