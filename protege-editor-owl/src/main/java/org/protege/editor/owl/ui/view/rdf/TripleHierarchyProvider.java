package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.utils.Graphs;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProviderListener;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * An {@link OWLObjectHierarchyProvider} for {@link Graph}.
 * Created by @ssz on 23.11.2019.
 */
@SuppressWarnings("WeakerAccess")
public class TripleHierarchyProvider implements OWLObjectHierarchyProvider<Triple> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TripleHierarchyProvider.class);

    private Graph graph;

    private final List<OWLObjectHierarchyProviderListener<Triple>> listeners = new CopyOnWriteArrayList<>();
    private final Map<BlankNodeId, String> ids = new HashMap<>();

    @Override
    public void setOntology(OWLOntology o) {
        this.graph = ((Ontology) o).asGraphModel().getGraph(); // UnionGraph with all hierarchy
        fireHierarchyChanged();
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        setOntology(Objects.requireNonNull(ontologies).iterator().next());
    }

    public PrefixMapping getPrefixes() {
        return graph == null ? OntModelFactory.STANDARD : graph.getPrefixMapping();
    }

    public Graph getGraph() {
        return graph;
    }

    public String getBlankNodeLable(BlankNodeId id) {
        return ids.computeIfAbsent(id, i -> "_:b" + ids.size());
    }

    @Override
    public Set<Triple> getRoots() {
        if (graph == null) return Collections.emptySet();
        LOGGER.debug("Start collecting roots for {}", Graphs.getName(graph));
        Set<Triple> res = graph.find().filterKeep(triple -> GraphUtils.isRoot(graph, triple.getSubject())).toSet();
        LOGGER.debug("There are {} root triples in {}", graph.size(), Graphs.getName(graph));
        return res;
    }

    @Override
    public Set<Triple> getChildren(Triple triple) {
        if (graph == null) return Collections.emptySet();
        Node o = triple.getObject();
        if (!o.isBlank()) return Collections.emptySet();
        return graph.find(o, Node.ANY, Node.ANY).toSet();
    }

    @Override
    public Set<Triple> getParents(Triple triple) {
        if (graph == null) return Collections.emptySet();
        Node s = triple.getSubject();
        if (!s.isBlank()) return Collections.emptySet();
        return graph.find(Node.ANY, Node.ANY, s).toSet();
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
    public void addListener(OWLObjectHierarchyProviderListener<Triple> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(OWLObjectHierarchyProviderListener<Triple> listener) {
        listeners.remove(listener);
    }

    @Override
    public void dispose() {
        listeners.clear();
        ids.clear();
    }

    public Stream<OWLObjectHierarchyProviderListener<Triple>> listeners() {
        return listeners.stream();
    }

    protected void fireHierarchyChanged() {
        listeners().forEach(listener -> {
            try {
                listener.hierarchyChanged();
            } catch (Throwable e) {
                LOGGER.error("Hierarchy change exception: '{}'", e.getMessage(), e);
            }
        });
    }

}
