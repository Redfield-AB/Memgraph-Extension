/**
 *
 */
package se.redfield.knime.memgraph.ui;


import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * @author redfield.ai
 *
 */
public class BatchNamedValueRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 5223006886411453577L;

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object origin, final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {
        final Object value = origin instanceof BatchPattern
                ? ((BatchPattern) origin).getName() : origin;
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
