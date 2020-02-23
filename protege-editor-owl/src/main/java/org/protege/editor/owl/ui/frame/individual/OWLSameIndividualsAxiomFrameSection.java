package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLIndividualSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLSameIndividualsAxiomFrameSection
        extends AbstractInferFrameSection<OWLNamedIndividual, OWLSameIndividualAxiom, Set<OWLNamedIndividual>> {

    public static final String LABEL = "Same Individual As";

    public OWLSameIndividualsAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLNamedIndividual> frame) {
        super(editorKit, LABEL, LABEL, frame);
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLNamedIndividual root = getRootObject();
        ontology.sameIndividualAxioms(root)
                .map(ax -> new OWLSameIndividualsAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLNamedIndividual root = getRootObject();
        Set<OWLIndividual> existingSameIndividuals = getCurrentlyDisplayedSameIndividuals();
        Set<OWLNamedIndividual> res = getCurrentReasoner().getSameIndividuals(root).entities()
                .filter(i -> !i.equals(root) && !existingSameIndividuals.contains(i))
                .collect(Collectors.toSet());
        if (res.isEmpty()) {
            return;
        }
        res.add(root);
        addRow(new OWLSameIndividualsAxiomFrameSectionRow(getOWLEditorKit(), this, null, root,
                getOWLDataFactory().getOWLSameIndividualAxiom(res)));
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_SAMEAS_INDIVIDUAL_ASSERTIONS;
    }

    public Set<OWLIndividual> getCurrentlyDisplayedSameIndividuals() {
        return getRows().stream().map(OWLFrameSectionRow::getAxiom)
                .flatMap(OWLNaryIndividualAxiom::individuals)
                .collect(Collectors.toSet());
    }

    @Override
    protected OWLSameIndividualAxiom createAxiom(Set<OWLNamedIndividual> object) {
        object.add(getRootObject());
        return getOWLDataFactory().getOWLSameIndividualAxiom(object);
    }

    @Override
    public OWLObjectEditor<Set<OWLNamedIndividual>> getObjectEditor() {
        return new OWLIndividualSetEditor(getOWLEditorKit());
    }
    
    @Override
	public boolean checkEditorResults(OWLObjectEditor<Set<OWLNamedIndividual>> editor) {
        return !Objects.requireNonNull(editor.getEditedObject()).contains(getRootObject());
    }
    
    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
    	if (!change.isAxiomChange()) {
    		return false;
    	}
    	OWLAxiom axiom = change.getAxiom();
    	if (axiom instanceof OWLSameIndividualAxiom) {
            return hasRoot(((OWLSameIndividualAxiom) axiom).individuals());
        }
    	return false;
    }
}
