/**
 *
 */
package se.redfield.knime.memgraph.ui;

import javax.swing.JSplitPane;

/**
 * @author redfield.ai
 *
 */
public class DividerLocationHolder {
    private Double locDouble;
    private Integer locInt;
    private final JSplitPane owner;

    public DividerLocationHolder(final JSplitPane owner) {
        super();
        this.owner = owner;
    }

    public void setLocation(final int location) {
        locDouble = null;
        locInt = location;
    }
    public void setLocation(final double location) {
        if (location < 0 || location > 1) {
            throw new IllegalArgumentException("JSplitPane divider location must be between 0 and 1");
        }

        locInt = null;
        locDouble = location;
    }
    public int getDividerLocation() {
        if (locInt != null) {
            return locInt;
        }

        final int size;
        if (owner.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
            size = owner.getHeight() - owner.getDividerSize();
        } else {
            size = owner.getWidth() - owner.getDividerSize();
        }

        return (int) ((locDouble == null ? 0. : locDouble) * size);
   }
}
