/**
 *
 */
package se.redfield.knime.neo4j.reader;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@FunctionalInterface
public interface ValueInsertHandler {
    void insert(String value);
}