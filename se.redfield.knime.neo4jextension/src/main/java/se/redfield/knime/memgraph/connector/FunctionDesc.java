/**
 *
 */
package se.redfield.knime.memgraph.connector;

/**
 * @author redfield.ai
 *
 */
public class FunctionDesc extends Named implements Cloneable {
    private String name;
    private String signature;
    private String description;

    public FunctionDesc() {
        super();
    }
    public FunctionDesc(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public void setName(final String name) {
        this.name = name;
    }
    public String getSignature() {
        return signature;
    }
    public void setSignature(final String signature) {
        this.signature = signature;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(final String description) {
        this.description = description;
    }
    @Override
    public FunctionDesc clone() {
        try {
            return (FunctionDesc) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
