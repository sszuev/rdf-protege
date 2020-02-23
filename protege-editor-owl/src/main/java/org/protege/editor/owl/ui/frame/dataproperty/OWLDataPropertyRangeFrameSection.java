package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataRangeEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLDataPropertyRangeFrameSection
        extends AbstractOWLFrameSection<OWLDataProperty, OWLDataPropertyRangeAxiom, OWLDataRange> {

    public static final String LABEL = "Ranges";

    public OWLDataPropertyRangeFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLDataProperty> frame) {
        super(editorKit, LABEL, "Range", frame);
    }

    @Override
    protected OWLDataPropertyRangeAxiom createAxiom(OWLDataRange object) {
        return getOWLDataFactory().getOWLDataPropertyRangeAxiom(getRootObject(), object);
    }

    @Override
    public OWLObjectEditor<OWLDataRange> getObjectEditor() {
        return new OWLDataRangeEditor(getOWLEditorKit());
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLDataProperty root = getRootObject();
        ontology.dataPropertyRangeAxioms(root)
                .map(ax -> new OWLDataPropertyRangeFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() &&
                change.getAxiom() instanceof OWLDataPropertyRangeAxiom &&
                ((OWLDataPropertyRangeAxiom) change.getAxiom()).getProperty().equals(getRootObject());
    }
}
