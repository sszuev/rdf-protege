package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLDataPropertyRelationshipEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLDataPropertyConstantPair;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.HashSet;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 30-Jan-2007<br><br>
 */
public class OWLDataPropertyAssertionAxiomFrameSection
        extends AbstractInferFrameSection<OWLIndividual, OWLDataPropertyAssertionAxiom, OWLDataPropertyConstantPair> {

    public static final String LABEL = "Data property assertions";

    private OWLDataPropertyRelationshipEditor editor;

    private final Set<OWLDataPropertyAssertionAxiom> added = new HashSet<>();

    public OWLDataPropertyAssertionAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLIndividual> frame) {
        super(editorKit, LABEL, "Data property assertion", frame);
    }

    @Override
    protected void refill(OWLOntology ontology) {
        added.clear();
        OWLIndividual root = getRootObject();
        ontology.dataPropertyAssertionAxioms(root).forEach(ax -> {
            addRow(new OWLDataPropertyAssertionAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            added.add(ax);
        });
    }

    @Override
    protected void clear() {
        if (editor != null) {
            editor.clear();
        }
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLIndividual root = getRootObject();
        if (root.isAnonymous()) {
            return;
        }
        getReasoner().getRootOntology().dataPropertiesInSignature(Imports.INCLUDED).forEach(dp -> {
            Set<OWLLiteral> values = getReasoner().getDataPropertyValues(root.asOWLNamedIndividual(), dp);
            for (OWLLiteral constant : values) {
                OWLDataPropertyAssertionAxiom ax = getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dp, root, constant);
                if (!added.contains(ax)) {
                    addRow(new OWLDataPropertyAssertionAxiomFrameSectionRow(getOWLEditorKit(), this, null, root, ax));
                }
            }
        });
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_DATA_PROPERTY_ASSERTIONS;
    }

    @Override
    protected OWLDataPropertyAssertionAxiom createAxiom(OWLDataPropertyConstantPair object) {
        return getOWLDataFactory().getOWLDataPropertyAssertionAxiom(object.getProperty(), getRootObject(), object.getConstant());
    }

    @Override
    public OWLObjectEditor<OWLDataPropertyConstantPair> getObjectEditor() {
        if (editor == null) {
            editor = new OWLDataPropertyRelationshipEditor(getOWLEditorKit());
        }
        return editor;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLDataPropertyAssertionAxiom
                && ((OWLDataPropertyAssertionAxiom) change.getAxiom()).getSubject().equals(getRootObject());
    }

}
