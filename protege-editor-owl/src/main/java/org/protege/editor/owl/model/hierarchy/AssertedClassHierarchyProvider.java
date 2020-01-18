package org.protege.editor.owl.model.hierarchy;

import org.protege.owlapi.inference.cls.ChildClassExtractor;
import org.protege.owlapi.inference.cls.ParentClassExtractor;
import org.protege.owlapi.inference.orphan.TerminalElementFinder;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 17-Jan-2007<br><br>
 */
public class AssertedClassHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLClass> {

    /*
     * The ontologies variable is protected by the ontologySetReadLock and the ontologySetWriteLock.
     * These locks are always taken and held inside of the getReadLock() and getWriteLock()'s for the
     * OWL Ontology Manager.  This is necessary because when the set of ontologies changes, everything
     * about this class changes.  So when the set of ontologies is changed we need to make sure that nothing
     * else is running.
     */
    /*
     * It is not safe to set the collection of ontologies to a HashSet or TreeSet.
     * When an ontology changes name it gets a new Hash Code and it is sorted
     * differently, so these Collections do not work.
     */
    private Collection<OWLOntology> ontologies;
    private volatile OWLClass root;

    private final OWLOntologyManager owlOntologyManager;
    private final ReadLock ontologySetReadLock;
    private final WriteLock ontologySetWriteLock;
    private final ParentClassExtractor parentClassExtractor;
    private final ChildClassExtractor childClassExtractor;
    private final OWLOntologyChangeListener listener;
    private final TerminalElementFinder<OWLClass> rootFinder;
    private final Set<OWLClass> nodesToUpdate = new HashSet<>();

    public AssertedClassHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
        this.owlOntologyManager = owlOntologyManager;
        /*
         * It is not safe to set the collection of ontologies to a HashSet or TreeSet.
         * When an ontology changes name it gets a new Hash Code and it is sorted
         * differently, so these Collections do not work.
         */
        ontologies = new ArrayList<>();
        ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
        ontologySetReadLock = locks.readLock();
        ontologySetWriteLock = locks.writeLock();
        rootFinder = new TerminalElementFinder<>(cls -> {
            Collection<OWLClass> parents = getParents(cls);
            parents.remove(root);
            return parents;
        });

