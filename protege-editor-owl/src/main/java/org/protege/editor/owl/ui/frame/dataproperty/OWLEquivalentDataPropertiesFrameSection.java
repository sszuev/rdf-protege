package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLDataPropertyEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLEquivalentDataPropertiesFrameSection
        extends AbstractInferFrameSection<OWLDataProperty, OWLEquivalentDataPropertiesAxiom, OWLDataProperty> {

    public static final String LABEL = "Equivalent To";
    private final Set<OWLEquivalentDataPropertiesAxiom> added = new HashSet<>();

    @Override
    protected void clear() {
        added.clear();
    }

    public OWLEquivalentDataPropertiesFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLDataProperty> frame) {
        super(editorKit, LABEL, "Equivalent property", frame);
    }

    @Override
    protected OWLEquivalentDataPropertiesAxiom createAxiom(OWLDataProperty object) {
        return getOWLDataFactory().getOWLEquivalentDataPropertiesAxiom(CollectionFactory.createSet(getRootObject(), object));
    }

    @Override
    public OWLObjectEditor<OWLDataProperty> getObjectEditor() {
        return new OWLDataPropertyEditor(getOWLEditorKit());
    }
    
    @Override
    public boolean checkEditorResults(OWLObjectEditor<OWLDataProperty> editor) {
    	Set<OWLDataProperty> equivalents = editor.getEditedObjects();
    	return equivalents.size() != 1 || !equivalents.contains(getRootObject());
    }
    
    @Override
    public void handleEditingFinished(Set<OWLDataProperty> editedObjects) {
    	editedObjects = new HashSet<>(editedObjects);
    	editedObjects.remove(getRootObject());
    	super.handleEditingFinished(editedObjects);
    }

    @Override
    protected void refill(OWLOntology ontology) {
        added.clear();
        OWLDataProperty root = getRootObject();
        ontology.equivalentDataPropertiesAxioms(root).forEach(ax -> {
            addRow(new OWLEquivalentDataPropertiesFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            added.add(ax);
        });
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLDataProperty root = getRootObject();
        Set<OWLDataProperty> res = getReasoner().getEquivalentDataProperties(root).entities().collect(Collectors.toSet());
        res.remove(root);
        if (res.isEmpty()) {
            return;
        }
        OWLEquivalentDataPropertiesAxiom ax = getOWLDataFactory().getOWLEquivalentDataPropertiesAxiom(res);
        if (!added.contains(ax)) {
            addRow(new OWLEquivalentDataPropertiesFrameSectionRow(getOWLEditorKit(), this, null, root, ax));
        }
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_DATATYPE_PROPERTIES;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLEquivalentDataPropertiesAxiom
                && hasRoot(((OWLEquivalentDataPropertiesAxiom) change.getAxiom()).properties());
    }
}
