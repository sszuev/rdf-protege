package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLIndividualSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.AsOWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLSameIndividualsAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLNamedIndividual, OWLSameIndividualAxiom, Set<OWLNamedIndividual>> {

    public OWLSameIndividualsAxiomFrameSectionRow(OWLEditorKit owlEditorKit,
                                                  OWLFrameSection<OWLNamedIndividual, OWLSameIndividualAxiom, Set<OWLNamedIndividual>> section,
                                                  OWLOntology ontology,
                                                  OWLNamedIndividual rootObject,
                                                  OWLSameIndividualAxiom axiom) {
        super(owlEditorKit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<Set<OWLNamedIndividual>> getObjectEditor() {
        return new OWLIndividualSetEditor(getOWLEditorKit());
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLNamedIndividual>> editor) {
        return !Objects.requireNonNull(editor.getEditedObject()).contains(getRoot());
    }

    @Override
    protected OWLSameIndividualAxiom createAxiom(Set<OWLNamedIndividual> editedObject) {
        editedObject.add(getRoot());
        return getOWLDataFactory().getOWLSameIndividualAxiom(editedObject);
    }

    @Override
    public Stream<OWLNamedIndividual> manipulatableObjects() {
        //@@TODO v3 port - what about anon indivs?
        return getAxiom().individuals()
                .filter(i -> !i.isAnonymous() && !i.equals(getRoot()))
                .map(AsOWLNamedIndividual::asOWLNamedIndividual);
    }
}
