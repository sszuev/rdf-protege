package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLClassExpressionSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.Objects;
import java.util.Set;

public class OWLDisjointUnionAxiomFrameSection
		extends AbstractOWLFrameSection<OWLClass, OWLDisjointUnionAxiom, Set<OWLClassExpression>> {
	public final static String LABEL = "Disjoint Union Of";

	public OWLDisjointUnionAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<OWLClass> frame) {
		super(editorKit, LABEL, LABEL, frame);
	}

	@Override
	protected OWLDisjointUnionAxiom createAxiom(Set<OWLClassExpression> editedObject) {
		return getOWLDataFactory().getOWLDisjointUnionAxiom(getRootObject(), editedObject);
	}
	
	@Override
	protected void refill(OWLOntology ontology) {
		OWLClass root = getRootObject();
		ontology.disjointUnionAxioms(root)
				.map(axiom -> new OWLDisjointUnionAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, axiom))
				.forEach(this::addRow);
	}

	@Override
	public OWLObjectEditor<Set<OWLClassExpression>> getObjectEditor() {
        return new OWLClassExpressionSetEditor(getOWLEditorKit());
	}
	
	@Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLClassExpression>> editor) {
		return Objects.requireNonNull(editor.getEditedObject()).size() >= 2;
	}
	
    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
		return change.isAxiomChange() && change.getAxiom() instanceof OWLDisjointUnionAxiom
				&& ((OWLDisjointUnionAxiom) change.getAxiom()).getOWLClass().equals(getRootObject());
	}
}
