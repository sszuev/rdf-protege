package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Jun-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public interface OWLOntologyObjectHierarchyProvider<N extends OWLObject> extends HierarchyProvider<N> {

    void setOntologies(Set<OWLOntology> ontologies);


    void handleOntologyChanges(List<? extends OWLOntologyChange> changes);
}
