package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLIndividualSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLDifferentIndividualAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLNamedIndividual, OWLDifferentIndividualsAxiom, Set<OWLNamedIndividual>> {

    public OWLDifferentIndividualAxiomFrameSectionRow(OWLEditorKit kit,
                                                      OWLFrameSection<OWLNamedIndividual, OWLDifferentIndividualsAxiom, Set<OWLNamedIndividual>> section,
                                                      OWLOntology ontology, OWLNamedIndividual root,
                                                      OWLDifferentIndividualsAxiom axiom) {
        super(kit, section, ontology, root, axiom);
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
    protected OWLDifferentIndividualsAxiom createAxiom(Set<OWLNamedIndividual> editedObject) {
        editedObject.add(getRoot());
        return getOWLDataFactory().getOWLDifferentIndividualsAxiom(editedObject);
    }

    @Override
    public Stream<OWLNamedIndividual> manipulatableObjects() {
        //@@TODO v3 port - what about anon indivs?
        OWLIndividual root = getRoot();
        return getAxiom().individuals()
                .filter(i -> !i.isAnonymous() && !i.equals(root))
                .map(AsOWLNamedIndividual::asOWLNamedIndividual);
    }
}
