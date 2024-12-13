/**
 *
 */
package se.redfield.knime.memgraph.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;


import java.util.stream.Collectors;
import org.knime.core.node.ExecutionContext;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Config.ConfigBuilder;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.summary.QueryType;
import org.neo4j.driver.summary.ResultSummary;

import se.redfield.knime.memgraph.async.AsyncRunnerLauncher;
import se.redfield.knime.memgraph.connector.AuthConfig;
import se.redfield.knime.memgraph.connector.ConnectorConfig;
import se.redfield.knime.memgraph.connector.FunctionDesc;
import se.redfield.knime.memgraph.connector.NamedWithProperties;

/**
 * @author redfield.ai
 *
 */
public class Neo4jSupport {
    private final ConnectorConfig config;

    public Neo4jSupport(final ConnectorConfig config) {
        super();
        this.config = config;
    }

    public static List<Record> runRead(final Driver driver, final String query, final RollbackListener l, final String dataBase) {
        return runRead(driver, query, l, dataBase, Map.of());
    }
    public static List<Record> runRead(final Driver driver, final String query, final RollbackListener l, final String dataBase, final Map<String, Object> parameters) {
        return runWithSession(driver, s ->  runInReadOnlyTransaction(s, query, l, parameters), dataBase);
    }

    /**
     * @param session session.
     * @param query query.
     * @param listener rollback listener.
     * @return execution result.
     */
    public static List<Record> runInReadOnlyTransaction(final Session session,
                                                        final String query,
                                                        final RollbackListener listener) {

        return runInReadOnlyTransaction(session, query, listener, Map.of());
    }

    /**
     * @param session session.
     * @param query query.
     * @param listener rollback listener.
     * @param parameters parameters.
     * @return execution result.
     */
    public static List<Record> runInReadOnlyTransaction(final Session session, final String query,
            final RollbackListener listener, final Map<String, Object> parameters) {
        final Transaction tx = session.beginTransaction();
        try {
            final Result run = tx.run(query, parameters);
            final List<Record> list = run.list();

            final ResultSummary summary = run.consume();
            if (summary.queryType() != QueryType.READ_ONLY) {
                tx.rollback();
                if (listener != null) {
                    listener.isRolledBack();
                }
            }
            return list;
        } finally {
            tx.close();
        }
    }
    public static <R> R runWithSession(final Driver driver, final WithSessionRunnable<R> r, final String dataBase) {
        final Session s;

        if (dataBase != null){
            s = driver.session(SessionConfig.forDatabase(dataBase));
        } else {
            s = driver.session();
        }

        try {
            return r.run(s);
        } finally {
            s.close();
        }
    }

