package org.protege.editor.owl.model.hierarchy;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 14-Sep-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class AssertedSuperClassHierarchyProvider extends AbstractSuperClassHierarchyProvider {
    private final OWLModelManager owlModelManager;

    public AssertedSuperClassHierarchyProvider(OWLModelManager owlModelManager) {
        super(owlModelManager.getOWLOntologyManager());
        this.owlModelManager = owlModelManager;
    }

    @Override
    public Set<OWLClass> getUnfilteredChildren(OWLClass object) {
        //return owlModelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider().getParents(object);
        throw new UnsupportedOperationException();
    }

    @Override
    protected Stream<OWLClass> unfilteredChildren(OWLClass object) {
        return owlModelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider().parents(object);
    }

    @Override
    public Set<OWLClass> getEquivalents(OWLClass object) {
        return Collections.emptySet();
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
    }

    @Override
    public Set<OWLClass> getParents(OWLClass object) {
        return owlModelManager.getOWLHierarchyManager().getOWLClassHierarchyProvider().getChildren(object);
    }

    @Override
    public boolean containsReference(OWLClass object) {
        return false;
    }
}
