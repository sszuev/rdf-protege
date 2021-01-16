package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.OntVocabulary;
import com.github.owlcs.ontapi.jena.utils.Graphs;
import com.github.owlcs.ontapi.jena.utils.Iter;
import com.github.owlcs.ontapi.jena.utils.Models;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.protege.editor.owl.model.hierarchy.HierarchyProviderListener;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.view.rdf.utils.OWLModelUtils;
import org.protege.editor.owl.ui.view.rdf.utils.OWLTripleUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link OWLHierarchyProvider} for {@link Graph}.
 * Describes RDF hierarchy.
 * <p>
 * Created by @ssz on 23.11.2019.
 */
@SuppressWarnings("WeakerAccess")
public class RDFHierarchyProvider implements OWLHierarchyProvider<Triple> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDFHierarchyProvider.class);

    public static final PrefixMapping STANDARD_PREFIXES = PrefixMapping.Factory.create()
            .setNsPrefixes(OntModelFactory.STANDARD)
            .setNsPrefix("dc", "http://purl.org/dc/elements/1.1/")
            .setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#")
            .setNsPrefix("swrl", "http://www.w3.org/2003/11/swrl#")
            .setNsPrefix("swrlb", "http://www.w3.org/2003/11/swrlb#")
            .lock();

    public static final OntVocabulary VOCABULARY = OntVocabulary.Factory.get();

    public static final Comparator<String> STRING_URI_COMPARATOR = Comparator.comparingInt(RDFHierarchyProvider::nsToInt)
            .thenComparing(Function.identity());
    public static final Comparator<Node> NODE_URI_COMPARATOR = Comparator.comparing(Node::getURI, STRING_URI_COMPARATOR);
    public static final Comparator<Resource> RESOURCE_URI_COMPARATOR = Comparator.comparing(Resource::getURI, STRING_URI_COMPARATOR);

    public static final Comparator<Triple> TRIPLE_PREDICATE_COMPARATOR =
            Comparator.comparing(Triple::getPredicate, NODE_URI_COMPARATOR).thenComparing(Triple::hashCode);

    public static final Set<Property> STANDARD_PROPERTIES = VOCABULARY.getSystemProperties().stream()
            .sorted(RESOURCE_URI_COMPARATOR)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    public static final Set<Resource> STANDARD_DATATYPES = VOCABULARY.getBuiltinDatatypes().stream()
            .sorted(RESOURCE_URI_COMPARATOR)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    private final List<HierarchyProviderListener<Triple>> listeners = new CopyOnWriteArrayList<>();

    private Ontology ontology;
    private Graph graph;

    /**
     * Decides is the given graph is big.
     * For big graphs sorting is disabled. Non-memory graphs are considered as big by default.
     *
     * @param g {@link Graph}, possibly {@code null}
     * @return boolean
     */
    public static boolean isBig(Graph g) {
        return !Graphs.baseGraphs(g).allMatch(x -> x instanceof GraphMem) || g.size() > 30_000;
    }

    private static String toString(Node n) {
        return n.isURI() ? n.getURI() : n.getBlankNodeId().toString();
    }

    /**
     * Gets the number according to namespace.
     * Used while sorting.
     * @param uri String, not {@code null}
     * @return int
     */
    static int nsToInt(String uri) {
        if (uri.equals(RDF.type.getURI())) {
            return 1;
        }
        if (uri.startsWith(RDF.getURI())) {
            return 10;
        }
        if (uri.startsWith(RDFS.getURI())) {
            return 20;
        }
        if (uri.startsWith(OWL.getURI())) {
            return 30;
        }
        return 100;
    }

    /**
     * Gets spliterator' characteristics.
     * @param graph {@link Graph}
     * @return int
     * @see com.github.owlcs.ontapi.jena.impl.OntGraphModelImpl
     */
    static int characteristics(Graph graph) {
        return Graphs.isDistinct(graph) ? Spliterator.NONNULL | Spliterator.DISTINCT : Spliterator.NONNULL;
    }

    /**
     * Collects the root {@link Triple}s as a {@code Set}.
     * The ontology header is expected first.
     * @param graph {@link Graph}, not {@code null}
     * @return a {@code Set} of {@link Triple}s
     */
    public static Set<Triple> collectRoots(Graph graph) {
        return isBig(graph) ? collectOrderedRoots(graph) : collectSortedRoots(graph);
    }

    private static Set<Triple> collectSortedRoots(Graph graph) {
        // header always first, then blanks, then any other roots sorted alphabetically
        Set<Triple> header = graph.find(Node.ANY, RDF.type.asNode(), OWL.Ontology.asNode()).toSet();
        Comparator<Triple> comp = Comparator.comparing((Function<Triple, Boolean>) header::contains)
                .thenComparing(t -> t.getSubject().isBlank())
                .thenComparing(Comparator.comparing((Triple t) -> toString(t.getSubject())).reversed())
                .thenComparing(Triple::hashCode) // otherwise TreeSet#contains does not work
                .reversed();
        Set<Triple> res = new TreeSet<>(comp);
        collectRoots(graph, res);
        return res;
    }

    private static Set<Triple> collectOrderedRoots(Graph graph) {
        // header always first
        Set<Triple> res = new LinkedHashSet<>();
        graph.find(Node.ANY, RDF.type.asNode(), OWL.Ontology.asNode())
                .mapWith(RootTriple::new).forEachRemaining(res::add);
        collectRoots(graph, res);
        return res;
    }

    private static void collectRoots(Graph graph, Collection<Triple> res) {
        graph.find().forEachRemaining(triple -> {
            if (res.contains(triple)) {
                return;
            }
            Node s = triple.getSubject();
            if (!s.isURI() && graph.contains(Node.ANY, Node.ANY, s)) {
                return;
            }
            RootTriple r = new RootTriple(findRootTriple(graph, s).orElse(triple));
            res.add(r);
        });
    }

    public static Optional<Triple> findRootTriple(Graph graph, Node top) {
        TreeSet<Triple> res = new TreeSet<>(TRIPLE_PREDICATE_COMPARATOR);
        graph.find(top, Node.ANY, Node.ANY)
                .filterDrop(x -> !x.getObject().isURI())
                .forEachRemaining(res::add);
        if (res.isEmpty())
            return Optional.empty();
        return Optional.of(res.first());
    }

    @Override
    public void setOntology(OWLOntology o) {
        this.ontology = OWLAdapter.get().asONT(o);
        // this.graph = ((Ontology) o).asGraphModel().getGraph(); // <-- concurrent union graph
        // todo: currently, only the base graph (i.e. GraphMeme) is used. for simplification. temporary ?
        this.graph = OWLModelUtils.getGraphModel(ontology).getBaseGraph();
        fireHierarchyChanged();
    }

    public Ontology getOntology() {
        return ontology;
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        setOntology(Objects.requireNonNull(ontologies).iterator().next());
    }

    public PrefixMapping getPrefixes() {
        return graph == null ? STANDARD_PREFIXES : graph.getPrefixMapping();
    }

    public Graph getGraph() {
        return graph;
    }

    @Override
    public final Set<Triple> getRoots() {
        if (graph == null) return Collections.emptySet();
        LOGGER.debug("Start collecting roots for {}", Graphs.getName(graph));
        Set<Triple> res = collectRoots(graph);
        LOGGER.debug("There are {} root triples in {}", graph.size(), Graphs.getName(graph));
        return res;
    }

    @Override
    public final Set<Triple> getChildren(Triple triple) {
        return findChildren(triple)
                .map(it -> {
                    Set<Triple> res = new TreeSet<>(TRIPLE_PREDICATE_COMPARATOR);
                    it.forEachRemaining(res::add);
                    return res;
                })
                .orElse(Collections.emptySet());
    }

    @Override
    public final Stream<Triple> children(Triple triple) {
        return findChildren(triple)
                .map(it -> Iter.asStream(it, -1, characteristics(graph)).sorted(TRIPLE_PREDICATE_COMPARATOR))
                .orElseGet(Stream::empty);
    }

    protected Optional<ExtendedIterator<Triple>> findChildren(Triple triple) {
        if (graph == null) return Optional.empty();
        Node o = triple.getObject();
        if (o.isBlank()) {
            return Optional.of(listChildren(o));
        }
        if (OWLTripleUtils.isRoot(triple)) {
            return Optional.of(listChildren(triple.getSubject()).filterDrop(triple::equals));
        }
        return Optional.empty();
    }

    protected ExtendedIterator<Triple> listChildren(Node o) {
        return graph.find(o, Node.ANY, Node.ANY);
    }

    @Override
    public final Set<Triple> getParents(Triple triple) {
        return findParents(triple).map(ExtendedIterator::toSet).orElse(Collections.emptySet());
    }

    @Override
    public final Stream<Triple> parents(Triple triple) {
        return findParents(triple).map(it -> Iter.asStream(it, -1, characteristics(graph))).orElseGet(Stream::empty);
    }

    protected Optional<ExtendedIterator<Triple>> findParents(Triple triple) {
        if (graph == null) return Optional.empty();
        Node s = triple.getSubject();
        if (s.isBlank()) {
            return Optional.of(listParents(s));
        }
        if (OWLTripleUtils.isRoot(triple)) {
            return Optional.empty();
        }
        return findRootTriple(graph, s).filter(x -> !triple.equals(x)).map(Iter::of);
    }

    protected ExtendedIterator<Triple> listParents(Node s) {
        return graph.find(Node.ANY, Node.ANY, s);
    }

    @Override
    public boolean containsReference(Triple triple) {
        return graph.contains(triple);
    }

    @Override
    public Set<Triple> getEquivalents(Triple triple) {
        return Collections.emptySet();
    }

    @Override
    public Set<Triple> getDescendants(Triple triple) {
        Set<Triple> res = new HashSet<>();
        Set<Node> anons = new HashSet<>();
        if (OWLTripleUtils.isRoot(triple)) {
            listChildren(triple.getSubject())
                    .filterDrop(triple::equals)
                    .forEachRemaining(t -> {
                        if (t.getObject().isBlank())
                            anons.add(t.getObject());
                        res.add(t);
                    });
        }
        if (triple.getObject().isBlank()) {
            anons.add(triple.getObject());
        }
        if (!anons.isEmpty()) {
            Model m = ModelFactory.createModelForGraph(graph);
            anons.forEach(n -> Models.getAssociatedStatements(m.wrapAsResource(n)).forEach(x -> res.add(x.asTriple())));
        }

        return res;
    }

    @Override
    public boolean hasDescendants(Triple triple) {
        return findChildren(triple).map(x -> Iter.findFirst(x).isPresent()).orElse(false);
    }

    @Override
    public Set<Triple> getAncestors(Triple triple) {
        throw new IllegalStateException("TODO"); // TODO
    }

    @Override
    public Set<List<Triple>> getPathsToRoot(Triple triple) {
        // DFS, since need to find all paths
        return Collections.unmodifiableSet(findPathsToRoot(triple, new HashSet<>()));
    }

    private Set<List<Triple>> findPathsToRoot(Triple triple, Set<Triple> seen) {
        Node s = triple.getSubject();
        Set<List<Triple>> res = new HashSet<>();
        if (OWLTripleUtils.isRoot(triple) || (s.isURI() || !graph.contains(Node.ANY, Node.ANY, s))) {
            List<Triple> triples = new ArrayList<>();
            triples.add(triple);
            res.add(triples);
            return res;
        }
        graph.find(Node.ANY, Node.ANY, s).filterKeep(seen::add).forEachRemaining(t -> {
            Set<List<Triple>> set = findPathsToRoot(t, seen);
            set.forEach(x -> x.add(triple));
            res.addAll(set);
        });
        return res;
    }

    @Override
    public void addListener(HierarchyProviderListener<Triple> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(HierarchyProviderListener<Triple> listener) {
        listeners.remove(listener);
    }

    @Override
    public void dispose() {
        listeners.clear();
    }

    public Stream<HierarchyProviderListener<Triple>> listeners() {
        return listeners.stream();
    }

    /**
     * Fires all listeners.
     * Usually to rebuild {@link org.protege.editor.owl.ui.tree.ObjectTree tree}.
     */
    public void fireHierarchyChanged() {
        listeners().forEach(x -> {
            try {
                x.hierarchyChanged();
            } catch (Throwable e) {
                LOGGER.error("Hierarchy change exception: '{}'", e.getMessage(), e);
            }
        });
    }

}
