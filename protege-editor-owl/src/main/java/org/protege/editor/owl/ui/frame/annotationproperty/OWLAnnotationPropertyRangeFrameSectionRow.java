package org.protege.editor.owl.ui.frame.annotationproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLAnnotationPropertyRangeEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 4, 2009<br><br>
 */
public class OWLAnnotationPropertyRangeFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLAnnotationProperty, OWLAnnotationPropertyRangeAxiom, IRI> {

    public OWLAnnotationPropertyRangeFrameSectionRow(OWLEditorKit kit,
                                                     OWLFrameSection<OWLAnnotationProperty, OWLAnnotationPropertyRangeAxiom, IRI> section,
                                                     OWLOntology ontology,
                                                     OWLAnnotationProperty property,
                                                     OWLAnnotationPropertyRangeAxiom axiom) {
        super(kit, section, ontology, property, axiom);
    }

    @Override
    protected OWLAnnotationPropertyRangeEditor getObjectEditor() {
        final OWLAnnotationPropertyRangeEditor editor = new OWLAnnotationPropertyRangeEditor(getOWLEditorKit());
        editor.setEditedObject(getAxiom().getRange());
        return editor;
    }

    @Override
    protected OWLAnnotationPropertyRangeAxiom createAxiom(IRI iri) {
        return getOWLDataFactory().getOWLAnnotationPropertyRangeAxiom(getRoot(), iri);
    }

    @Override
    public Stream<IRI> manipulatableObjects() {
        return Stream.of(getAxiom().getRange());
    }
}