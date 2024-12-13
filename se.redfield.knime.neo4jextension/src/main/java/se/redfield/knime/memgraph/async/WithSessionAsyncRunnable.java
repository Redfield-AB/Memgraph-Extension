/**
 *
 */
package se.redfield.knime.memgraph.async;

import org.neo4j.driver.Session;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface WithSessionAsyncRunnable<V, R> {
    R run(Session session, V arg) throws Throwable;
}
