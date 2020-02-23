package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLDataPropertyEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLSubDataPropertyAxiomSuperPropertyFrameSection
        extends AbstractInferFrameSection<OWLDataProperty, OWLSubDataPropertyOfAxiom, OWLDataProperty> {

    public static final String LABEL = "SubProperty Of";

    private final Set<OWLDataPropertyExpression> added = new HashSet<>();

    public OWLSubDataPropertyAxiomSuperPropertyFrameSection(OWLEditorKit editorKit,
                                                            OWLFrame<? extends OWLDataProperty> frame) {
        super(editorKit, LABEL, "Super property", frame);
    }

    @Override
    protected OWLSubDataPropertyOfAxiom createAxiom(OWLDataProperty object) {
        return getOWLDataFactory().getOWLSubDataPropertyOfAxiom(getRootObject(), object);
    }

    @Override
    public OWLObjectEditor<OWLDataProperty> getObjectEditor() {
        return new OWLDataPropertyEditor(getOWLEditorKit());
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void refill(OWLOntology ontology) {
        added.clear();
        OWLDataProperty root = getRootObject();
        ontology.dataSubPropertyAxiomsForSubProperty(root).forEach(ax -> {
            addRow(new OWLSubDataPropertyAxiomSuperPropertyFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            added.add(ax.getSuperProperty());
        });
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLDataProperty root = getRootObject();
        getReasoner().getSuperDataProperties(root, true).entities()
                .filter(p -> !added.contains(p))
                .map(p -> getOWLDataFactory().getOWLSubDataPropertyOfAxiom(root, p))
                .map(ax -> new OWLSubDataPropertyAxiomSuperPropertyFrameSectionRow(getOWLEditorKit(),
                        this, null, root, ax))
                .forEach(this::addInferredRowIfNontrivial);
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_SUPER_DATATYPE_PROPERTIES;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLSubDataPropertyOfAxiom) {
            return ((OWLSubDataPropertyOfAxiom) axiom).getSubProperty().equals(getRootObject());
        }
        return false;
    }
}
