/**
 *
 */
package se.redfield.knime.memgraph.db;

import java.util.List;

import org.knime.core.data.DataRow;
import org.neo4j.driver.Record;

/**
 * @author redfield.ai
 *
 */
public class ScriptExecutionResult {
    public final DataRow row;
    public final List<Record> recors;
    public final Throwable error;
    public ScriptExecutionResult(final DataRow row, final List<Record> recors, final Throwable error) {
        super();
        this.row = row;
        this.recors = recors;
        this.error = error;
    }
}
