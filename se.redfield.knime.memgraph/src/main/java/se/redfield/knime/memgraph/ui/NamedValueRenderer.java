/**
 *
 */
package se.redfield.knime.memgraph.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import se.redfield.knime.memgraph.connector.Named;

/**
 * @author redfield.ai
 *
 */
public class NamedValueRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 5223006886411453577L;

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object origin, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final Object value = origin instanceof Named
                ? ((Named) origin).getName() : origin;
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
