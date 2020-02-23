package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataPropertyRelationshipEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLDataPropertyConstantPair;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 01-Feb-2007<br><br>
 */
public class OWLNegativeDataPropertyAssertionFrameSectionRow
        extends AbstractPropertyAssertionFrameSectionRow<OWLIndividual, OWLNegativeDataPropertyAssertionAxiom, OWLDataPropertyConstantPair> {

    public OWLNegativeDataPropertyAssertionFrameSectionRow(OWLEditorKit kit,
                                                           OWLFrameSection<OWLIndividual, OWLNegativeDataPropertyAssertionAxiom, OWLDataPropertyConstantPair> section,
                                                           OWLOntology ontology,
                                                           OWLIndividual rootObject,
                                                           OWLNegativeDataPropertyAssertionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }


    protected OWLObjectEditor<OWLDataPropertyConstantPair> getObjectEditor() {
        OWLDataPropertyRelationshipEditor editor = new OWLDataPropertyRelationshipEditor(getOWLEditorKit());
        editor.setDataPropertyAxiom(getAxiom());
        return editor;
    }

    @Override
    protected OWLNegativeDataPropertyAssertionAxiom createAxiom(OWLDataPropertyConstantPair editedObject) {
        return getOWLDataFactory().getOWLNegativeDataPropertyAssertionAxiom(editedObject.getProperty(),
                getRoot(), editedObject.getConstant());
    }
}
