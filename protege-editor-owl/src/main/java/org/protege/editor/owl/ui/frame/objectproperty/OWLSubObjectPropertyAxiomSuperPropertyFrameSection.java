package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyExpressionEditor;
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
public class OWLSubObjectPropertyAxiomSuperPropertyFrameSection
        extends AbstractInferFrameSection<OWLObjectProperty, OWLSubObjectPropertyOfAxiom, OWLObjectPropertyExpression> {

    public static final String LABEL = "SubProperty Of";

    private final Set<OWLObjectPropertyExpression> added = new HashSet<>();

    public OWLSubObjectPropertyAxiomSuperPropertyFrameSection(OWLEditorKit editorKit,
                                                              OWLFrame<? extends OWLObjectProperty> frame) {
        super(editorKit, LABEL, "Super property", frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLObjectProperty root = getRootObject();
        ontology.objectSubPropertyAxiomsForSubProperty(root).forEach(ax -> {
            addRow(new OWLSubObjectPropertyAxiomSuperPropertyFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            added.add(ax.getSuperProperty());
        });
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLObjectProperty root = getRootObject();
        getReasoner().getSuperObjectProperties(root, true).entities()
                .filter(p -> !added.contains(p))
                .map(p -> new OWLSubObjectPropertyAxiomSuperPropertyFrameSectionRow(getOWLEditorKit(), this, null, root,
                        getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(root, p)))
                .forEach(this::addInferredRowIfNontrivial);
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_SUPER_OBJECT_PROPERTIES;
    }

    @Override
    protected OWLSubObjectPropertyOfAxiom createAxiom(OWLObjectPropertyExpression object) {
        return getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(getRootObject(), object);
    }

    @Override
    public OWLObjectEditor<OWLObjectPropertyExpression> getObjectEditor() {
        return new OWLObjectPropertyExpressionEditor(getOWLEditorKit());
    }
    
    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
    	if (!change.isAxiomChange()) {
    		return false;
    	}
    	OWLAxiom axiom = change.getAxiom();
    	if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
    		return ((OWLSubObjectPropertyOfAxiom) axiom).getSubProperty().equals(getRootObject());
    	}
    	return false;
    }
}
