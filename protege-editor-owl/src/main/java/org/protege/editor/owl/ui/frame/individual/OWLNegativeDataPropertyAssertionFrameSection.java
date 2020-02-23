package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataPropertyRelationshipEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLDataPropertyConstantPair;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 01-Feb-2007<br><br>
 */
public class OWLNegativeDataPropertyAssertionFrameSection
        extends AbstractOWLFrameSection<OWLIndividual, OWLNegativeDataPropertyAssertionAxiom, OWLDataPropertyConstantPair> {

    public static final String LABEL = "Negative data property assertions";

    private OWLDataPropertyRelationshipEditor editor;

    public OWLNegativeDataPropertyAssertionFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLIndividual> frame) {
        super(editorKit, LABEL, "Negative data property assertion", frame);
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLIndividual root = getRootObject();
        ontology.negativeDataPropertyAssertionAxioms(root)
                .map(ax -> new OWLNegativeDataPropertyAssertionFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected OWLNegativeDataPropertyAssertionAxiom createAxiom(OWLDataPropertyConstantPair object) {
        return getOWLDataFactory().getOWLNegativeDataPropertyAssertionAxiom(object.getProperty(),
                getRootObject(), object.getConstant());
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
    	if (!change.isAxiomChange()) {
    		return false;
    	}
    	OWLAxiom axiom = change.getAxiom();
    	if (axiom instanceof OWLNegativeDataPropertyAssertionAxiom) {
    		return ((OWLNegativeDataPropertyAssertionAxiom) axiom).getSubject().equals(getRootObject());
    	}
    	return false;
    }

}
