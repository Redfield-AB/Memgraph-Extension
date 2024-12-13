/**
 *
 */
package se.redfield.knime.memgraph.ui;

import javax.swing.ImageIcon;

/**
 * @author redfield.ai
 *
 */
public final class UiUtils {
    private UiUtils() {}

    /**
     * @return refresh icon.
     */
    public static ImageIcon createRefreshIcon() {
        return new ImageIcon(UiUtils.class.getClassLoader().getResource("/icons/refresh.png"));
    }
}
