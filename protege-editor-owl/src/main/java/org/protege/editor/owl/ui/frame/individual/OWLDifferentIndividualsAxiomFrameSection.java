package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLIndividualSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.Objects;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLDifferentIndividualsAxiomFrameSection extends AbstractOWLFrameSection<OWLNamedIndividual, OWLDifferentIndividualsAxiom, Set<OWLNamedIndividual>> {

    public static final String LABEL = "Different Individuals";

    public OWLDifferentIndividualsAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLNamedIndividual> frame) {
        super(editorKit, LABEL, "Different individuals", frame);
    }

    @Override
    protected void clear() {
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLNamedIndividual root = getRootObject();
        ontology.differentIndividualAxioms(root)
                .map(ax -> new OWLDifferentIndividualAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected OWLDifferentIndividualsAxiom createAxiom(Set<OWLNamedIndividual> object) {
        object.add(getRootObject());
        return getOWLDataFactory().getOWLDifferentIndividualsAxiom(object);
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
        return change.isAxiomChange() && change.getAxiom() instanceof OWLDifferentIndividualsAxiom
                && ((OWLDifferentIndividualsAxiom) change.getAxiom()).individuals().anyMatch(x -> x.equals(getRootObject()));
    }
}
