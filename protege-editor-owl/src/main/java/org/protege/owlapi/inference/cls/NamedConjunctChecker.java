package org.protege.owlapi.inference.cls;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import java.util.Iterator;

/**
 * Checks whether a class description contains a specified named conjunct.
 */
public class NamedConjunctChecker implements OWLClassExpressionVisitor {
    private boolean found;
    private OWLClass searchClass;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean containsConjunct(OWLClass conjunct, OWLClassExpression description) {
        found = false;
        searchClass = conjunct;
        description.accept(this);
        return found;
    }

    @Override
    public void visit(OWLClass desc) {
        if (desc.equals(searchClass)) {
            found = true;
        }
    }

    @Override
    public void visit(OWLObjectIntersectionOf desc) {
        Iterator<OWLClassExpression> it = desc.operands().iterator();
        while (it.hasNext()) {
            it.next().accept(this);
            if (found) {
                return;
            }
        }
    }
}