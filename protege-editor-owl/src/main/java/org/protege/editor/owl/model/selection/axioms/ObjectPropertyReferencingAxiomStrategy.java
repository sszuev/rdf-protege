package org.protege.editor.owl.model.selection.axioms;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Set;

/**
 * User: nickdrummond
 * Date: May 20, 2008
 */
@Deprecated // todo: unused -> delete
public class ObjectPropertyReferencingAxiomStrategy extends EntityReferencingAxiomsStrategy<OWLObjectProperty> {

    public String getName() {
        return "Axioms referencing a given object property (or properties)";
    }

    public Set<OWLAxiom> getAxioms(Set<OWLOntology> ontologies) {
        Set<OWLAxiom> axioms = new HashSet<>();
        for (OWLObjectProperty p : getEntities()){
            for (OWLOntology ont : ontologies){
                axioms.addAll(ont.getReferencingAxioms(p));
            }
        }
        return axioms;
    }

    public Class<OWLObjectProperty> getType() {
        return OWLObjectProperty.class;
    }
}
