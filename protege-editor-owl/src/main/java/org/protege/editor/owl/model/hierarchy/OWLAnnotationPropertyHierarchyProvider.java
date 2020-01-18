package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Apr 23, 2009<br><br>
 */
public class OWLAnnotationPropertyHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLAnnotationProperty> {

    /*
     * The ontologies variable is protected by the ontologySetReadLock and the ontologySetWriteLock.
     * These locks are always taken and held inside of the getReadLock() and getWriteLock()'s for the
     * OWL Ontology Manager.  This is necessary because when the set of ontologies changes, everything
     * about this class changes.  So when the set of ontologies is changed we need to make sure that nothing
     * else is running.
     */
    private final Set<OWLOntology> ontologies;
    private final Set<OWLAnnotationProperty> roots;
    private final OWLOntologyChangeListener ontologyListener = this::handleChanges;

    private final ReadLock ontologySetReadLock;
    private final WriteLock ontologySetWriteLock;

    public OWLAnnotationPropertyHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
        this.roots = new HashSet<>();
        ontologies = new HashSet<>();
        ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
        ontologySetReadLock = locks.readLock();
        ontologySetWriteLock = locks.writeLock();
        owlOntologyManager.addOntologyChangeListener(ontologyListener);
    }

    @Override
    public Set<OWLAnnotationProperty> getRoots() {
        return Collections.unmodifiableSet(roots);
    }

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
    public boolean containsReference(OWLAnnotationProperty object) {
        ontologySetReadLock.lock();
        try {
            return ontologies.stream().anyMatch(x -> x.containsAnnotationPropertyInSignature(object.getIRI()));
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public Set<OWLAnnotationProperty> getUnfilteredChildren(OWLAnnotationProperty object) {
        ontologySetReadLock.lock();
        try {
            return ontologies.stream()
                    .flatMap(x -> x.axioms(AxiomType.SUB_ANNOTATION_PROPERTY_OF))
                    .filter(x -> object.equals(x.getSuperProperty()))
                    .map(OWLSubAnnotationPropertyOfAxiom::getSubProperty)
                    .filter(p -> !getAncestors(p).contains(p))
                    .collect(Collectors.toSet());
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public Set<OWLAnnotationProperty> getEquivalents(OWLAnnotationProperty object) {
        ontologySetReadLock.lock();
        try {
            Set<OWLAnnotationProperty> res = new HashSet<>();
            Set<OWLAnnotationProperty> ancestors = getAncestors(object);
            if (ancestors.contains(object)) {
                for (OWLAnnotationProperty anc : ancestors) {
                    if (!getAncestors(anc).contains(object)) {
                        continue;
                    }
                    res.add(anc);
                }
            }
            res.remove(object);
            return res;
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public Set<OWLAnnotationProperty> getParents(OWLAnnotationProperty object) {
        ontologySetReadLock.lock();
        try {
            return ontologies.stream()
                    .flatMap(x -> x.subAnnotationPropertyOfAxioms(object))
                    .map(OWLSubAnnotationPropertyOfAxiom::getSuperProperty)
                    .collect(Collectors.toSet());
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        getManager().removeOntologyChangeListener(ontologyListener);
    }

    /**
     * This call holds the write lock so no other thread can hold the either the OWL ontology
     * manager read or write locks or the ontologies
     *
     * @param changes {@code List}
     */
    private void handleChanges(List<? extends OWLOntologyChange> changes) {
        for (OWLAnnotationProperty prop : getPropertiesReferencedInChange(changes)) {
            if (isRoot(prop)) {
                roots.add(prop);
                fireNodeChanged(prop);
                continue;
            }
            Set<OWLAnnotationProperty> ancestors = getAncestors(prop);
            if (!ancestors.contains(prop)) {
                roots.remove(prop);
            } else {
                roots.add(prop);
                for (OWLAnnotationProperty anc : ancestors) {
                    if (!getAncestors(anc).contains(prop)) {
                        continue;
                    }
                    roots.add(anc);
                    fireNodeChanged(anc);
                }
            }
            fireNodeChanged(prop);
        }
    }

    @SuppressWarnings("NullableProblems")
    private Set<OWLAnnotationProperty> getPropertiesReferencedInChange(List<? extends OWLOntologyChange> changes) {
        Set<OWLAnnotationProperty> res = new HashSet<>();
        for (OWLOntologyChange chg : changes) {
            if (!chg.isAxiomChange()) {
                continue;
            }
            chg.getAxiom().accept(new OWLAxiomVisitor() {
                @Override
                public void visit(OWLSubAnnotationPropertyOfAxiom x) {
                    res.add(x.getSubProperty());
                    res.add(x.getSuperProperty());
                }

                @Override
                public void visit(OWLDeclarationAxiom x) {
                    if (x.getEntity().isOWLAnnotationProperty()) {
                        res.add(x.getEntity().asOWLAnnotationProperty());
                    }
                }
            });
        }
        return res;
    }

    private boolean isRoot(OWLAnnotationProperty prop) {

        // We deem a property to be a root property if it doesn't have
        // any super properties (i.e. it is not on
        // the LHS of a subproperty axiom
        // Assume the property is a root property to begin with
        if (!hasParents(prop) && (containsReference(prop) || prop.isBuiltIn())) {
            return true;
        }
        // Additional condition: If we have  P -> Q and Q -> P, then
        // there is no path to the root, so put P and Q as root properties
        // Collapse any cycles and force properties that are equivalent
        // through cycles to appear at the root.
        return getAncestors(prop).contains(prop);
    }

    private void rebuildRoots() {
        roots.clear();
        ontologies.stream()
                .flatMap(HasAnnotationPropertiesInSignature::annotationPropertiesInSignature)
                .collect(Collectors.toSet())
                .forEach(x -> {
                    if (isRoot(x)) {
                        roots.add(x);
                    }
                });
        OWLDataFactory df = getManager().getOWLDataFactory();
        for (IRI uri : OWLRDFVocabulary.BUILT_IN_AP_IRIS) {
            roots.add(df.getOWLAnnotationProperty(uri));
        }
    }
}
