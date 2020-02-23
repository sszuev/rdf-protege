package org.protege.editor.owl.ui.frame.annotationproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLAnnotationPropertyEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;

import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 4, 2009<br><br>
 */
public class OWLSubAnnotationPropertyFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLAnnotationProperty, OWLSubAnnotationPropertyOfAxiom, OWLAnnotationProperty> {

    public OWLSubAnnotationPropertyFrameSectionRow(OWLEditorKit kit,
                                                   OWLFrameSection<OWLAnnotationProperty, OWLSubAnnotationPropertyOfAxiom, OWLAnnotationProperty> section,
                                                   OWLOntology ontology,
                                                   OWLAnnotationProperty property,
                                                   OWLSubAnnotationPropertyOfAxiom axiom) {
        super(kit, section, ontology, property, axiom);
    }

    @Override
    protected OWLAnnotationPropertyEditor getObjectEditor() {
        final OWLAnnotationPropertyEditor editor = new OWLAnnotationPropertyEditor(getOWLEditorKit());
        editor.setEditedObject(getAxiom().getSuperProperty());
        return editor;
    }

    @Override
    protected OWLSubAnnotationPropertyOfAxiom createAxiom(OWLAnnotationProperty property) {
        return getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(getRoot(), property);
    }

    @Override
    public Stream<OWLAnnotationProperty> manipulatableObjects() {
        return Stream.of(getAxiom().getSuperProperty());
    }
}
