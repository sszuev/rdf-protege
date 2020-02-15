package org.protege.owlapi.inference.cls;

import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public class ChildClassExtractor implements OWLAxiomVisitor {

    private final NamedConjunctChecker checker = new NamedConjunctChecker();
    private final NamedClassExtractor namedClassExtractor = new NamedClassExtractor();
    private final Set<OWLClass> results = new HashSet<>();

    private OWLClass currentParentClass;

    public void reset() {
        results.clear();
        namedClassExtractor.reset();
    }

    public void setCurrentParentClass(OWLClass currentParentClass) {
        this.currentParentClass = currentParentClass;
        reset();
    }

    public Set<OWLClass> getResult() {
        return new HashSet<>(results);
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        // Example:
        // If searching for subs of B, candidates are:
        // SubClassOf(A B)
        // SubClassOf(A And(B ...))
        if (!checker.containsConjunct(currentParentClass, axiom.getSuperClass())) {
            return;
        }
        // We only want named classes
        if (axiom.getSubClass().isAnonymous()) {
            return;
        }
        results.add(axiom.getSubClass().asOWLClass());
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        // EquivalentClasses(A  And(B...))
        if (!namedClassInEquivalentAxiom(axiom)) {
            return;
        }
        Set<OWLClassExpression> candidateDescriptions = new HashSet<>();
        boolean found = false;
        Iterator<OWLClassExpression> it = axiom.classExpressions().iterator();
        while (it.hasNext()) {
            OWLClassExpression equivalentClass = it.next();
            if (!checker.containsConjunct(currentParentClass, equivalentClass)) {
                // Potential operand
                candidateDescriptions.add(equivalentClass);
            } else {
                // This axiom is relevant
                if (equivalentClass.isAnonymous()) {
                    found = true;
                }
            }
        }
        if (!found) {
            return;
        }
        namedClassExtractor.reset();
        for (OWLClassExpression desc : candidateDescriptions) {
            desc.accept(namedClassExtractor);
        }
        results.addAll(namedClassExtractor.getResult());
    }

    private boolean namedClassInEquivalentAxiom(OWLEquivalentClassesAxiom axiom) {
        return axiom.classExpressions().anyMatch(x -> !x.isAnonymous());
    }
}