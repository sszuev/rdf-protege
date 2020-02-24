package org.protege.editor.owl.ui.view.individual;

import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

/**
 * Only shows the members of the currently selected class
 * <p>
 * TODO - this class should probably no longer extend OWLIndividualListViewComponent.
 *  there are too many hacks piling up.
 *  It is not really selectable in the usual sense.
 *  It completely overrides the process changes methods.
 *  It should display anonymous individuals.
 *
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 14, 2008<br><br>
 */
public class OWLMembersListViewComponent extends OWLIndividualListViewComponent {

    private final JLabel typeLabel = new JLabel();

    private final OWLSelectionModelListener listener = () -> {
        if (getOWLWorkspace().getOWLSelectionModel().getSelectedObject() instanceof OWLClass) {
            refill();
        }
    };

    @Override
    public void initialiseIndividualsView() throws Exception {
        super.initialiseIndividualsView();
        getOWLWorkspace().getOWLSelectionModel().addListener(listener);
        JComponent typePanel = new Box(BoxLayout.X_AXIS);
        typePanel.add(new JLabel("For: "));
        typePanel.add(typeLabel);
        typePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
        add(typePanel, BorderLayout.NORTH);
    }

    @Override
    protected void refill() {
        Set<OWLNamedIndividual> individuals = new HashSet<>();
        OWLClass clazz = getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
        if (clazz != null) {
            typeLabel.setText(getRendering(clazz));
            typeLabel.setIcon(getOWLWorkspace().getOWLIconProvider().getIcon(clazz));
            EntitySearcher.getIndividuals(clazz, getOntologies().stream())
                    .filter(i -> !i.isAnonymous())
                    .forEach(i -> individuals.add(i.asOWLNamedIndividual()));
            if (clazz.isOWLThing()) {
                untypedIndividuals().forEach(individuals::add);
            }
        } else {
            typeLabel.setIcon(null);
            typeLabel.setText("Nothing selected");
            untypedIndividuals().forEach(individuals::add);
        }
        this.individualsInList = individuals;
        reset();
    }

    //TODO: do we want to cache this?
    protected Stream<OWLNamedIndividual> untypedIndividuals() {
        OWLOntology ont = getOWLModelManager().getActiveOntology();
        return ont.individualsInSignature(Imports.INCLUDED)
                .filter(i -> !EntitySearcher.getTypes(i, ont.importsClosure()).findFirst().isPresent());
    }

    @Override
    protected void processChanges(List<? extends OWLOntologyChange> changes) {
        refill(); // TODO for now this is ok - but things are bad
    }

    @Override
    protected List<OWLOntologyChange> dofurtherCreateSteps(OWLIndividual newIndividual) {
        OWLClass cls = getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
        if (cls != null && !cls.isOWLThing()) {
            OWLAxiom typeAxiom = getOWLModelManager().getOWLDataFactory().getOWLClassAssertionAxiom(cls, newIndividual);
            OWLOntologyChange change = new AddAxiom(getOWLModelManager().getActiveOntology(), typeAxiom);
            return Collections.singletonList(change);
        }
        return new ArrayList<>();
    }

    @Override
    public void disposeView() {
        getOWLWorkspace().getOWLSelectionModel().removeListener(listener);
        super.disposeView();
    }
}
