package org.protege.editor.owl.model.hierarchy;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Describes a hierarchy.
 * Created by @ssz on 16.01.2020.
 *
 * @param <N> - any node
 */
public interface HierarchyProvider<N> {

    /**
     * Gets the objects that represent the roots of the hierarchy.
     *
     * @return a {@code Set} of root {@link N node}s
     */
    Set<N> getRoots();

    Set<N> getChildren(N object);

    Set<N> getParents(N object);

    Set<N> getDescendants(N object);

    Set<N> getAncestors(N object);

    Set<N> getEquivalents(N object);

    boolean containsReference(N object);

    Set<List<N>> getPathsToRoot(N object);

    /**
     * @param node {@link N}
     * @return {@code true} if the specified {@link N node} is root in this hierarchy
     * @see #getRoots()
     */
    default boolean hasRoot(N node) {
        return getRoots().contains(node);
    }

    /**
     * @return <b>distinct</b> {@code Stream} of root {@link N node}s
     * @see #getRoots()
     */
    default Stream<N> roots() {
        return getRoots().stream();
    }

    /**
     * @param parent {@link N} - parent
     * @return <b>distinct</b> {@code Stream} of root {@link N node}s
     * @see #getChildren(Object)
     */
    default Stream<N> children(N parent) {
        return getChildren(parent).stream();
    }

    /**
     * @param parent {@link N}
     * @return {@code true} if the specified {@link N node} is parent for some children
     */
    default boolean hasChildren(N parent) {
        return !getChildren(parent).isEmpty();
    }

    /**
     * @param child {@link N}
     * @return <b>distinct</b> {@code Stream} of parent {@link N node}s
     * @see #getParents(Object)
     */
    default Stream<N> parents(N child) {
        return getParents(child).stream();
    }

    /**
     * @param child {@link N}
     * @return {@code true} if the specified {@link N node} has some parent
     */
    default boolean hasParents(N child) {
        return !getParents(child).isEmpty();
    }

    /**
     * @param node {@link N}
     * @return <b>distinct</b> {@code Stream} of descendant {@link N node}s
     * @see #getDescendants(Object)
     */
    default Stream<N> descendants(N node) {
        return getDescendants(node).stream();
    }

    /**
     * @param node {@link N}
     * @return {@code true} if the specified {@link N node} has descendants
     */
    default boolean hasDescendants(N node) {
        return !getDescendants(node).isEmpty();
    }

    /**
     * @param node {@link N}
     * @return <b>distinct</b> {@code Stream} of ancestors {@link N node}s
     * @see #getAncestors(Object)
     */
    default Stream<N> ancestors(N node) {
        return getAncestors(node).stream();
    }

    /**
     * @param node {@link N}
     * @return <b>distinct</b> {@code Stream} of equivalent {@link N node}s
     * @see #getEquivalents(Object)
     */
    default Stream<N> equivalents(N node) {
        return getEquivalents(node).stream();
    }
}
