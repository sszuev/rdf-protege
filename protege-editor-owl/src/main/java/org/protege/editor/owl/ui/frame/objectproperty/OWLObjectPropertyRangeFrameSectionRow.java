package org.protege.editor.owl.ui.frame.objectproperty;

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
public class OWLObjectPropertyRangeFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLObjectProperty, OWLObjectPropertyRangeAxiom, OWLClassExpression> {

    public OWLObjectPropertyRangeFrameSectionRow(OWLEditorKit kit,
                                                 OWLFrameSection<OWLObjectProperty, OWLObjectPropertyRangeAxiom, OWLClassExpression> section,
                                                 OWLOntology ontology,
                                                 OWLObjectProperty rootObject,
                                                 OWLObjectPropertyRangeAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLComponentFactory().getOWLClassDescriptionEditor(getAxiom().getRange(), AxiomType.OBJECT_PROPERTY_RANGE);
    }

    @Override
    protected OWLObjectPropertyRangeAxiom createAxiom(OWLClassExpression editedObject) {
        return getOWLDataFactory().getOWLObjectPropertyRangeAxiom(getRoot(), editedObject);
    }

    @Override
    public Stream<OWLClassExpression> manipulatableObjects() {
        return Stream.of(getAxiom().getRange());
    }
}
