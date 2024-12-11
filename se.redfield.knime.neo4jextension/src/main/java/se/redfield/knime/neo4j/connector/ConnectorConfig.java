/**
 *
 */
package se.redfield.knime.neo4j.connector;

import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.ICredentials;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author redfield.ai
 *
 */
public class ConnectorConfig implements Cloneable {
    private URI location;
    private AuthConfig auth;
    private int maxConnectionPoolSize;
    private String database;
    private boolean usedDefaultDbName;

    public ConnectorConfig() {
        super();
        maxConnectionPoolSize = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        database = "memgraph";
        usedDefaultDbName = true;

        try {
            this.location = new URI("bolt://localhost:7687");
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }

        auth = new AuthConfig();
        auth.setScheme(AuthScheme.basic);
        auth.setPrincipal("memgraph");
    }

    public URI getLocation() {
        return location;
    }

    public void setLocation(final URI location) {
        this.location = location;
    }

    public AuthConfig getAuth() {
        return auth;
    }

    public void setAuth(final AuthConfig auth) {
        this.auth = auth;
    }

    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public void setMaxConnectionPoolSize(final int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isUsedDefaultDbName() {
        return usedDefaultDbName;
    }

    public void setUsedDefaultDbName(boolean usedDefaultDbName) {
        this.usedDefaultDbName = usedDefaultDbName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Memgraph DB: ");
        sb.append(getLocation());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(auth, database, location, maxConnectionPoolSize, usedDefaultDbName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConnectorConfig other = (ConnectorConfig) obj;
        return Objects.equals(auth, other.auth) && Objects.equals(database, other.database)
                && Objects.equals(location, other.location) && maxConnectionPoolSize == other.maxConnectionPoolSize
                && Objects.equals(usedDefaultDbName, other.usedDefaultDbName);
    }

    @Override
    public ConnectorConfig clone() {
        try {
            final ConnectorConfig clone = (ConnectorConfig) super.clone();
            if (auth != null) {
                clone.auth = auth.clone();
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public ConnectorConfig createResolvedConfig(final CredentialsProvider cp) {
        final ConnectorConfig cfg = clone();
        final AuthConfig auth = cfg.getAuth();
        if (auth != null && auth.getScheme() == AuthScheme.flowCredentials) {
            final ICredentials c = cp.get(auth.getPrincipal());
            cfg.getAuth().setPrincipal(c.getLogin());
            cfg.getAuth().setCredentials(c.getPassword());
        }
        return cfg;
    }
}
