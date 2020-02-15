package org.protege.editor.owl.model.selection.axioms;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.beans.PropertyChangeListener;
import java.util.Set;

/**
 * User: nickdrummond
 * Date: May 20, 2008
 */
@Deprecated // todo: unused -> delete
public interface AxiomSelectionStrategy {

    String getName();

    Set<? extends OWLAxiom> getAxioms(Set<OWLOntology> ontologies);

//    void setOntologies(Set<OWLOntology> ontologies);
//
//    Set<OWLOntology> getOntologies();

    void addPropertyChangeListener(PropertyChangeListener l);

    void removePropertyChangeListener(PropertyChangeListener l);    
}
