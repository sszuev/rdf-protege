package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyTabbedSetEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLDisjointObjectPropertiesFrameSection
        extends AbstractOWLFrameSection<OWLObjectProperty, OWLDisjointObjectPropertiesAxiom, Set<OWLObjectPropertyExpression>> {

    public static final String LABEL = "Disjoint With";

    public OWLDisjointObjectPropertiesFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLObjectProperty> frame) {
        super(editorKit, LABEL, "Disjoint properties", frame);
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLObjectProperty root = getRootObject();
        ontology.disjointObjectPropertiesAxioms(getRootObject())
                .map(ax -> new OWLDisjointObjectPropertiesAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected OWLDisjointObjectPropertiesAxiom createAxiom(Set<OWLObjectPropertyExpression> object) {
        Set<OWLObjectPropertyExpression> disjoints = new HashSet<>(object);
        disjoints.add(getRootObject());
        return getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(disjoints);
    }

    @Override
    public OWLObjectEditor<Set<OWLObjectPropertyExpression>> getObjectEditor() {
        return new OWLObjectPropertyTabbedSetEditor(getOWLEditorKit());
    }

    @Override
	public boolean checkEditorResults(OWLObjectEditor<Set<OWLObjectPropertyExpression>> editor) {
        return !Objects.requireNonNull(editor.getEditedObject()).contains(getRootObject());
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLDisjointObjectPropertiesAxiom
                && hasRoot(((OWLDisjointObjectPropertiesAxiom) change.getAxiom()).properties());
    }
}
