package org.protege.editor.owl.ui.frame.annotationproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLAnnotationPropertyEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 4, 2009<br><br>
 */
public class OWLSubAnnotationPropertyFrameSection
        extends AbstractOWLFrameSection<OWLAnnotationProperty, OWLSubAnnotationPropertyOfAxiom, OWLAnnotationProperty> {

    public static final String LABEL = "Superproperties";

    private OWLAnnotationPropertyEditor editor;

    public OWLSubAnnotationPropertyFrameSection(OWLEditorKit editorKit, OWLFrame<OWLAnnotationProperty> frame) {
        super(editorKit, LABEL, "Superproperties", frame);
    }

    @Override
    protected OWLSubAnnotationPropertyOfAxiom createAxiom(OWLAnnotationProperty superProp) {
        return getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(getRootObject(), superProp);
    }

    @Override
    public OWLObjectEditor<OWLAnnotationProperty> getObjectEditor() {
        if (editor == null){
            editor = new OWLAnnotationPropertyEditor(getOWLEditorKit());
        }
        return editor;
    }

    @Override
    public final boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLAnnotationProperty)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLAnnotationProperty)) {
                return false;
            }
            OWLAnnotationProperty property = (OWLAnnotationProperty) obj;
            OWLAxiom ax = createAxiom(property);
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    @Override
    protected final void refill(OWLOntology ontology) {
        OWLAnnotationProperty root = getRootObject();
        ontology.subAnnotationPropertyOfAxioms(root)
                .map(ax -> new OWLSubAnnotationPropertyFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
    	if (!change.isAxiomChange()) {
    		return false;
    	}
    	OWLAxiom axiom = change.getAxiom();
    	if (axiom instanceof OWLSubAnnotationPropertyOfAxiom) {
    		return ((OWLSubAnnotationPropertyOfAxiom) axiom).getSubProperty().equals(getRootObject());
    	}
    	return false;
    }

}
