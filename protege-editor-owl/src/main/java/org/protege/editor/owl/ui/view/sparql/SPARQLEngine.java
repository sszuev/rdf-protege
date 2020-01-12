package org.protege.editor.owl.ui.view.sparql;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Jena (ARQ) based SPARQL engine.
 * Created by @ssz on 07.01.2020.
 */
public interface SPARQLEngine {

    /**
     * Executes the given query.
     *
     * @param data   {@link Model Jena RDF Model} to execute on
     * @param query, {@code String}, not {@code null}
     * @return {@link Res}
     * @throws Error - something is wrong while executing query
     */
    Res executeQuery(Model data, String query) throws Error;

    /**
     * Answers a query type.
     *
     * @return {@link Type}
     */
    Type getType();

    /**
     * Describes query type.
     */
    enum Type {
        SELECT {
            @Override
            boolean test(Query q) {
                return q.isSelectType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = Factory.STD_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL SELECT: \n");
                Map<String, String> prefixes = pm.getNsPrefixMap();
                for (String prefix : prefixes.keySet()) {
                    res.append("PREFIX ").append(prefix).append(": <").append(prefixes.get(prefix)).append(">\n");
                }
                return res.append("SELECT ?subject ?object\n{\n  ?subject ")
                        .append(pm.shortForm(RDFS.subClassOf.getURI()))
                        .append(" ?object .\n}")
                        .toString();
            }
        }, ASK {
            @Override
            boolean test(Query q) {
                return q.isAskType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = Factory.SHORT_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL ASK: \n");
                Map<String, String> prefixes = pm.getNsPrefixMap();
                for (String prefix : prefixes.keySet()) {
                    res.append("PREFIX ").append(prefix).append(": <").append(prefixes.get(prefix)).append(">\n");
                }
                return res.append("ASK\n{\n  ?s ")
                        .append(pm.shortForm(RDF.type.getURI())).append(" ")
                        .append(pm.shortForm(OWL.Ontology.getURI())).append(" .\n}").toString();
            }
        }, JSON {
            @Override
            public String getSampleQuery() {
                return "# Example of ARQ JSON Query: \n" +
                        "JSON {\n  \"s\": ?s , \"p\": ?p , \"o\" : ?o \n} WHERE {\n   ?s ?p ?o \n}";
            }

            @Override
            Syntax getSyntax() {
                return Syntax.syntaxARQ;
            }

            @Override
            boolean test(Query q) {
                return q.isJsonType();
            }
        }, DESCRIBE {
            @Override
            boolean test(Query q) {
                return q.isDescribeType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = Factory.SHORT_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL DESCRIBE: \n");
                Map<String, String> prefixes = pm.getNsPrefixMap();
                for (String prefix : prefixes.keySet()) {
                    res.append("PREFIX ").append(prefix).append(": <").append(prefixes.get(prefix)).append(">\n");
                }
                return res.append("DESCRIBE ?s WHERE\n{\n  ?s ")
                        .append(pm.shortForm(RDF.type.getURI())).append(" ")
                        .append(pm.shortForm(OWL.Ontology.getURI())).append(" .\n}").toString();
            }
        }, CONSTRUCT {
            @Override
            boolean test(Query q) {
                return q.isConstructType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = Factory.STD_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL CONSTRUCT: \n");
                Map<String, String> prefixes = pm.getNsPrefixMap();
                for (String prefix : prefixes.keySet()) {
                    res.append("PREFIX ").append(prefix).append(": <").append(prefixes.get(prefix)).append(">\n");
                }
                return res.append("CONSTRUCT\n{\n  ?s ")
                        .append(pm.shortForm(RDFS.comment.getURI()))
                        .append(" 'Autogenerated ontology header (comment)' .\n}\nWHERE\n{\n  ?s ")
                        .append(pm.shortForm(RDF.type.getURI())).append(" ")
                        .append(pm.shortForm(OWL.Ontology.getURI())).append(" .\n}").toString();
            }
        };

        Syntax getSyntax() {
            return Syntax.defaultSyntax;
        }

        abstract boolean test(Query q);

        /**
         * Answers the sample SPARQL-query.
         *
         * @return String
         */
        public abstract String getSampleQuery();
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

    /**
     * The factory to produce {@link SPARQLEngine} instances.
     */
    class Factory {
        public static final Res EMPTY = new ResImpl(Collections.emptyList(), Collections.emptyMap());

