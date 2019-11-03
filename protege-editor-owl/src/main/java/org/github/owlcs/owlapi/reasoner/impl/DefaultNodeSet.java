package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asUnorderedSet;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @param <E> the type of owl objects in the node
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/DefaultNodeSet.java'>org.semanticweb.owlapi.reasoner.impl.DefaultNodeSet</a>
 */
@SuppressWarnings({"WeakerAccess", "NullableProblems", "unused"})
public abstract class DefaultNodeSet<E extends OWLObject> implements NodeSet<E> {

    private final Set<Node<E>> nodes = new LinkedHashSet<>();

    /**
     * constructor for an empty node set.
     */
    public DefaultNodeSet() {
    }

    /**
     * @param entity the entity to be contained
     */
    public DefaultNodeSet(E entity) {
        addNode(getNode(checkNotNull(entity, "entity cannot be null")));
    }

    /**
     * @param node the node to be contained
     */
    public DefaultNodeSet(Node<E> node) {
        addNode(checkNotNull(node, "node cannot be null"));
    }

    /**
     * @param nodes a set of nodes to be contained
     */
    public DefaultNodeSet(Set<Node<E>> nodes) {
        addAllNodes(checkNotNull(nodes, "nodes cannot be null"));
    }

    @Override
    public Stream<Node<E>> nodes() {
        return nodes.stream();
    }

    /**
     * Adds an entity to this {@code NodeSet} by wrapping it in a {@code Node}.
     *
     * @param entity The entity to be added. The entity will be wrapped in the {@code Node} and the
     *               {@code Node} added to this set. Must not be {@code null}.
     * @throws NullPointerException if {@code entity} is {@code null}.
     */
    public void addEntity(E entity) {
        addNode(getNode(checkNotNull(entity, "entity cannot be null")));
    }

    /**
     * Adds a {@code Node} to this set.
     *
     * @param node The {@code Node} to be added.
     * @throws NullPointerException if {@code entity} is {@code null}.
     */
    public void addNode(Node<E> node) {
        nodes.add(checkNotNull(node, "Cannot add null to a NodeSet"));
    }

    /**
     * Adds a collection of {@code Node}s to this set.
     *
     * @param nodeset The {@code Node}s to be added. Note that if the collection is not a set then
     *                duplicate {@code Node}s will be filtered out.
     */
    public void addAllNodes(Collection<Node<E>> nodeset) {
        nodeset.forEach(this::addNode);
    }

    /**
     * Adds the set of entities as a {@code Node} to this set.
     *
     * @param entities The set of entities to be added. The entities will be wrapped in a {@code
     *                 Node} which will be added to this {@code NodeSet}.
     */
    public void addSameEntities(Set<E> entities) {
        nodes.add(getNode(entities));
    }

    /**
     * Adds the specified entities as {@code Node}s to this set.
     *
     * @param entities The entities to be added. Each entity will be wrapped in a {@code Node} which
     *                 will then be added to this {@code NodeSet}.
     */
    public void addDifferentEntities(Set<E> entities) {
        entities.forEach(e -> addNode(getNode(e)));
    }

    protected abstract DefaultNode<E> getNode(E entity);

    protected abstract DefaultNode<E> getNode(Set<E> entities);

    @Override
    public Stream<E> entities() {
        return nodes().flatMap(Node::entities);
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public boolean containsEntity(E e) {
        for (Node<E> node : nodes) {
            if (node.contains(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSingleton() {
        return nodes.size() == 1;
    }

    @Override
    public boolean isTopSingleton() {
        return isSingleton() && nodes.iterator().next().isTopNode();
    }

    @Override
    public boolean isBottomSingleton() {
        return isSingleton() && nodes.iterator().next().isBottomNode();
    }

    @Override
    public Iterator<Node<E>> iterator() {
        return nodes.iterator();
    }

    @Override
    public String toString() {
        return "Nodeset" + nodes;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NodeSet)) {
            return false;
        }
        NodeSet<?> other = (NodeSet<?>) obj;
        return nodes.equals(asUnorderedSet(other.nodes()));
    }

    @Override
    public int hashCode() {
        return nodes.hashCode();
    }
}
