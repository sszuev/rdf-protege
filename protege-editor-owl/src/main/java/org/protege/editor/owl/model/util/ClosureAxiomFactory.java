package org.protege.editor.owl.model.util;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.Set;
import java.util.TreeSet;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 14-Jun-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
@SuppressWarnings("NullableProblems")
public class ClosureAxiomFactory extends ObjectSomeValuesFromFillerExtractor {

    protected final OWLDataFactory owlDataFactory;
    private final Set<OWLOntology> onts;
    private final Set<OWLClass> visitedClasses = new TreeSet<>();

    private ClosureAxiomFactory(OWLObjectProperty objectProperty, OWLDataFactory df, Set<OWLOntology> onts) {
        super(df, objectProperty);
        this.owlDataFactory = df;
        this.onts = onts;
    }

    public static OWLAxiom getClosureAxiom(OWLClass cls,
                                           OWLObjectProperty prop,
                                           OWLDataFactory df,
                                           Set<OWLOntology> onts) {
        ClosureAxiomFactory fac = new ClosureAxiomFactory(prop, df, onts);
        cls.accept(fac);
        OWLObjectAllValuesFrom closure = fac.getClosureRestriction();
        return (closure != null) ? df.getOWLSubClassOfAxiom(cls, closure) : null;
    }

    /**
     * Gets a universal restriction (<code>OWLObjectAllValuesFrom</code>)
     * that closes off the existential restrictions that have been visited by this visitor.
     * For example, if the visitor had visited p some A, p some B, then the restriction p only (A or B) would be returned.
     *
     * @return A universal restriction that represents a closure axiom for visited restrictions,
     * or <code>null</code> if no existential restrictions have been visited by this visitor
     * and a universal closure axiom therefore doesn't make sense.
     */
    public OWLObjectAllValuesFrom getClosureRestriction() {
        Set<OWLClassExpression> descriptions = getFillers();
        if (descriptions.isEmpty()) {
            return null;
        }
        if (descriptions.size() == 1) {
            return owlDataFactory.getOWLObjectAllValuesFrom(getObjectProperty(), descriptions.iterator().next());
        }
        return owlDataFactory.getOWLObjectAllValuesFrom(getObjectProperty(),
                owlDataFactory.getOWLObjectUnionOf(descriptions));
    }

    /* Get the inherited restrictions also */
    @Override
    public void visit(OWLClass cls) {
        if (visitedClasses.contains(cls)) {
            return;
        }
        if (onts == null) {
            return;
        }
        visitedClasses.add(cls);
        EntitySearcher.getSuperClasses(cls, onts.stream()).forEach(superCls -> superCls.accept(this));

        EntitySearcher.getEquivalentClasses(cls, onts.stream()).forEach(equiv -> equiv.accept(this));
    }

    @Override
    public void visit(OWLObjectIntersectionOf owlObjectIntersectionOf) {
        owlObjectIntersectionOf.operands().forEach(x -> x.accept(ClosureAxiomFactory.this));
    }

    /* Get min cardinality restriction fillers */
    @Override
    public void visit(OWLObjectMinCardinality r) {
        handleCardinality(r);
    }

    /* Get exact cardinality fillers */
    @Override
    public void visit(OWLObjectExactCardinality r) {
        handleCardinality(r);
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom restr) {
        if (!restr.getProperty().equals(getObjectProperty())) {
            return;
        }
        OWLClassExpression filler = restr.getFiller();
        if (filler.equals(owlDataFactory.getOWLThing())) {
            return;
        }
        fillers.add(filler);
    }

    private void handleCardinality(OWLObjectCardinalityRestriction restr) {
        if (!restr.getProperty().equals(getObjectProperty()) || restr.getCardinality() <= 0) {
            return;
        }
        OWLClassExpression filler = restr.getFiller();
        if (filler.equals(owlDataFactory.getOWLThing())) {
            return;
        }
        fillers.add(filler);
    }
}
