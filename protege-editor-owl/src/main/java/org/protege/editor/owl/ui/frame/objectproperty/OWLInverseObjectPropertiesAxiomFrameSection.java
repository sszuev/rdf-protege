package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyEditor;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLInverseObjectPropertiesAxiomFrameSection
        extends AbstractInferFrameSection<OWLObjectProperty, OWLInverseObjectPropertiesAxiom, OWLObjectProperty> {

    public static final String LABEL = "Inverse Of";

    private final Set<OWLObjectPropertyExpression> added = new HashSet<>();

    public OWLInverseObjectPropertiesAxiomFrameSection(OWLEditorKit editorKit,
                                                       OWLFrame<? extends OWLObjectProperty> frame) {
        super(editorKit, LABEL, "Inverse property", frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLObjectProperty root = getRootObject();
        ontology.inverseObjectPropertyAxioms(root).forEach(ax -> {
            addRow(new OWLInverseObjectPropertiesAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            ax.properties().forEach(added::add);
        });
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLObjectProperty root = getRootObject();
        getReasoner().getInverseObjectProperties(root).entities()
                .filter(p -> !added.contains(p))
                .map(p -> getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(root, p))
                .map(ax -> new OWLInverseObjectPropertiesAxiomFrameSectionRow(getOWLEditorKit(), this, null, root, ax))
                .forEach(this::addInferredRowIfNontrivial);
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_INVERSE_PROPERTIES;
    }

    @Override
    protected OWLInverseObjectPropertiesAxiom createAxiom(OWLObjectProperty object) {
        return getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(getRootObject(), object);
    }

    @Override
    public OWLObjectEditor<OWLObjectProperty> getObjectEditor() {
        return new OWLObjectPropertyEditor(getOWLEditorKit());
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLInverseObjectPropertiesAxiom) {
            return hasRoot(((OWLInverseObjectPropertiesAxiom) axiom).properties());
        }
        return false;
    }
}
