package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyIndividualPairEditor2;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLObjectPropertyIndividualPair;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 30-Jan-2007<br><br>
 */
public class OWLObjectPropertyAssertionAxiomFrameSectionRow
        extends AbstractPropertyAssertionFrameSectionRow<OWLIndividual, OWLObjectPropertyAssertionAxiom, OWLObjectPropertyIndividualPair> {

    public OWLObjectPropertyAssertionAxiomFrameSectionRow(OWLEditorKit kit,
                                                          OWLFrameSection<OWLIndividual, OWLObjectPropertyAssertionAxiom, OWLObjectPropertyIndividualPair> section,
                                                          OWLOntology ontology,
                                                          OWLIndividual rootObject,
                                                          OWLObjectPropertyAssertionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLObjectPropertyIndividualPair> getObjectEditor() {
        OWLObjectPropertyIndividualPairEditor2 editor = new OWLObjectPropertyIndividualPairEditor2(getOWLEditorKit());
        editor.setEditedObject(new OWLObjectPropertyIndividualPair(getAxiom().getProperty().asOWLObjectProperty(), getAxiom().getObject()));
        return editor;
    }

    @Override
    protected OWLObjectPropertyAssertionAxiom createAxiom(OWLObjectPropertyIndividualPair editedObject) {
        return getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(editedObject.getProperty(),
                getRoot(), editedObject.getIndividual());
    }
}
