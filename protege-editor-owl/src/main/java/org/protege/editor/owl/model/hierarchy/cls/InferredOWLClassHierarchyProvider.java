package org.protege.editor.owl.model.hierarchy.cls;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.hierarchy.AbstractOWLObjectHierarchyProvider;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 06-Jun-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class InferredOWLClassHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLClass> {

    /*
     * There is no local state in this class - all the state is held in the reasoner and the ontologies on which
     * this works.  But there is one race condition that I don't know how to track here.  The reasoner can be changed
     * underneath this provider while it is running.  A listener doesn't really help because the reasoner can be changed
     * at any time.  But I can hope that the new reasoner will run the same way the old one did.
     */
    private final OWLModelManager owlModelManager;
    private final OWLClass owlThing;
    private final OWLClass owlNothing;

    private final OWLModelManagerListener owlModelManagerListener = e -> {
        if (e.isOneOf(EventType.REASONER_CHANGED
                , EventType.ACTIVE_ONTOLOGY_CHANGED
                , EventType.ONTOLOGY_CLASSIFIED
                , EventType.ONTOLOGY_RELOADED)) {
            fireHierarchyChanged();
        }
    };

    public InferredOWLClassHierarchyProvider(OWLModelManager owlModelManager, OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
        this.owlModelManager = owlModelManager;

        owlThing = owlModelManager.getOWLDataFactory().getOWLThing();
        owlNothing = owlModelManager.getOWLDataFactory().getOWLNothing();

        owlModelManager.addListener(owlModelManagerListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        owlModelManager.removeListener(owlModelManagerListener);
    }

    @Override
    public Set<OWLClass> getRoots() {
        return Collections.singleton(owlThing);
    }

    protected OWLReasoner getReasoner() {
        return owlModelManager.getOWLReasonerManager().getCurrentReasoner();
    }

    @Override
    public Set<OWLClass> getUnfilteredChildren(OWLClass object) {
        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return Collections.emptySet();
        }
        Set<OWLClass> res = reasoner.getSubClasses(object, true).entities().collect(Collectors.toSet());
        // Add in owl:Nothing if there are inconsistent classes
        if (object.isOWLThing() && !owlModelManager.getReasoner().getUnsatisfiableClasses().isSingleton()) {
            res.add(owlNothing);
        } else if (object.isOWLNothing()) {
            res.addAll(reasoner.getUnsatisfiableClasses().getEntitiesMinus(owlNothing));
        } else {
            // Class which is not Thing or Nothing
            res.remove(owlNothing);
            res.removeIf(x -> !reasoner.isSatisfiable(x));
        }
        return res;
    }

    @Override
    public Set<OWLClass> getParents(OWLClass object) {
        if (!getReasoner().isConsistent()) {
            return Collections.emptySet();
        }
        if (object.isOWLNothing()) {
            return Collections.singleton(owlThing);
        } else if (!getReasoner().isSatisfiable(object)) {
            return Collections.singleton(owlNothing);
        }
        Set<OWLClass> res = getReasoner().getSuperClasses(object, true).entities().collect(Collectors.toSet());
        res.remove(object);
        return res;
    }

    @Override
    public Set<OWLClass> getDescendants(OWLClass object) {
        if (!getReasoner().isConsistent()) {
            return Collections.emptySet();
        }
        return getReasoner().getSubClasses(object, false).entities().collect(Collectors.toSet());
    }

    @Override
    public Set<OWLClass> getAncestors(OWLClass object) {
        if (!getReasoner().isConsistent()) {
            return Collections.emptySet();
        }
        return getReasoner().getSuperClasses(object, false).entities().collect(Collectors.toSet());
    }

    @Override
    public Set<OWLClass> getEquivalents(OWLClass object) {
        if (!getReasoner().isConsistent()) {
            return Collections.emptySet();
        }
        if (!getReasoner().isSatisfiable(object)) {
            return Collections.emptySet();
        }
        return getReasoner().getEquivalentClasses(object).getEntitiesMinus(object);
    }

    @Override
    public boolean containsReference(OWLClass object) {
        return false;
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
    }
}
