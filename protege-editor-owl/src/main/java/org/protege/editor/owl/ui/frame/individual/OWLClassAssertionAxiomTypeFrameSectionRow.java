package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.*;

import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLClassAssertionAxiomTypeFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLIndividual, OWLClassAssertionAxiom, OWLClassExpression> {

    public OWLClassAssertionAxiomTypeFrameSectionRow(OWLEditorKit kit,
                                                     OWLFrameSection<OWLIndividual, OWLClassAssertionAxiom, OWLClassExpression> section,
                                                     OWLOntology ontology, OWLIndividual rootObject,
                                                     OWLClassAssertionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLComponentFactory().getOWLClassDescriptionEditor(getAxiom().getClassExpression(), AxiomType.CLASS_ASSERTION);
    }

    @Override
    protected OWLClassAssertionAxiom createAxiom(OWLClassExpression editedObject) {
        return getOWLDataFactory().getOWLClassAssertionAxiom(editedObject, getRoot());
    }

    @Override
    public Stream<OWLClassExpression> manipulatableObjects() {
        return Stream.of(getAxiom().getClassExpression());
    }
}
