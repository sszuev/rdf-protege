package org.protege.editor.owl.ui.frame.datatype;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataRangeEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 5, 2009<br><br>
 */
public class OWLDatatypeDefinitionFrameSection extends AbstractOWLFrameSection<OWLDatatype, OWLDatatypeDefinitionAxiom, OWLDataRange> {

    public static final String LABEL = "Datatype Definitions";

    public OWLDatatypeDefinitionFrameSection(OWLEditorKit editorKit, OWLFrame<OWLDatatype> frame) {
        super(editorKit, LABEL, "Datatype Definition", frame);
    }

    @Override
    protected OWLDatatypeDefinitionAxiom createAxiom(OWLDataRange range) {
        return getOWLDataFactory().getOWLDatatypeDefinitionAxiom(getRootObject(), range);
    }

    @Override
    public OWLObjectEditor<OWLDataRange> getObjectEditor() {
        return new OWLDataRangeEditor(getOWLEditorKit());
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLDatatype root = getRootObject();
        ontology.datatypeDefinitions(root)
                .map(ax -> new OWLDatatypeDefinitionFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLDatatypeDefinitionAxiom
                && ((OWLDatatypeDefinitionAxiom) change.getAxiom()).getDatatype().equals(getRootObject());
    }
}
