/**
 *
 */
package se.redfield.knime.memgraph.connector;

/**
 * @author redfield.ai
 *
 */
public class Named {
    protected String name;

    public Named() {
        super();
    }
    public String getName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }
}
