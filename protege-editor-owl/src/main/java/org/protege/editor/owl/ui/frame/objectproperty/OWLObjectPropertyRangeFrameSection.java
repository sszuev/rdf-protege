package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLObjectPropertyRangeFrameSection
        extends AbstractInferFrameSection<OWLObjectProperty, OWLObjectPropertyRangeAxiom, OWLClassExpression> {

    public static final String LABEL = "Ranges (intersection)";

    private final Set<OWLClassExpression> added = new HashSet<>();

    public OWLObjectPropertyRangeFrameSection(OWLEditorKit owlEditorKit, OWLFrame<? extends OWLObjectProperty> frame) {
        super(owlEditorKit, LABEL, "Range", frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLObjectProperty root = getRootObject();
        ontology.objectPropertyRangeAxioms(root).forEach(ax -> {
            addRow(new OWLObjectPropertyRangeFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            added.add(ax.getRange());
        });
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLObjectProperty root = getRootObject();
        getReasoner().getObjectPropertyRanges(root, true)
                .entities()
                .forEach(range -> {
                    if (!added.contains(range)) {
                        OWLObjectPropertyRangeAxiom ax = getOWLDataFactory().getOWLObjectPropertyRangeAxiom(root, range);
                        addInferredRowIfNontrivial(new OWLObjectPropertyRangeFrameSectionRow(getOWLEditorKit(), this, null, root, ax));
                    }
                    added.add(range);
                });
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_RANGES;
    }

    @Override
    protected OWLObjectPropertyRangeAxiom createAxiom(OWLClassExpression object) {
        return getOWLDataFactory().getOWLObjectPropertyRangeAxiom(getRootObject(), object);
    }

    @Override
    public OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLComponentFactory().getOWLClassDescriptionEditor(null, AxiomType.OBJECT_PROPERTY_RANGE);
    }

    @Override
    public boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
            OWLClassExpression desc = (OWLClassExpression) obj;
            OWLAxiom ax = getOWLDataFactory().getOWLObjectPropertyRangeAxiom(getRootObject(), desc);
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
    	if (!change.isAxiomChange()) {
    		return false;
    	}
    	OWLAxiom axiom = change.getAxiom();
    	if (axiom instanceof OWLObjectPropertyRangeAxiom) {
    		return ((OWLObjectPropertyRangeAxiom) axiom).getProperty().equals(getRootObject());
    	}
    	return false;
    }
}
