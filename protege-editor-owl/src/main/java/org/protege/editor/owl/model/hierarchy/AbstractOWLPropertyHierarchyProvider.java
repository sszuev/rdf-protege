package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Jan-2007<br><br>
 */
public abstract class AbstractOWLPropertyHierarchyProvider<E extends OWLPropertyExpression, P extends E>
        extends AbstractOWLObjectHierarchyProvider<P> {

//    private static final Logger logger = LoggerFactory.getLogger(AbstractOWLPropertyHierarchyProvider.class);

    private ReadLock ontologySetReadLock;
    private WriteLock ontologySetWriteLock;

    /*
     * The ontologies variable is protected by the ontologySetReadLock and the ontologySetWriteLock.
     * These locks are always taken and held inside of the getReadLock() and getWriteLock()'s for the
     * OWL Ontology Manager.  This is necessary because when the set of ontologies changes, everything
     * about this class changes.  So when the set of ontologies is changed we need to make sure that nothing
     * else is running.
     */
    private final Set<OWLOntology> ontologies;
    private final Set<P> subPropertiesOfRoot;
    private final OWLOntologyChangeListener listener;

    public AbstractOWLPropertyHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
        this.subPropertiesOfRoot = new HashSet<>();
        ontologies = new FakeSet<>();
        ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
        ontologySetReadLock = locks.readLock();
        ontologySetWriteLock = locks.writeLock();
        listener = this::handleChanges;
        owlOntologyManager.addOntologyChangeListener(listener);
    }

    @Override
    public void dispose() {
        super.dispose();
        getManager().removeOntologyChangeListener(listener);
    }

    /*
     * This call holds the write lock so no other thread can hold the either the OWL ontology
     * manager read or write locks or the ontologies
     */
    private void handleChanges(List<? extends OWLOntologyChange> changes) {
        Set<P> properties = propertiesReferencedInChange(changes).collect(Collectors.toSet());
        for (P prop : properties) {
            if (isSubPropertyOfRoot(prop)) {
                subPropertiesOfRoot.add(prop);
                fireNodeChanged(getRoot());
            } else {
                if (getAncestors(prop).contains(prop)) {
                    subPropertiesOfRoot.add(prop);
                    getAncestors(prop).stream()
                            .filter(anc -> getAncestors(anc).contains(prop))
                            .forEach(anc -> {
                                subPropertiesOfRoot.add(anc);
                                fireNodeChanged(anc);
                            });
                } else {
                    subPropertiesOfRoot.remove(prop);
                }
            }
            fireNodeChanged(prop);
        }
        fireNodeChanged(getRoot());
    }

    protected abstract Stream<P> propertiesReferencedInChange(List<? extends OWLOntologyChange> changes);

    private boolean isSubPropertyOfRoot(P prop) {
        if (prop.equals(getRoot())) {
            return false;
        }

        // We deem a property to be a sub of the top property if this is asserted
        // or if no named superproperties are asserted
        Set<P> parents = getParents(prop);
        if (parents.isEmpty() || parents.contains(getRoot())) {
            for (OWLOntology ont : ontologies) {
                if (containsReference(ont, prop)) {
                    return true;
                }
            }
        }
        // Additional condition: If we have  P -> Q and Q -> P, then
        // there is no path to the root, so put P and Q as root properties
        // Collapse any cycles and force properties that are equivalent
        // through cycles to appear at the root.
        return getAncestors(prop).contains(prop);
    }

    private void rebuildRoots() {
        subPropertiesOfRoot.clear();
        ontologies.stream()
                .flatMap(this::referencedProperties)
                .filter(this::isSubPropertyOfRoot)
                .forEach(subPropertiesOfRoot::add);
    }

    protected abstract boolean containsReference(OWLOntology ont, P prop);

    /**
     * Gets the relevant properties in the specified ontology that are contained within the property hierarchy.
     * For example, for an object property hierarchy
     * this would constitute the set of referenced object properties in the specified ontology.
     *
     * @param ont The ontology
     * @return {@code Stream}
     */
    protected abstract Stream<? extends P> referencedProperties(OWLOntology ont);

    protected abstract P getRoot();

    /**
     * Gets the objects that represent the roots of the hierarchy.
     */
    @Override
    public Set<P> getRoots() {
        return Collections.singleton(getRoot());
    }

    /**
     * Sets the ontologies that this hierarchy provider should use in order to determine the hierarchy.
     *
     * @param ontologies {@code Set}
     */
    @Override
    public final void setOntologies(Set<OWLOntology> ontologies) {
        ontologySetWriteLock.lock();
        try {
            this.ontologies.clear();
            this.ontologies.addAll(ontologies);
            rebuildRoots();
            fireHierarchyChanged();
        } finally {
            ontologySetWriteLock.unlock();
        }
    }

    @Override
    public boolean containsReference(P object) {
        ontologySetReadLock.lock();
        try {
            for (OWLOntology ont : ontologies) {
                if (containsReference(ont, object)) {
                    return true;
                }
            }
            return false;
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public Set<P> getUnfilteredChildren(P object) {
        ontologySetReadLock.lock();
        try {
            if (object.equals(getRoot())) {
                return Collections.unmodifiableSet(subPropertiesOfRoot);
            }
            return subProperties(object, ontologies)
                    // Don't add the sub property if it is a parent of itself - i.e. prevent cycles
                    .filter(p -> !p.isAnonymous() && !getAncestors(p).contains(p))
                    .collect(Collectors.toSet());
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    protected abstract Stream<P> superProperties(P subProperty, Set<OWLOntology> ontologies);

    protected abstract Stream<P> subProperties(P superProperty, Set<OWLOntology> ontologies);

    @SuppressWarnings("unchecked")
    @Override
    public Set<P> getEquivalents(P prop) {
        ontologySetReadLock.lock();
        try {
            Set<P> res = new HashSet<>();
            Set<P> ancestors = getAncestors(prop);
            if (ancestors.contains(prop)) {
                for (P anc : ancestors) {
                    if (getAncestors(anc).contains(prop)) {
                        res.add(anc);
                    }
                }
            }
            if (prop instanceof OWLDataProperty) {
                EntitySearcher.getEquivalentProperties((OWLDataProperty) prop, ontologies.stream())
                        .filter(x -> !x.isAnonymous())
                        .forEach(x -> res.add((P) x));
            }
            if (prop instanceof OWLObjectPropertyExpression) {
                EntitySearcher.getEquivalentProperties((OWLObjectPropertyExpression) prop, ontologies.stream())
                        .filter(x -> !x.isAnonymous())
                        .forEach(x -> res.add((P) x));
            }
            res.remove(prop);
            return res;
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public Set<P> getParents(P object) {
        ontologySetReadLock.lock();
        try {
            if (object.equals(getRoot())) {
                return Collections.emptySet();
            }
            Set<P> res = superProperties(object, ontologies)
                    .filter(p -> !p.isAnonymous())
                    .collect(Collectors.toSet());
            if (res.isEmpty() && isReferenced(object)) {
                res.add(getRoot());
            }
            return res;
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    private boolean isReferenced(P e) {
        return e.accept(new IsReferencePropertyExpressionVisitor());
    }

    @SuppressWarnings("NullableProblems")
    private class IsReferencePropertyExpressionVisitor implements OWLPropertyExpressionVisitorEx<Boolean> {

        @Override
        public Boolean visit(OWLAnnotationProperty owlAnnotationProperty) {
            return isReferenced(owlAnnotationProperty);
        }

        @Override
        public Boolean visit(OWLObjectProperty property) {
            return isReferenced(property);
        }

        @Override
        public Boolean visit(OWLObjectInverseOf property) {
            return property.getInverse().accept(this);
        }

        @Override
        public Boolean visit(OWLDataProperty property) {
            return isReferenced(property);
        }

        private boolean isReferenced(OWLEntity e) {
            for (OWLOntology ontology : ontologies) {
                if (ontology.containsEntityInSignature(e)) {
                    return true;
                }
            }
            return false;
        }
    }

    @SuppressWarnings("NullableProblems")
    private static class FakeSet<X> extends AbstractSet<X> {
        private final List<X> elements = new ArrayList<>();

        @Override
        public Iterator<X> iterator() {
            return elements.iterator();
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public boolean add(X e) {
            if (elements.contains(e)) {
                return false;
            }
            elements.add(e);
            return true;
        }

        @Override
        public void clear() {
            elements.clear();
        }
    }
}
