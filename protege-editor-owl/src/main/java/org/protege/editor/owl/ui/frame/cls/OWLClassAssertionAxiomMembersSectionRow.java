package org.protege.editor.owl.ui.frame.cls;

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
 * Date: 27-Jan-2007<br><br>
 */
public class OWLClassAssertionAxiomMembersSectionRow
        extends AbstractOWLFrameSectionRow<OWLClassExpression, OWLClassAssertionAxiom, OWLNamedIndividual> {

    public OWLClassAssertionAxiomMembersSectionRow(OWLEditorKit kit,
                                                   OWLFrameSection<OWLClassExpression, OWLClassAssertionAxiom, OWLNamedIndividual> section,
                                                   OWLOntology ontology, OWLClassExpression rootObject,
                                                   OWLClassAssertionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLNamedIndividual> getObjectEditor() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isDeleteable() {
        return true;
    }

    @Override
    protected OWLClassAssertionAxiom createAxiom(OWLNamedIndividual editedObject) {
        return getOWLDataFactory().getOWLClassAssertionAxiom(getRoot(), editedObject);
    }

    @Override
    public boolean isFixedHeight() {
        return true;
    }

    @Override
    public Stream<OWLIndividual> manipulatableObjects() {
        return Stream.of(getAxiom().getIndividual());
    }
}