    public Driver createDriver() {
        return createDriver(config);
    }
    public ContextListeningDriver createDriver(final ExecutionContext context) {
        final Driver d = createDriver(config);
        return new ContextListeningDriver(d, context);
    }
    /**
     * @param con Neo4J configuration.
     * @return Neo4J driver.
     */
    private static Driver createDriver(final ConnectorConfig con) {
        final AuthConfig auth = con.getAuth();
        final AuthToken token = auth == null ? null :  AuthTokens.basic(
                auth.getPrincipal(), auth.getCredentials(), null);

        final Driver d = GraphDatabase.driver(con.getLocation(), token,
                createConfig(con.getMaxConnectionPoolSize()));
        try {
            d.verifyConnectivity();
        } catch (final RuntimeException e) {
            d.close();
            throw e;
        }
        return d;
    }
    private static Config createConfig(final int poolSize) {
        final ConfigBuilder cfg = Config.builder();
        cfg.withMaxConnectionPoolSize(poolSize);
        return cfg.build();
    }
    public LabelsAndFunctions loadLabesAndFunctions() throws Exception {
        final Map<String, NamedWithProperties> nodes = new HashMap<>();
        final Map<String, NamedWithProperties> relationships = new HashMap<>();
        final List<FunctionDesc> functions = new LinkedList<>();

        final Driver driver = createDriver();
        try {
            final List<WithSessionRunnable<Void>> runs = new ArrayList<>(3);
            //runs.add(s -> loadNamedWithProperties(s, nodes)); //call db.labels()
            runs.add(s -> loadNodeLabelPropertiess(s, nodes));
            runs.add(s -> loadNamedWithProperties(s, relationships));
            runs.add(s -> loadRelationshipProperties(s, relationships));
            runs.add(s -> loadFunctions(s, functions));

            final AsyncRunnerLauncher<WithSessionRunnable<Void>, Void> runner
                = AsyncRunnerLauncher.Builder.<WithSessionRunnable<Void>, Void>newBuilder()
                    .withRunner((r) -> runWithSession(driver, r, config.getDatabase()))
                    .withSource(runs.iterator())
                    .withNumThreads(runs.size())
                    .withStopOnFailure(true)
                    .build();
            runner.run();

            if (runner.hasErrors()) {
                throw new Exception("Failed to read Neo4j DB metadata");
            }
        } finally {
            driver.closeAsync();
        }

        final LabelsAndFunctions data = new LabelsAndFunctions();
        data.getNodes().addAll(new LinkedList<NamedWithProperties>(nodes.values()));
        data.getRelationships().addAll(new LinkedList<NamedWithProperties>(relationships.values()));
        data.getFunctions().addAll(functions);

        return data;
    }


//    private Void loadNamedWithProperties(final Session s, final Map<String, NamedWithProperties> map) {
//        // Use DISTINCT to get unique labels
//        final List<Record> result = s.readTransaction(tx -> tx.run(
//            "MATCH (n) " +
//            "RETURN DISTINCT labels(n) AS labels"
//        ).list());
//        
//        for (final Record r : result) {
//            List<Object> labels = r.get("labels").asList();
//            for (Object labelObj : labels) {
//                final String type = (String) labelObj;
//                synchronized (map) {
//                    if (!map.containsKey(type)) {
//                        map.put(type, new NamedWithProperties(type));
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    private Void loadNodeLabelPropertiess(final Session s, final Map<String, NamedWithProperties> map) {
//        // Fetch node properties for each label
//        final List<Record> result = s.readTransaction(tx -> tx.run(
//            "MATCH (n) " +
//            "WITH DISTINCT labels(n) AS lbls, keys(n) AS props " +
//            "UNWIND lbls AS label " +
//            "UNWIND props AS prop " +
//            "RETURN DISTINCT label, prop"
//        ).list());
//        
//        for (final Record r : result) {
//            final String type = r.get("label").asString();
//            final String property = r.get("prop").asString();
//
//            NamedWithProperties n;
//            synchronized (map) {
//                n = map.get(type);
//                if (n == null) {
//                    n = new NamedWithProperties(type);
//                    map.put(type, n);
//                }
//            }
//
//            if (property != null && !property.equals("null")) {
//                n.getProperties().add(property);
//            }
//        }
//        return null;
//    }
//
//    private Void loadRelationshipProperties(final Session s, final Map<String, NamedWithProperties> map) {
//        // Fetch relationship types and properties
//        final List<Record> result = s.readTransaction(tx -> tx.run(
//            "MATCH ()-[r]-() " +
//            "WITH DISTINCT type(r) AS relType, keys(r) AS props " +
//            "UNWIND props AS prop " +
//            "RETURN DISTINCT relType, prop"
//        ).list());
//        
//        for (final Record r : result) {
//            String type = r.get("relType").asString();
//            final String property = r.get("prop").asString();
//
//            NamedWithProperties n;
//            synchronized (map) {
//                n = map.get(type);
//                if (n == null) {
//                    n = new NamedWithProperties(type);
//                    map.put(type, n);
//                }
//            }
//
//            if (property != null && !property.equals("null")) {
//                n.getProperties().add(property);
//            }
//        }
//        return null;
//    }

   
    private Void loadNamedWithProperties(final Session s, final Map<String, NamedWithProperties> map) {
        // Use schema.node_type_properties to get node labels
        final List<Record> result = s.readTransaction(tx -> tx.run(
            "CALL schema.node_type_properties() YIELD nodeLabels"
        ).list());
        for (final Record r : result) {
            List<Object> labels = r.get("nodeLabels").asList();
                        
            for (Object labelObj : labels) {
                final String type = (String) labelObj;
                synchronized (map) {
                    if (!map.containsKey(type)) {
                        map.put(type, new NamedWithProperties(type));
                    }
                }
            }
        }
        return null;
    }

    private Void loadNodeLabelPropertiess(final Session s, final Map<String, NamedWithProperties> map) {
        // Use schema.node_type_properties to get node properties
        final List<Record> result = s.readTransaction(tx -> tx.run(
            "CALL schema.node_type_properties() YIELD nodeType, nodeLabels, propertyName, propertyTypes"
        ).list());
        
        for (final Record r : result) {
            List<Object> labels = r.get("nodeLabels").asList();
            final String property = r.get("propertyName").asString();
            final List<Object> propertyTypes = r.get("propertyTypes").asList();

            for (Object labelObj : labels) {
                final String type = (String) labelObj;

                NamedWithProperties n;
                synchronized (map) {
                    n = map.get(type);
                    if (n == null) {
                        n = new NamedWithProperties(type);
                        map.put(type, n);
                    }
                }

                if (property != null && !property.equals("null")) {
                    n.getProperties().add(property);

                }
            }
        }
        return null;
    }

    private Void loadRelationshipProperties(final Session s, final Map<String, NamedWithProperties> map) {
        // Use schema.rel_type_properties to get relationship properties
        final List<Record> result = s.readTransaction(tx -> tx.run(
            "CALL schema.rel_type_properties() YIELD relType, propertyName, propertyTypes"
        ).list());
        
        for (final Record r : result) {
            String type = r.get("relType").asString();
            final String property = r.get("propertyName").asString();
            final List<Object> propertyTypes = r.get("propertyTypes").asList();

            NamedWithProperties n;
            synchronized (map) {
                n = map.get(type);
                if (n == null) {
                    n = new NamedWithProperties(type);
                    map.put(type, n);
                }
            }

            if (property != null && !property.equals("null")) {
                n.getProperties().add(property);

            }
        }
        return null;
    }

 

    private Void loadFunctions(final Session s, final List<FunctionDesc> functions) {
        // Manually define known Memgraph and Cypher functions
        List<FunctionDesc> predefinedFunctions = Arrays.asList(
            
            
        	
    		createFunction("assert", "Raises an exception if the given argument is not true.", "assert(expression: boolean, message: string = null) -> ()"),
            createFunction("coalesce", "Returns the first non-null value in the given list of expressions.", "coalesce(expression: any [, expression: any]*) -> (any)"),
            createFunction("degree", "Returns the number of relationships (both incoming and outgoing) of a node.", "degree(node: Node) -> (integer)"),
            createFunction("endNode", "Returns the destination node of a relationship.", "endNode(relationship: Relationship) -> (Node)"),
            createFunction("range", "Constructs a list of value in given range.", "range(start-number: integer, end-number: integer, increment: integer = 1) -> (List[integer])"),
            createFunction("reduce", "Accumulate list elements into a single result by applying an expression.", "reduce(accumulator = initial_value, variable IN list|expression)"),
            createFunction("uniformSample", "Returns elements of a given list randomly oversampled or undersampled to desired size", "uniformSample(list: List[any], size: integer) -> (List[any])"),
            createFunction("all", "Check if all elements of a list satisfy a predicate.\n NOTE: Whenever possible, use Memgraph's lambda functions when matching instead.", "uniformSample(list: List[any], size: integer) -> (List[any])"),
            createFunction("exist", "Checks if a pattern exists as part of the filtering clause. Symbols provided in the MATCH clause can also be used here. The function can be used only with the WHERE clause.", "exists(pattern: Pattern)"),
            createFunction("propertySize", "propertySize(entity: Node|Relationship, property-name: string) -> (integer)", "Returns the total amount of bytes stored in RAM for the property of a given entity node or relationship. For more information, check storage of properties inside Memgraph."),
            createFunction("randomUUID", "Returns randomly-generated Universally Unique Identifier (UUID)", "randomUUID() -> (string)"),
            createFunction("toBoolean","Converts the input argument to a boolean value, regardless of case sensitivity. The values true and false are directly converted to true or false, respectively. Additionally, the strings \"true\" and \"t\" are mapped to true, while the strings \"false\" and \"f\" are mapped to false.", "toBoolean(value: boolean|integer|string) -> (boolean)"),
            
            // Cypher Standard Functions
            createFunction("id", "Get node/relationship ID", "id(node/relationship)"),
            createFunction("labels", "Get node labels", "labels(node)"),
            createFunction("type", "Get relationship type", "type(relationship)"),
            
            // Math Functions
            createFunction("abs", "Absolute value", "abs(number)"),
            createFunction("ceil", "Round up", "ceil(number)"),
            createFunction("floor", "Round down", "floor(number)"),
            createFunction("round", "Round to nearest", "round(number)"),
            createFunction("avg", "Returns an average value of rows with numerical values generated with the MATCH or UNWIND clause.", "avg(row: int|float) -> (float)"),
            createFunction("collect", "Returns a single aggregated list containing returned values.", "collect(values: any) -> (List[any])"),
            createFunction("count", "Counts the number of non-null values returned by the expression.", "count(values: any) -> (integer)"),
            
            // String Functions
            createFunction("toString", "Convert to string", "toString(value)"),
            createFunction("toUpper", "Uppercase", "toUpper(string)"),
            createFunction("toLower", "Lowercase", "toLower(string)"),
            createFunction("contains", "Check if the first argument has an element which is equal to the second argument.", "contains(string: string, substring: string) -> (boolean)"),
            createFunction("endsWith", "Check if the first argument ends with the second.", "endsWith(string: string, substring: string) -> (boolean)"),
            createFunction("left", "Returns a string containing the specified number of leftmost characters of the original string.", "left(string: string, count: integer) -> (string)"),
            createFunction("lTrim", "Returns the original string with leading whitespace removed.", "lTrim(string: string) -> (string)"),
            createFunction("replace", "Returns a string in which all occurrences of a specified string in the original string have been replaced by another (specified) string.", "replace(string: string, search-string: string, replacement-string: string) -> (string)"),
            createFunction("reverse", "Returns a string in which the order of all characters in the original string have been reversed.", "reverse(string: string) -> (string)"),
            
            // Type Conversion
            createFunction("toInteger", "Convert to integer", "toInteger(value)"),
            createFunction("toFloat", "Convert to float", "toFloat(value)"),
            createFunction("toBoolean", "Convert to boolean", "toBoolean(value)"),
            
            // date
            createFunction("duration", "Returns the data type that represents a period of time", "duration(value: string|Duration) -> (Duration)"),
            createFunction("date", "Returns the data type that reprsents a date with year, month, and day", "date(value: string|Date|LocalDateTime) -> (Date)"),
            createFunction("localTime", "Returns the data type that represents time within a day without timezone.", "localDateTime(value: string|LocalDateTime)-> (LocalDateTime)"),
            createFunction("timestamp", "Current timestamp", "timestamp()"),
            
            
            
            // Community detection
            createFunction("community_detection", "Computes graph communities using the Louvain method", "community_detection.get()"),
            createFunction("leiden_community_detection", "Computes graph communities using the Leiden algorithm", "leiden_community_detection.get()")
        );
        
        functions.addAll(predefinedFunctions);
        return null;
    }

    private FunctionDesc createFunction(String name, String description, String signature) {
        FunctionDesc f = new FunctionDesc();
        f.setName(name);
        f.setDescription(description);
        f.setSignature(signature);
        return f;
    }

    

    
    private void putToFunctions(final List<Record> records, final List<FunctionDesc> functions) {
        for (final Record r : records) {
            final FunctionDesc f = new FunctionDesc();
            f.setName(r.get("name").asString());
            f.setSignature(r.get("signature").asString());
            f.setDescription(r.get("description").asString());
            functions.add(f);
        }
    }

    public ConnectorConfig getConfig() {
        return config;
    }
}