        private static final PrefixMapping STD_PM = OntModelFactory.STANDARD;
        private static final PrefixMapping SHORT_PM = PrefixMapping.Factory.create()
                .setNsPrefix("owl", OWL.NS)
                .setNsPrefix("rdf", RDF.uri).lock();

        /**
         * Creates an engine.
         *
         * @param type {@link Type}
         * @return {@link SPARQLEngine}
         */
        public SPARQLEngine create(Type type) {
            return new Impl(type) {
                @Override
                protected Res exec(QueryExecution qe) {
                    switch (type) {
                        case SELECT:
                            return ResImpl.collectSelect(qe.execSelect());
                        case ASK:
                            return ResImpl.collectAsk(qe.execAsk());
                        case JSON:
                            return ResImpl.collectJson(qe.execJson());
                        case DESCRIBE:
                            return ResImpl.collectModel(qe.execDescribe(ModelFactory.createDefaultModel()));
                        case CONSTRUCT:
                            Model res = qe.execConstruct(ModelFactory.createDefaultModel());
                            extractModel(qe).add(res);
                            return ResImpl.collectModel(res);
                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            };
        }

        protected abstract static class Impl implements SPARQLEngine {
            private final Type type;

            protected Impl(Type type) {
                this.type = Objects.requireNonNull(type);
            }

            protected Query parse(String q) throws Error {
                Query res;
                try {
                    res = QueryFactory.create(q, type.getSyntax());
                } catch (QueryException qe) {
                    throw new Error("Can't parse query '" + q + "': " + qe.getMessage(), qe);
                }
                if (!type.test(res)) {
                    throw new Error("Unsupported query type: '" + res + "' (expected " + type + ").");
                }
                return res;
            }

            @Override
            public Type getType() {
                return type;
            }

            @Override
            public Res executeQuery(Model data, String query) throws Error {
                try (QueryExecution qe = QueryExecutionFactory.create(parse(query), data)) {
                    return exec(qe);
                } catch (RuntimeException re) {
                    throw new Error("Unexpected error while executing '" + query + "'", re);
                }
            }

            protected abstract Res exec(QueryExecution qe);

            protected Model extractModel(QueryExecution qe) {
                return ModelFactory.createModelForGraph(qe.getDataset().asDatasetGraph().getGraph(null));
            }
        }

        /**
         * Default implementation: everything is stored in memory as a {@code Map}.
         * <p>
         * Created by @ssz on 07.01.2020.
         */
        protected static class ResImpl implements Res {
            private final List<String> header;
            private final Map<String, List<String>> table;

            protected ResImpl(List<String> header, Map<String, List<String>> data) {
                if (!data.isEmpty() && data.size() != header.size()) {
                    throw new IllegalStateException();
                }
                this.header = header;
                this.table = data;
            }

            public static ResImpl collectSelect(ResultSet res) {
                List<String> header = res.getResultVars();
                Map<String, List<String>> table = new HashMap<>();
                while (res.hasNext()) {
                    QuerySolution qs = res.next();
                    for (String h : header) {
                        RDFNode n = qs.get(h);
                        table.computeIfAbsent(h, k -> new ArrayList<>()).add(n == null ? null : n.toString());
                    }
                }
                return new ResImpl(header, table);
            }

            public static ResImpl collectAsk(boolean res) {
                List<String> header = Collections.singletonList("Result:");
                Map<String, List<String>> table = new HashMap<>();
                table.put(header.get(0), Collections.singletonList(Boolean.toString(res)));
                return new ResImpl(header, table);
            }

            public static ResImpl collectJson(JsonArray res) {
                List<String> header = Collections.singletonList("Results:");
                Map<String, List<String>> table = new HashMap<>();
                List<String> values = res.stream().map(JsonValue::toString).collect(Collectors.toList());
                table.put(header.get(0), values);
                return new ResImpl(header, table);
            }

            public static ResImpl collectModel(Model m) {
                List<String> header = Arrays.asList("subject", "predicate", "object");
                Map<String, List<String>> table = new HashMap<>();
                m.listStatements().forEachRemaining(s -> {
                    table.computeIfAbsent(header.get(0), x -> new ArrayList<>()).add(s.getSubject().toString());
                    table.computeIfAbsent(header.get(1), x -> new ArrayList<>()).add(s.getPredicate().toString());
                    table.computeIfAbsent(header.get(2), x -> new ArrayList<>()).add(s.getObject().toString());
                });
                return new ResImpl(header, table);
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
    }
}
