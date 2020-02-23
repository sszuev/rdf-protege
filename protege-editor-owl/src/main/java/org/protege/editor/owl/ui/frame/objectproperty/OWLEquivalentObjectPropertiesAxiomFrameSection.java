package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyExpressionEditor;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLEquivalentObjectPropertiesAxiomFrameSection
        extends AbstractInferFrameSection<OWLObjectProperty, OWLEquivalentObjectPropertiesAxiom, OWLObjectPropertyExpression> {

    public static final String LABEL = "Equivalent To";
    private final Set<OWLEquivalentObjectPropertiesAxiom> added = new HashSet<>();

    public OWLEquivalentObjectPropertiesAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLObjectProperty> frame) {
        super(editorKit, LABEL, "Equivalent object property", frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void refill(OWLOntology ontology) {
        added.clear();
        OWLObjectProperty root = getRootObject();
        ontology.equivalentObjectPropertiesAxioms(root).forEach(ax -> {
            addRow(new OWLEquivalentObjectPropertiesAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            added.add(ax);
        });
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLObjectProperty root = getRootObject();
        Node<OWLObjectPropertyExpression> node = getReasoner().getEquivalentObjectProperties(root);
        if (node.getEntitiesMinus(root).isEmpty()) {
            return;
        }
        OWLEquivalentObjectPropertiesAxiom ax = getOWLDataFactory()
                .getOWLEquivalentObjectPropertiesAxiom(node.entities().collect(Collectors.toSet()));
        if (added.contains(ax)) {
            return;
        }
        addInferredRowIfNontrivial(new OWLEquivalentObjectPropertiesAxiomFrameSectionRow(getOWLEditorKit(), this, null, root, ax));
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_OBJECT_PROPERTIES;
    }

    @Override
    protected OWLEquivalentObjectPropertiesAxiom createAxiom(OWLObjectPropertyExpression object) {
        return getOWLDataFactory().getOWLEquivalentObjectPropertiesAxiom(CollectionFactory.createSet(getRootObject(), object));
    }

    @Override
    public OWLObjectEditor<OWLObjectPropertyExpression> getObjectEditor() {
        return new OWLObjectPropertyExpressionEditor(getOWLEditorKit());
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<OWLObjectPropertyExpression> editor) {
        Set<OWLObjectPropertyExpression> equivalents = editor.getEditedObjects();
        return equivalents.size() != 1 || !equivalents.contains(getRootObject());
    }
    
    @Override
    public void handleEditingFinished(Set<OWLObjectPropertyExpression> editedObjects) {
    	editedObjects = new HashSet<>(editedObjects);
    	editedObjects.remove(getRootObject());
    	super.handleEditingFinished(editedObjects);
    }
    
    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLEquivalentObjectPropertiesAxiom
                && hasRoot(((OWLEquivalentObjectPropertiesAxiom) change.getAxiom()).properties());
    }

}
