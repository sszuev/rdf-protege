package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyIndividualPairEditor2;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLObjectPropertyIndividualPair;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 01-Feb-2007<br><br>
 */
public class OWLNegativeObjectPropertyAssertionFrameSectionRow
        extends AbstractPropertyAssertionFrameSectionRow<OWLIndividual, OWLNegativeObjectPropertyAssertionAxiom, OWLObjectPropertyIndividualPair> {

    public OWLNegativeObjectPropertyAssertionFrameSectionRow(OWLEditorKit kit,
                                                             OWLFrameSection<OWLIndividual, OWLNegativeObjectPropertyAssertionAxiom, OWLObjectPropertyIndividualPair> section,
                                                             OWLOntology ontology,
                                                             OWLIndividual rootObject,
                                                             OWLNegativeObjectPropertyAssertionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLObjectPropertyIndividualPair> getObjectEditor() {
        OWLObjectPropertyIndividualPairEditor2 editor = new OWLObjectPropertyIndividualPairEditor2(getOWLEditorKit());
        editor.setEditedObject(new OWLObjectPropertyIndividualPair(getAxiom().getProperty(), getAxiom().getObject()));
        return editor;
    }

    @Override
    protected OWLNegativeObjectPropertyAssertionAxiom createAxiom(OWLObjectPropertyIndividualPair editedObject) {
        return getOWLDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(editedObject.getProperty(),
                getRoot(), editedObject.getIndividual());
    }
}
