package org.protege.editor.owl.ui.frame.property;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.*;

import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 16, 2008<br><br>
 */
public abstract class AbstractPropertyDomainFrameSectionRow<P extends OWLProperty, A extends OWLPropertyDomainAxiom<?>>
        extends AbstractOWLFrameSectionRow<P, A, OWLClassExpression> {

    public AbstractPropertyDomainFrameSectionRow(OWLEditorKit kit,
                                                 OWLFrameSection<P, A, OWLClassExpression> section,
                                                 OWLOntology ontology,
                                                 P root,
                                                 A axiom) {
        super(kit, section, ontology, root, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        A ax = getAxiom();
        return getOWLComponentFactory().getOWLClassDescriptionEditor(ax.getDomain(), ax.getAxiomType());
    }

    @Override
    public Stream<? extends OWLObject> manipulatableObjects() {
        return Stream.of(getAxiom().getDomain());
    }
}

