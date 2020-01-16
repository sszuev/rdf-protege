package org.protege.editor.owl.model.util;

import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.HashSet;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 03-Jul-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
@Deprecated
public class CoveringAxiomFactory implements OWLClassExpressionVisitor {

    private OWLDataFactory owlDataFactory;

    private OWLHierarchyProvider<OWLClass> provider;

    private Set<OWLClassExpression> operands;


    public CoveringAxiomFactory(OWLDataFactory owlDataFactory, OWLHierarchyProvider<OWLClass> provider) {
        this.owlDataFactory = owlDataFactory;
        this.provider = provider;
        operands = new HashSet<>();
    }


    public void reset() {
        operands.clear();
    }


    public OWLClassExpression getCoveringAxiom() {
        if (operands.isEmpty()) {
            return null;
        }
        return owlDataFactory.getOWLObjectUnionOf(operands);
    }


    public void visit(OWLClass owlClass) {
        reset();
        for (OWLClass cls : provider.getChildren(owlClass)) {
            operands.add(cls);
        }
    }
}
