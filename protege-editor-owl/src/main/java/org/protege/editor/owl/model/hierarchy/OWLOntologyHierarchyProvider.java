package org.protege.editor.owl.model.hierarchy;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.*;

/*
 * Copyright (C) 2007, University of Manchester
 *
 *
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 28-Oct-2007<br><br>
 */
public class OWLOntologyHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLOntology> {

    /*
     * The internal state of this class is synchronized by the roots object.
     */
    private final Set<OWLOntology> roots;
    private final Map<OWLOntology, Set<OWLOntology>> parent2ChildMap;
    private final Map<OWLOntology, Set<OWLOntology>> child2ParentMap;
    private final OWLModelManager mngr;

    private final OWLModelManagerListener modelManagerListener = event -> {
        if (event.isOneOf(EventType.ONTOLOGY_LOADED, EventType.ONTOLOGY_RELOADED, EventType.ONTOLOGY_CREATED)) {
            rebuild();
        }
    };

    public OWLOntologyHierarchyProvider(OWLModelManager mngr) {
        super(mngr.getOWLOntologyManager());
        this.mngr = mngr;
        roots = new HashSet<>();
        parent2ChildMap = new HashMap<>();
        child2ParentMap = new HashMap<>();
        rebuild();
        mngr.addListener(modelManagerListener);
    }

    private void rebuild() {
        synchronized (roots) {
            roots.clear();
            parent2ChildMap.clear();
            child2ParentMap.clear();
            OWLOntologyManager m = mngr.getOWLOntologyManager();
            for (OWLOntology ont : mngr.getOntologies()) {
                m.imports(ont).forEach(x -> add(ont, x));
            }
            for (OWLOntology ont : mngr.getOntologies()) {
                if (!child2ParentMap.containsKey(ont)) {
                    roots.add(ont);
                }
            }
        }
        fireHierarchyChanged();
    }

    /*
     * only called inside of rebuild so the roots lock is taken.
     */
    private void add(OWLOntology ont, OWLOntology imp) {
        getChildrenForParent(ont).add(imp);
        getParentsForChild(imp).add(ont);
    }

    private Set<OWLOntology> getChildrenForParent(OWLOntology parent) {
        synchronized (roots) {
            return parent2ChildMap.computeIfAbsent(parent, k -> new HashSet<>());
        }
    }

    private Set<OWLOntology> getParentsForChild(OWLOntology child) {
        synchronized (roots) {
            return child2ParentMap.computeIfAbsent(child, k -> new HashSet<>());
        }
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
    }

    @Override
    public Set<OWLOntology> getRoots() {
        synchronized (roots) {
            return Collections.unmodifiableSet(roots);
        }
    }

    @Override
    public Set<OWLOntology> getParents(OWLOntology object) {
        return getParentsForChild(object);
    }

    @Override
    public Set<OWLOntology> getEquivalents(OWLOntology object) {
        return Collections.emptySet();
    }

    @Override
    public Set<OWLOntology> getUnfilteredChildren(OWLOntology object) {
        return getChildrenForParent(object);
    }

    @Override
    public boolean containsReference(OWLOntology object) {
    	synchronized (roots) {
            return parent2ChildMap.containsKey(object) || roots.contains(object);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        mngr.removeListener(modelManagerListener);
    }
}
