package org.protege.editor.owl.ui.view.sparql;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.utils.Graphs;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.util.graph.GraphListenerBase;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The factory to produce {@link SPARQLEngine} instances.
 */
public class SPARQLFactory {
    public static final SPARQLEngine.Res EMPTY = new ResImpl(Collections.emptyList(), Collections.emptyMap());

    static final PrefixMapping FULL_PM = OntModelFactory.STANDARD;
    static final PrefixMapping SHORT_PM = PrefixMapping.Factory.create()
            .setNsPrefix("owl", OWL.NS)
            .setNsPrefix("rdf", RDF.uri).lock();

    static void writePrefixes(PrefixMapping pm, StringBuilder res) {
        Map<String, String> prefixes = pm.getNsPrefixMap();
        for (String prefix : prefixes.keySet()) {
            res.append("PREFIX ").append(prefix).append(": <").append(prefixes.get(prefix)).append(">\n");
        }
    }

    /**
     * Creates an engine.
     *
     * @param type {@link SPARQLEngine.Type}
     * @return {@link SPARQLEngine}
     */
    public SPARQLEngine create(SPARQLEngine.Type type) {
        if (SPARQLEngine.Type.UPDATE == type) {
            return new UpdateImpl(type);
        }
        return new QueryImpl(type) {
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

    protected abstract static class Impl<X extends Prologue> implements SPARQLEngine {
        protected final Type type;

        protected Impl(Type type) {
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public Type getType() {
            return type;
        }

        protected abstract X parse(String q, Syntax s);

        protected final X parse(String q) throws Error {
            X res;
            try {
                res = parse(q, type.getSyntax());
            } catch (QueryException qe) {
                throw new Error("Can't parse query '" + q + "': " + qe.getMessage(), qe);
            }
            if (!type.test(res)) {
                throw new Error("Unsupported query type: '" + res + "' (expected " + type + ").");
            }
            return res;
        }
    }

    /**
     * A base for any SPARQL-Query
     */
    protected abstract static class QueryImpl extends Impl<Query> {

        protected QueryImpl(Type type) {
            super(type);
        }

        @Override
        protected Query parse(String q, Syntax s) {
            return QueryFactory.create(q, type.getSyntax());
        }

        @Override
        public Res execute(Model data, String query) throws Error {
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
     * SPARQL Update
     */
    protected static class UpdateImpl extends Impl<UpdateRequest> {

        protected UpdateImpl(Type type) {
            super(type);
        }

        @Override
        protected UpdateRequest parse(String q, Syntax s) {
            return UpdateFactory.create(q, type.getSyntax());
        }

        @Override
        public Res execute(Model data, String query) throws Error {
            UpdateRequest q = parse(query);
            GraphEventManager em = Graphs.getBase(data.getGraph()).getEventManager();
            UpdateImpl.GListener stats = new UpdateImpl.GListener();
            try {
                em.register(stats);
                UpdateAction.execute(q, data);
            } catch (RuntimeException re) {
                throw new Error("Unexpected error while executing '" + query + "'", re);
            } finally {
                em.unregister(stats);
            }
            List<String> header = Arrays.asList("operation", "subject", "predicate", "object");
            Map<String, List<String>> table = new HashMap<>();
            stats.triples.forEach((op, triples) -> triples.forEach(s -> {
                table.computeIfAbsent(header.get(0), x -> new ArrayList<>()).add(op.getLabel());
                table.computeIfAbsent(header.get(1), x -> new ArrayList<>()).add(s.getSubject().toString());
                table.computeIfAbsent(header.get(2), x -> new ArrayList<>()).add(s.getPredicate().toString());
                table.computeIfAbsent(header.get(3), x -> new ArrayList<>()).add(s.getObject().toString());
            }));
            return new ResImpl(header, table);
        }

        private static class GListener extends GraphListenerBase {
            private Map<Operation, Set<Triple>> triples = new EnumMap<>(Operation.class);

            @Override
            protected void addEvent(Triple t) {
                triples.computeIfAbsent(UpdateImpl.GListener.Operation.ADD, x -> new HashSet<>()).add(t);
            }

            @Override
            protected void deleteEvent(Triple t) {
                triples.computeIfAbsent(UpdateImpl.GListener.Operation.DELETE, x -> new HashSet<>()).add(t);
            }

            enum Operation {
                ADD,
                DELETE,
                ;

                public String getLabel() {
                    return name();
                }
            }
        }
    }

    /**
     * Default implementation: everything is stored in memory as a {@code Map}.
     * <p>
     * Created by @ssz on 07.01.2020.
     */
    protected static class ResImpl implements SPARQLEngine.Res {
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
