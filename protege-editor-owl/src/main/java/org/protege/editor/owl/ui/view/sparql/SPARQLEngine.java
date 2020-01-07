package org.protege.editor.owl.ui.view.sparql;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;

/**
 * A Jena (ARQ) based SPARQL engine.
 * Created by @ssz on 07.01.2020.
 */
public interface SPARQLEngine {

    Res EMPTY = new MemResImpl(Collections.emptyList(), Collections.emptyMap());

    /**
     * Executes the given SELECT-query.
     *
     * @param data   {@link Model Jena RDF Model} to execute on
     * @param query, {@code String}, {@code SELECT}-query, not {@code null}
     * @return {@link Res}
     * @throws Error - something is wrong while executing query
     */
    Res executeSelect(Model data, String query) throws Error;

    /**
     * Executes the given query.
     *
     * @param data   {@link Model Jena RDF Model} to execute on
     * @param query, {@code String}, not {@code null}
     * @return {@link Res}
     * @throws Error - something is wrong while executing query
     */
    default Res executeQuery(Model data, String query) throws Error {
        return executeSelect(data, query);
    }

    /**
     * Creates an engine.
     *
     * @return {@link SPARQLEngine}
     */
    static SPARQLEngine create() {
        return (m, q) -> {
            Query query;
            try {
                query = QueryFactory.create(q);
            } catch (QueryException qe) {
                throw new Error("Can't parse query '" + q + "'", qe);
            }
            if (!query.isSelectType()) {
                throw new Error("Unsupported query type: '" + query + "'");
            }
            try (QueryExecution qe = QueryExecutionFactory.create(query, m)) {
                return MemResImpl.collect(qe.execSelect());
            } catch (RuntimeException re) {
                throw new Error("Unexpected error while executing '" + query + "'", re);
            }
        };
    }

    /**
     * Answers the sample SPARQL-query.
     *
     * @return String
     */
    static String getSampleQuery() {
        StringBuilder res = new StringBuilder();
        Map<String, String> prefixes = OntModelFactory.STANDARD.getNsPrefixMap();
        for (String prefix : prefixes.keySet()) {
            res.append("PREFIX ").append(prefix).append(": <").append(prefixes.get(prefix)).append(">\n");
        }
        res.append("SELECT ?subject ?object\n\tWHERE { ?subject rdfs:subClassOf ?object }");
        return res.toString();
    }

    /**
     * Describes a result set table.
     */
    interface Res {
        int getColumnCount();

        int getRowCount();

        String getColumnName(int col);

        Object getResult(int row, int col);
    }

    /**
     * Default implementation: everything is stored in memory as a {@code Map}.
     * <p>
     * Created by @ssz on 07.01.2020.
     */
    class MemResImpl implements Res {
        private final List<String> header;
        private final Map<String, List<String>> table;

        protected MemResImpl(List<String> header, Map<String, List<String>> data) {
            if (!data.isEmpty() && data.size() != header.size()) {
                throw new IllegalStateException();
            }
            this.header = header;
            this.table = data;
        }

        public static MemResImpl collect(ResultSet res) {
            List<String> header = res.getResultVars();
            Map<String, List<String>> table = new HashMap<>();
            while (res.hasNext()) {
                QuerySolution qs = res.next();
                for (String h : header) {
                    RDFNode n = qs.get(h);
                    table.computeIfAbsent(h, k -> new ArrayList<>()).add(n == null ? null : n.toString());
                }
            }
            return new MemResImpl(header, table);
        }

        @Override
        public int getColumnCount() {
            return header.size();
        }

        @Override
        public int getRowCount() {
            return header.isEmpty() || table.isEmpty() ? 0 : table.get(header.get(0)).size();
        }

        @Override
        public String getColumnName(int col) {
            return header.get(col);
        }

        @Override
        public Object getResult(int row, int col) {
            if (table.isEmpty()) return null;
            String h = getColumnName(col);
            return table.get(h).get(row);
        }
    }

    /**
     * An exception that may happen while query execution.
     * <p>
     * Created by @ssz on 07.01.2020.
     */
    class Error extends Exception {
        public Error(String message, Throwable t) {
            super(message, t);
        }

        public Error(String message) {
            super(message);
        }
    }
}
