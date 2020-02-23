package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyIndividualPairEditor2;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLObjectPropertyIndividualPair;
import org.semanticweb.owlapi.model.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 01-Feb-2007<br><br>
 */
public class OWLNegativeObjectPropertyAssertionFrameSection
        extends AbstractOWLFrameSection<OWLIndividual, OWLNegativeObjectPropertyAssertionAxiom, OWLObjectPropertyIndividualPair> {

    public static final String LABEL = "Negative object property assertions";
    private OWLObjectPropertyIndividualPairEditor2 editor;

    public OWLNegativeObjectPropertyAssertionFrameSection(OWLEditorKit editorKit,
                                                          OWLFrame<? extends OWLIndividual> frame) {
        super(editorKit, LABEL, "Negative object property assertion", frame);
        editor = new OWLObjectPropertyIndividualPairEditor2(getOWLEditorKit());
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLIndividual root = getRootObject();
        ontology.negativeObjectPropertyAssertionAxioms(root)
                .map(ax -> new OWLNegativeObjectPropertyAssertionFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected OWLNegativeObjectPropertyAssertionAxiom createAxiom(OWLObjectPropertyIndividualPair object) {
        return getOWLDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(object.getProperty(),
                getRootObject(), object.getIndividual());
    }

    @Override
    public OWLObjectEditor<OWLObjectPropertyIndividualPair> getObjectEditor() {
        return editor;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
    	if (!change.isAxiomChange()) {
    		return false;
    	}
    	OWLAxiom axiom = change.getAxiom();
    	if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiom) {
    		return ((OWLNegativeObjectPropertyAssertionAxiom) axiom).getSubject().equals(getRootObject());
    	}
    	return false;
    }
}
