/**
 *
 */
package se.redfield.knime.memgraph.ui;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;

/**
 * @author redfield.ai
 *
 */
public class OnClickInserter<T> extends MouseAdapter {
    private final JList<T> list;
    private final ValueInsertHandler<T> handler;

    /**
     * @param list listened list of values.
     * @param h event handler.
     */
    public OnClickInserter(final JList<T> list, final ValueInsertHandler<T> h) {
        super();
        this.list = list;
        this.handler = h;
    }
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
            final int index = list.getSelectedIndex();
            if (index > -1 && index == indexOfUnderlyingElement(list, e.getX(), e.getY())) {
                final T value = list.getModel().getElementAt(index);
                if (handler != null) {
                    handler.insert(value);
                }
            }
        }
    }
    /**
     * @param list list of values.
     * @param x x mouse coordinate
     * @param y y mouse coordinate
     * @return index of element which is clicked on. Or -1 if is clicked outside of any
     * element.
     */
    public static int indexOfUnderlyingElement(final JList<?> list, final int x, final int y) {
        final int size = list.getModel().getSize();
        for (int i = 0; i < size; i++) {
            final Rectangle b = list.getCellBounds(i, i);
            if (b.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
}
