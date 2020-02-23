package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataRangeEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLDataPropertyRangeFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLDataProperty, OWLDataPropertyRangeAxiom, OWLDataRange> {

    public OWLDataPropertyRangeFrameSectionRow(OWLEditorKit kit,
                                               OWLFrameSection<OWLDataProperty, OWLDataPropertyRangeAxiom, OWLDataRange> section,
                                               OWLOntology ontology,
                                               OWLDataProperty rootObject,
                                               OWLDataPropertyRangeAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLDataPropertyRangeAxiom createAxiom(OWLDataRange editedObject) {
        return getOWLDataFactory().getOWLDataPropertyRangeAxiom(getRoot(), editedObject);
    }

    @Override
    protected OWLObjectEditor<OWLDataRange> getObjectEditor() {
        OWLDataRangeEditor editor = new OWLDataRangeEditor(getOWLEditorKit());
        editor.setEditedObject(getAxiom().getRange());
        return editor;
    }

    @Override
    public Stream<OWLDataRange> manipulatableObjects() {
        return Stream.of(getAxiom().getRange());
    }
}
