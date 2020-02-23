package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.classexpression.anonymouscls.AnonymousDefinedClassManager;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 28, 2008<br><br>
 */
public abstract class AbstractOWLClassAxiomFrameSection<A extends OWLAxiom, E>
        extends AbstractInferFrameSection<OWLClassExpression, A, E> {

    protected AbstractOWLClassAxiomFrameSection(OWLEditorKit kit,
                                                String label,
                                                String rowLabel,
                                                OWLFrame<? extends OWLClassExpression> frame) {
        super(kit, label, rowLabel, frame);
    }

    @Override
    public final OWLClassExpression getRootObject() {
        OWLClassExpression cls = super.getRootObject();
        if (cls == null) {
            return cls;
        }
        AnonymousDefinedClassManager m = getOWLModelManager().get(AnonymousDefinedClassManager.ID);
        if (m != null && m.isAnonymous(cls.asOWLClass())) {
            return m.getExpression(cls.asOWLClass());
        }
        return cls;
    }

    @Override
    protected final void refill(OWLOntology ontology) {
        classAxioms(getRootObject(), ontology).forEach(ax -> addAxiom(ax, ontology));
    }

    protected abstract void addAxiom(A ax, OWLOntology ont);

    protected abstract Stream<A> classAxioms(OWLClassExpression descr, OWLOntology ont);

}
