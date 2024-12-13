/**
 *
 */
package se.redfield.knime.memgraph.json;

import java.io.StringWriter;
import java.util.List;

import org.neo4j.driver.Record;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import se.redfield.knime.memgraph.db.Neo4jDataConverter;

/**
 * @author redfield.ai
 *
 */
public class JsonBuilder {
    private final Neo4jDataConverter adapter;
    public JsonBuilder(final Neo4jDataConverter types) {
        super();
        this.adapter = types;
    }

    private void writeJson(final List<Record> records, final JsonGenerator gen) {
        writeJson(records, gen, adapter);
    }

    /**
     * @param records records to convert to JSON.
     * @param gen JSON generator.
     * @param adapter
     */
    public static void writeJson(final List<Record> records,
            final JsonGenerator gen, final Neo4jDataConverter adapter) {
        final Neo4jValueWriter wr = new Neo4jValueWriter(gen, adapter);

        gen.writeStartArray();
        for (final Record r : records) {
            gen.writeStartObject();
            for (final String key: r.keys()) {
                gen.writeKey(key);
                adapter.convert(r.get(key), wr);
            }
            gen.writeEnd();
        }
        gen.writeEnd();
    }
    /**
     * @param records records to convert to JSON.
     * @return JSON as string.
     */
    public String buildJson(final List<Record> records) {
        final StringWriter wr = new StringWriter();

        final JsonGenerator gen = Json.createGenerator(wr);
        writeJson(records, gen);
        gen.flush();

        return wr.toString();
    }
}
