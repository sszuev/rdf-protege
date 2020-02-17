package org.protege.owlapi.inference.cls;

import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Set;


public class ParentClassExtractor implements OWLAxiomVisitor {

    private final NamedClassExtractor extractor = new NamedClassExtractor();

    private OWLClass current;

    public void setCurrentClass(OWLClass current) {
        this.current = current;
    }

    public void reset() {
        extractor.reset();
    }

    public Set<OWLClass> getResult() {
        return extractor.getResult();
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        axiom.getSuperClass().accept(extractor);
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        axiom.classExpressions().filter(x -> !x.equals(current)).forEach(x -> x.accept(extractor));
    }
}