        parentClassExtractor = new ParentClassExtractor();
        childClassExtractor = new ChildClassExtractor();
        listener = this::handleChanges;
        getManager().addOntologyChangeListener(listener);
    }

    /**
     * Sets the ontologies that this hierarchy provider should use in order to determine the hierarchy.
     */
    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        ontologySetWriteLock.lock();
        try {
            /*
             * It is not safe to set the collection of ontologies to a HashSet or TreeSet.
             * When an ontology changes name it gets a new Hash Code and it is sorted
             * differently, so these Collections do not work.
             */
            this.ontologies = new ArrayList<>(ontologies);
            nodesToUpdate.clear();
            if (root == null) {
                root = owlOntologyManager.getOWLDataFactory().getOWLThing();
            }
            rebuildImplicitRoots();
            fireHierarchyChanged();
        } finally {
            ontologySetWriteLock.unlock();
        }
    }

    private void rebuildImplicitRoots() {
        ontologySetReadLock.lock();
        try {
            rootFinder.clear();
            for (OWLOntology ont : ontologies) {
                Set<OWLClass> ref = ont.classesInSignature().collect(Collectors.toSet());
                rootFinder.appendTerminalElements(ref);
            }
            rootFinder.finish();
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public void dispose() {
        getManager().removeOntologyChangeListener(listener);
    }

    /*
     * This call holds the write lock so no other thread can hold the either the OWL ontology
     * manager read or write locks or the ontologies
     */
    private void handleChanges(List<? extends OWLOntologyChange> changes) {
        Set<OWLClass> oldTerminalElements = new HashSet<>(rootFinder.getTerminalElements());
        Set<OWLClass> changedClasses = new HashSet<>();
        changedClasses.add(root);
        List<OWLAxiomChange> filteredChanges = filterIrrelevantChanges(changes);
        updateImplicitRoots(filteredChanges);
        for (OWLOntologyChange change : filteredChanges) {
            change.signature()
                    .filter(x -> x instanceof OWLClass && !x.equals(root))
                    .map(AsOWLClass::asOWLClass)
                    .forEach(changedClasses::add);
        }
        for (OWLClass cls : changedClasses) {
            registerNodeChanged(cls);
        }
        for (OWLClass cls : rootFinder.getTerminalElements()) {
            if (!oldTerminalElements.contains(cls)) {
                registerNodeChanged(cls);
            }
        }
        for (OWLClass cls : oldTerminalElements) {
            if (!rootFinder.getTerminalElements().contains(cls)) {
                registerNodeChanged(cls);
            }
        }
        notifyNodeChanges();
    }

    private List<OWLAxiomChange> filterIrrelevantChanges(List<? extends OWLOntologyChange> changes) {
        List<OWLAxiomChange> res = new ArrayList<>();
        for (OWLOntologyChange change : changes) {
            // only listen for changes on the appropriate ontologies
            if (!ontologies.contains(change.getOntology())) {
                continue;
            }
            if (change.isAxiomChange()) {
                res.add((OWLAxiomChange) change);
            }
        }
        return res;
    }

    private void registerNodeChanged(OWLClass node) {
        nodesToUpdate.add(node);
    }

    private void notifyNodeChanges() {
        for (OWLClass node : nodesToUpdate) {
            fireNodeChanged(node);
        }
        nodesToUpdate.clear();
    }

    private void updateImplicitRoots(List<OWLAxiomChange> changes) {
        Set<OWLClass> possibleTerminalElements = new HashSet<>();
        Set<OWLClass> notInOntologies = new HashSet<>();

        for (OWLOntologyChange change : changes) {
            // only listen for changes on the appropriate ontologies
            if (!ontologies.contains(change.getOntology())) {
                continue;
            }
            if (!change.isAxiomChange()) {
                continue;
            }
            boolean remove = change instanceof RemoveAxiom;
            OWLAxiom axiom = change.getAxiom();
            axiom.signature().forEach(entity -> {
                if (!(entity instanceof OWLClass) || entity.equals(root)) {
                    return;
                }
                OWLClass cls = (OWLClass) entity;
                if (remove && !containsReference(cls)) {
                    notInOntologies.add(cls);
                    return;
                }
                possibleTerminalElements.add(cls);
            });
        }

        possibleTerminalElements.addAll(rootFinder.getTerminalElements());
        possibleTerminalElements.removeAll(notInOntologies);
        rootFinder.findTerminalElements(possibleTerminalElements);
    }

    @Override
    public Set<OWLClass> getRoots() {
        if (root == null) {
            root = owlOntologyManager.getOWLDataFactory().getOWLThing();
        }
        return Collections.singleton(root);
    }

    @Override
    protected Set<OWLClass> getUnfilteredChildren(OWLClass object) {
        ontologySetReadLock.lock();
        try {
            if (object.equals(root)) {
                Set<OWLClass> res = new HashSet<>();
                res.addAll(rootFinder.getTerminalElements());
                res.addAll(extractChildren(object));
                res.remove(object);
                return res;
            } else {
                Set<OWLClass> res = extractChildren(object);
                res.removeIf(x -> getAncestors(object).contains(x));
                return res;
            }
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    private Set<OWLClass> extractChildren(OWLClass parent) {
        childClassExtractor.setCurrentParentClass(parent);
        for (OWLOntology ont : ontologies) {
            ont.referencingAxioms(parent)
                    .filter(OWLAxiom::isLogicalAxiom)
                    .forEach(x -> x.accept(childClassExtractor));
        }
        return childClassExtractor.getResult();
    }

    @Override
    public boolean containsReference(OWLClass object) {
        ontologySetReadLock.lock();
        try {
            for (OWLOntology ont : ontologies) {
                if (ont.containsClassInSignature(object.getIRI())) {
                    return true;
                }
            }
            return false;
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public Set<OWLClass> getParents(OWLClass object) {
        ontologySetReadLock.lock();
        try {
            // If the object is thing then there are no
            // parents
            if (object.equals(root)) {
                return Collections.emptySet();
            }
            Set<OWLClass> result = new HashSet<>();
            // Thing if the object is a root class
            if (rootFinder.getTerminalElements().contains(object)) {
                result.add(root);
            }
            // Not a root, so must have another parent
            parentClassExtractor.reset();
            parentClassExtractor.setCurrentClass(object);
            for (OWLOntology ont : ontologies) {
                ont.axioms(object, Imports.EXCLUDED).forEach(x -> x.accept(parentClassExtractor));
            }
            result.addAll(parentClassExtractor.getResult());
            return result;
        } finally {
            ontologySetReadLock.unlock();
        }
    }

    @Override
    public Set<OWLClass> getEquivalents(OWLClass object) {
        ontologySetReadLock.lock();
        try {
            Set<OWLClass> res = new HashSet<>();
            for (OWLOntology ont : ontologies) {
                EntitySearcher.getEquivalentClasses(object, ont)
                        .filter(x -> !x.isAnonymous())
                        .forEach(x -> res.add(x.asOWLClass()));
            }
            Set<OWLClass> ancestors = getAncestors(object);
            if (!ancestors.contains(object)) {
                return res;
            }
            for (OWLClass cls : ancestors) {
                if (getAncestors(cls).contains(object)) {
                    res.add(cls);
                }
            }
            res.remove(object);
            res.remove(root);
            return res;
        } finally {
            ontologySetReadLock.unlock();
        }
    }

}
