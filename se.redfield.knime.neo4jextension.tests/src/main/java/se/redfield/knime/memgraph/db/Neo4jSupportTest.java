/**
 *
 */
package se.redfield.knime.memgraph.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.Driver;

import se.redfield.knime.memgraph.utils.Neo4jHelper;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Neo4jSupportTest {
    private Driver driver;
    private Neo4jSupport support;

    /**
     * Default constructor.
     */
    public Neo4jSupportTest() {
        super();
    }

    @Before
    public void setUp() {
        support = Neo4jHelper.createSupport();
        driver = support.createDriver();
    }
    @After
    public void tearDown() {
        driver.close();
    }

    @Test
    public void testLoadLabesAndFunctions() throws Exception {
        final LabelsAndFunctions lf = support.loadLabesAndFunctions();

        assertNotNull(lf);
        assertFalse(lf.getFunctions().isEmpty());
    }
}
