package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataPropertyRelationshipEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLDataPropertyConstantPair;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 30-Jan-2007<br><br>
 */
public class OWLDataPropertyAssertionAxiomFrameSectionRow
        extends AbstractPropertyAssertionFrameSectionRow<OWLIndividual, OWLDataPropertyAssertionAxiom, OWLDataPropertyConstantPair> {

    public OWLDataPropertyAssertionAxiomFrameSectionRow(OWLEditorKit kit,
                                                        OWLFrameSection<OWLIndividual, OWLDataPropertyAssertionAxiom, OWLDataPropertyConstantPair> section,
                                                        OWLOntology ontology, OWLIndividual rootObject,
                                                        OWLDataPropertyAssertionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLDataPropertyConstantPair> getObjectEditor() {
        OWLDataPropertyRelationshipEditor editor = new OWLDataPropertyRelationshipEditor(getOWLEditorKit());
        editor.setDataPropertyAxiom(getAxiom());
        return editor;
    }

    @Override
    protected OWLDataPropertyAssertionAxiom createAxiom(OWLDataPropertyConstantPair editedObject) {
        return getOWLDataFactory().getOWLDataPropertyAssertionAxiom(editedObject.getProperty(),
                getRoot(), editedObject.getConstant());
    }
}
