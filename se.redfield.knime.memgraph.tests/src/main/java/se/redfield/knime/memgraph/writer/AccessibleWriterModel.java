/**
 *
 */
package se.redfield.knime.memgraph.writer;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessibleWriterModel extends WriterModel {
    /**
     * Default constructor.
     */
    public AccessibleWriterModel(final NodeCreationConfiguration creationConfig) {
        super(creationConfig);
    }

    @Override
    public PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return super.configure(inSpecs);
    }
    @Override
    public PortObject[] execute(final PortObject[] input, final ExecutionContext exec) throws Exception {
        return super.execute(input, exec);
    }
    @Override
    public void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
    }
    public String getWarningMsg() {
        return getWarningMessage();
    }
}
