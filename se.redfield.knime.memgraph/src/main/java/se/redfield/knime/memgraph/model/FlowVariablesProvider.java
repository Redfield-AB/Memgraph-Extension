/**
 *
 */
package se.redfield.knime.memgraph.model;

import java.util.Map;

import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType;

/**
 * @author redfield.ai
 *
 */
@FunctionalInterface
public interface FlowVariablesProvider {
    Map<String, FlowVariable> getAvailableFlowVariables(final VariableType<?>[] types);
}
