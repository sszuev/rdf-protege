package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLOntology;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Describes hierarchy.
 * Created by @ssz on 16.01.2020.
 *
 * @param <N> - any node
 */
public interface HierarchyProvider<N> {

    /**
     * Gets the objects that represent the roots of the hierarchy.
     *
     * @return a {@code Set} of {@link OWLOntology ontologies}
     */
    Set<N> getRoots();

    Set<N> getChildren(N object);

    Set<N> getDescendants(N object);

    Set<N> getParents(N object);

    Set<N> getAncestors(N object);

    Set<N> getEquivalents(N object);

    Set<List<N>> getPathsToRoot(N object);

    boolean containsReference(N object);

    /**
     * @param node {@link N}
     * @return {@code true} if the specified {@link N node} is root in this hierarchy
     * @see #getRoots()
     */
    default boolean hasRoot(N node) {
        return getRoots().contains(node);
    }

    /**
     * @return long
     * @see #getRoots()
     */
    default long getRootsCount() {
        return getRoots().size();
    }

    /**
     * @return <b>distinct</b> {@code Stream} of root {@link N node}s
     * @see #getRoots()
     */
    default Stream<N> roots() {
        return getRoots().stream();
    }

}
