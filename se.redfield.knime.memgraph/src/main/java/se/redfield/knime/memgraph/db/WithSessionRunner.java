/**
 *
 */
package se.redfield.knime.memgraph.db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import se.redfield.knime.memgraph.async.AsyncRunner;
import se.redfield.knime.memgraph.async.WithSessionAsyncRunnable;

/**
 * @author redfield.ai
 *
 * @param <V>
 * @param <R>
 */
public final class WithSessionRunner<V, R> implements AsyncRunner<V, R> {
    private final WithSessionAsyncRunnable<V, R> r;
    private final Driver driver;
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    /**
     * @param r runnable.
     * @param driver Neo4j driver.
     */
    public WithSessionRunner(final WithSessionAsyncRunnable<V, R> r, final Driver driver) {
        this.r = r;
        this.driver = driver;
    }

    @Override
    public R run(final V arg) throws Throwable {
        return r.run(sessions.get(Thread.currentThread().getId()), arg);
    }
    @Override
    public void workerStarted() {
        sessions.put(Thread.currentThread().getId(), driver.session());
    }
    @Override
    public void workerStopped() {
        final Session s = sessions.remove(Thread.currentThread().getId());
        if (s != null) { //if is started
            s.close();
        }
    }
}