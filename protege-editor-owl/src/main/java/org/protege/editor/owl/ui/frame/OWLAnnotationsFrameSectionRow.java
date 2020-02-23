package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLAnnotationEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 26-Jan-2007<br><br>
 */
public class OWLAnnotationsFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> {

    public OWLAnnotationsFrameSectionRow(OWLEditorKit kit,
                                         OWLFrameSection<OWLAnnotationSubject, OWLAnnotationAssertionAxiom, OWLAnnotation> section,
                                         OWLOntology ontology,
                                         OWLAnnotationSubject rootObject, OWLAnnotationAssertionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLAnnotation> getObjectEditor() {
        OWLAnnotationEditor editor = new OWLAnnotationEditor(getOWLEditorKit());
        editor.setEditedObject(getAxiom().getAnnotation());
        return editor;
    }

    @Override
    protected OWLAnnotationAssertionAxiom createAxiom(OWLAnnotation editedObject) {
        return getOWLDataFactory().getOWLAnnotationAssertionAxiom(getRoot(), editedObject);
    }

    @Override
    public Stream<OWLAnnotation> manipulatableObjects() {
        return getAxiom().annotations();
    }
}
