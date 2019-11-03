package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.Node;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;
import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;
import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asUnorderedSet;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @param <E> the type of entities in the node
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/DefaultNode.java'>org.semanticweb.owlapi.reasoner.impl.DefaultNode</a>
 */
@SuppressWarnings({"NullableProblems"})
public abstract class DefaultNode<E extends OWLObject> implements Node<E> {

    private final Set<E> entities = new HashSet<>(4);

    /**
     * @param entity the entity to add
     */
    public DefaultNode(E entity) {
        entities.add(checkNotNull(entity, "entity cannot be null"));
    }

    /**
     * @param entities the entities to add
     */
    public DefaultNode(Collection<E> entities) {
        this.entities.addAll(checkNotNull(entities, "entities cannot be null"));
    }

    protected DefaultNode() {
    }

    protected abstract Optional<E> getTopEntity();

    protected abstract Optional<E> getBottomEntity();

    /**
     * @param entity entity to be added
     */
    public void add(E entity) {
        entities.add(entity);
    }

    @Override
    public boolean isTopNode() {
        if (!getTopEntity().isPresent()) {
            return false;
        }
        return entities.contains(getTopEntity().get());
    }

    @Override
    public boolean isBottomNode() {
        if (!getBottomEntity().isPresent()) {
            return false;
        }
        return entities.contains(getBottomEntity().get());
    }

    @Override
    public Stream<E> entities() {
        return entities.stream();
    }

    @Override
    public int getSize() {
        return entities.size();
    }

    @Override
    public boolean contains(E entity) {
        return entities.contains(entity);
    }

    @Override
    public Set<E> getEntitiesMinus(E e) {
        return asUnorderedSet(entities.stream().filter(i -> !i.equals(e)));
    }

    @Override
    public Set<E> getEntitiesMinusTop() {
        Optional<E> topEntity = getTopEntity();
        if (topEntity.isPresent()) {
            return getEntitiesMinus(topEntity.get());
        }
        return asUnorderedSet(entities.stream());
    }

    @Override
    public Set<E> getEntitiesMinusBottom() {
        Optional<E> bottomEntity = getBottomEntity();
        if (bottomEntity.isPresent()) {
            return getEntitiesMinus(bottomEntity.get());
        }
        return asUnorderedSet(entities.stream());
    }

    @Override
    public boolean isSingleton() {
        return entities.size() == 1;
    }

    @Override
    public E getRepresentativeElement() {
        return entities.iterator().next();
    }

    @Override
    public Iterator<E> iterator() {
        return entities.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node( ");
        for (OWLObject entity : entities) {
            sb.append(entity);
            sb.append(' ');
        }
        sb.append(')');
        return verifyNotNull(sb.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Node)) {
            return false;
        }
        Node<E> other = (Node<E>) obj;
        return entities.equals(asUnorderedSet(other.entities()));
    }

    @Override
    public int hashCode() {
        return entities.hashCode();
    }
}

