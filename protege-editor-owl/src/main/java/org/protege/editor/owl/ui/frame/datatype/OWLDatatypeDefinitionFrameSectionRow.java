package org.protege.editor.owl.ui.frame.datatype;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataRangeEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 5, 2009<br><br>
 */
public class OWLDatatypeDefinitionFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLDatatype, OWLDatatypeDefinitionAxiom, OWLDataRange> {

    public OWLDatatypeDefinitionFrameSectionRow(OWLEditorKit kit,
                                                OWLFrameSection<OWLDatatype, OWLDatatypeDefinitionAxiom, OWLDataRange> section,
                                                OWLOntology ontology,
                                                OWLDatatype rootObject, OWLDatatypeDefinitionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLDatatypeDefinitionAxiom createAxiom(OWLDataRange range) {
        return getOWLDataFactory().getOWLDatatypeDefinitionAxiom(getRoot(), range);
    }

    @Override
    protected OWLObjectEditor<OWLDataRange> getObjectEditor() {
        OWLDataRangeEditor editor = new OWLDataRangeEditor(getOWLEditorKit());
        editor.setEditedObject(getAxiom().getDataRange());
        return editor;
    }

    @Override
    public Stream<OWLDataRange> manipulatableObjects() {
        return Stream.of(getAxiom().getDataRange());
    }
}
