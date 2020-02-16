package org.protege.editor.owl.ui.action.export.inferred;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredClassAxiomGenerator;

import java.util.Set;

@SuppressWarnings("NullableProblems")
public class InferredDisjointClassesAxiomGenerator extends InferredClassAxiomGenerator<OWLDisjointClassesAxiom> {

    @Override
    protected void addAxioms(OWLClass entity,
                             OWLReasoner reasoner,
                             OWLDataFactory dataFactory,
                             Set<OWLDisjointClassesAxiom> result) {
        reasoner.getDisjointClasses(entity).entities()
                .map(e -> dataFactory.getOWLDisjointClassesAxiom(entity, e))
                .forEach(result::add);
    }

    @Override
    public String getLabel() {
        return "Disjoint classes";
    }
}
