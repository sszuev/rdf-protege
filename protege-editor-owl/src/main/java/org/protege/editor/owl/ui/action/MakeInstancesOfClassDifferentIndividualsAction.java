package org.protege.editor.owl.ui.action;

import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 31 Aug 16
 */
public class MakeInstancesOfClassDifferentIndividualsAction extends SelectedOWLClassAction {

    @Override
    protected void initialiseAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLClass selectedClass = getOWLClass();
        if (selectedClass == null) {
            return;
        }

        Set<OWLIndividual> individuals = getOWLModelManager().getActiveOntologies().stream()
                .flatMap(o -> o.classAssertionAxioms(selectedClass))
                .map(OWLClassAssertionAxiom::getIndividual)
                .filter(i -> !i.isAnonymous())
                .map(OWLIndividual::asOWLNamedIndividual)
                .collect(toSet());

        OWLOntology activeOntology = getOWLModelManager().getActiveOntology();

        List<OWLOntologyChange> removeExistingAxiomsChanges = activeOntology
                .axioms(AxiomType.DIFFERENT_INDIVIDUALS)
                .filter(x -> x.individuals().allMatch(individuals::contains))
                .map(x -> new RemoveAxiom(activeOntology, x))
                .collect(toList());

        List<OWLOntologyChange> allChanges = new ArrayList<>();
        if (!removeExistingAxiomsChanges.isEmpty()) {
            int res = JOptionPane.showConfirmDialog(getWorkspace(),
                    "Do you want to remove existing Different Individuals axioms which assert that\n" +
                            "some instances of the selected class are different?",
                    "Remove existing axioms", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                allChanges.addAll(removeExistingAxiomsChanges);
            }
        }
        OWLDifferentIndividualsAxiom ax = getOWLDataFactory().getOWLDifferentIndividualsAxiom(individuals);
        allChanges.add(new AddAxiom(activeOntology, ax));
        getOWLModelManager().applyChanges(allChanges);
    }
}
