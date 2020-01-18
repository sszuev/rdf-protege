package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.utils.Graphs;
import com.github.owlcs.ontapi.jena.utils.Iter;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.protege.editor.owl.model.hierarchy.HierarchyProviderListener;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * An {@link OWLHierarchyProvider} for {@link Graph}.
 * Created by @ssz on 23.11.2019.
 */
@SuppressWarnings("WeakerAccess")
public class TripleHierarchyProvider implements OWLHierarchyProvider<Triple> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TripleHierarchyProvider.class);

    private static final Map<Graph, Comparator<?>> COMPARATORS_CACHE = new WeakHashMap<>();

    private final List<HierarchyProviderListener<Triple>> listeners = new CopyOnWriteArrayList<>();
    private final Map<BlankNodeId, String> ids = new HashMap<>();
    private Graph graph;

    static int characteristics(Graph graph) {
        return Graphs.isDistinct(graph) ? Spliterator.NONNULL | Spliterator.DISTINCT : Spliterator.NONNULL;
    }

    @SuppressWarnings("unchecked")
    public static Comparator<? super Triple> createComparatorFor(Graph g) {
        return g == null ? null : (Comparator<? super Triple>) COMPARATORS_CACHE.computeIfAbsent(g,
                k -> GraphUtils.isBig(k) ? null : GraphUtils.getComparator(k));
    }

    @Override
    public void setOntology(OWLOntology o) {
        // this.graph = ((Ontology) o).asGraphModel().getGraph(); // <-- concurrent union graph
        // todo: currently only the base (GraphMeme) graph is used. for simplification. temporary ?
        this.graph = OWLAdapter.get().asBaseModel(OWLAdapter.get().asONT(o)).getBase().getBaseGraph();
        fireHierarchyChanged();
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        setOntology(Objects.requireNonNull(ontologies).iterator().next());
    }

    protected Comparator<? super Triple> getComparator() {
        return createComparatorFor(graph);
    }

    public PrefixMapping getPrefixes() {
        return graph == null ? OntModelFactory.STANDARD : graph.getPrefixMapping();
    }

    public Graph getGraph() {
        return graph;
    }

    public String getBlankNodeLable(BlankNodeId id) {
        // todo: move mapper to model manager -> need share blank node labels between components
        return ids.computeIfAbsent(id, i -> "_:b" + ids.size());
    }

    @Override
    public final Set<Triple> getRoots() {
        if (graph == null) return Collections.emptySet();
        LOGGER.debug("Start collecting roots for {}", Graphs.getName(graph));
        Set<Triple> res = listRoots().toSet();
        LOGGER.debug("There are {} root triples in {}", graph.size(), Graphs.getName(graph));
        return res;
    }

    @Override
    public final Stream<Triple> roots() {
        // todo: not really used now
        if (graph == null) return Stream.empty();
        int characteristics = characteristics(graph);
        long size = -1;
        if (Graphs.isSized(graph)) {
            size = Graphs.size(graph);
            characteristics = characteristics | Spliterator.SIZED;
        }
        return Iter.asStream(listRoots(), size, characteristics);
    }

    protected ExtendedIterator<Triple> listRoots() {
        return graph.find().filterKeep(x -> GraphUtils.isRoot(graph, x.getSubject()));
    }

    @Override
    public final Set<Triple> getChildren(Triple triple) {
        if (graph == null) return Collections.emptySet();
        Node o = triple.getObject();
        if (!o.isBlank()) return Collections.emptySet();
        return listChildren(o).toSet();
    }

    @Override
    public final Stream<Triple> children(Triple triple) {
        if (graph == null) return Stream.empty();
        Node o = triple.getObject();
        if (!o.isBlank()) return Stream.empty();
        return Iter.asStream(listChildren(o), -1, characteristics(graph));
    }

    protected ExtendedIterator<Triple> listChildren(Node o) {
        return graph.find(o, Node.ANY, Node.ANY);
    }

    @Override
    public final Set<Triple> getParents(Triple triple) {
        if (graph == null) return Collections.emptySet();
        Node s = triple.getSubject();
        if (!s.isBlank()) return Collections.emptySet();
        return listParents(s).toSet();
    }

    @Override
    public final Stream<Triple> parents(Triple triple) {
        if (graph == null) return Stream.empty();
        Node s = triple.getSubject();
        if (!s.isBlank()) return Stream.empty();
        return Iter.asStream(listParents(s), -1, characteristics(graph));
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
        throw new IllegalStateException("TODO"); // TODO
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
        if (GraphUtils.isRoot(graph, s)) {
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
        ids.clear();
    }

    public Stream<HierarchyProviderListener<Triple>> listeners() {
        return listeners.stream();
    }

    protected void fireHierarchyChanged() {
        listeners().forEach(x -> {
            try {
                x.hierarchyChanged();
            } catch (Throwable e) {
                LOGGER.error("Hierarchy change exception: '{}'", e.getMessage(), e);
            }
        });
    }

}
