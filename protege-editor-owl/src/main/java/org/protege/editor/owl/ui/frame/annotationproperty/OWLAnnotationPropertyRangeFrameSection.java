package org.protege.editor.owl.ui.frame.annotationproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLAnnotationPropertyRangeEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 4, 2009<br><br>
 */
public class OWLAnnotationPropertyRangeFrameSection
        extends AbstractOWLFrameSection<OWLAnnotationProperty, OWLAnnotationPropertyRangeAxiom, IRI> {

    public static final String LABEL = "Range (intersection)";

    public OWLAnnotationPropertyRangeFrameSection(OWLEditorKit editorKit, OWLFrame<OWLAnnotationProperty> frame) {
        super(editorKit, LABEL, "Range", frame);
    }

    @Override
    protected OWLAnnotationPropertyRangeAxiom createAxiom(IRI iri) {
        return getOWLDataFactory().getOWLAnnotationPropertyRangeAxiom(getRootObject(), iri);
    }


    protected Stream<OWLAnnotationPropertyRangeAxiom> axioms(OWLOntology ontology) {
        return ontology.annotationPropertyRangeAxioms(getRootObject());
    }

    @Override
    public OWLObjectEditor<IRI> getObjectEditor() {
        return new OWLAnnotationPropertyRangeEditor(getOWLEditorKit());
    }

    @Override
    public final boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLObject obj : objects) {
            if (obj instanceof OWLEntity) {
                OWLEntity entity = (OWLEntity) obj;
                OWLAxiom ax = createAxiom(entity.getIRI());
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            } else {
                return false;
            }
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    @Override
    protected final void refill(OWLOntology ontology) {
        axioms(ontology)
                .map(ax -> new OWLAnnotationPropertyRangeFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), ax))
                .forEach(this::addRow);
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLAnnotationPropertyRangeAxiom
                && ((OWLAnnotationPropertyRangeAxiom) change.getAxiom()).getProperty().equals(getRootObject());
    }
}
