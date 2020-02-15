package org.protege.editor.owl.model.selection.axioms;

import org.semanticweb.owlapi.model.HasAxioms;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: nickdrummond
 * Date: May 21, 2008
 */
@Deprecated // todo: unused -> delete
public class AllAxiomsStrategy extends AbstractAxiomSelectionStrategy {

    @Override
    public String getName() {
        return "All axioms in the specified ontologies";
    }

    @Override
    public Set<OWLAxiom> getAxioms(Set<OWLOntology> ontologies) {
        return ontologies.stream().flatMap(HasAxioms::axioms).collect(Collectors.toSet());
    }
}
