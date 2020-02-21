package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 30-Jun-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class RemoveAllDisjointAxiomsAction extends ProtegeOWLAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveAllDisjointAxiomsAction.class);

    private final OWLModelManagerListener listener = event -> {
        if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)) {
            updateState();
        }
    };

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            UIHelper uiHelper = new UIHelper(getOWLEditorKit());
            int result = uiHelper.showOptionPane("Remove axioms from imported ontologies?",
                    "Do you want to remove the disjoint classes axioms from imported ontologies?",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            Set<OWLOntology> ontologies = new HashSet<>();
            if (result == JOptionPane.YES_OPTION) {
                ontologies.addAll(getOWLModelManager().getActiveOntologies());
            } else if (result == JOptionPane.NO_OPTION) {
                ontologies = Collections.singleton(getOWLModelManager().getActiveOntology());
            }
            List<OWLOntologyChange> changes = new ArrayList<>();
            for (OWLOntology ont : ontologies) {
                ont.axioms(AxiomType.DISJOINT_CLASSES).map(ax -> new RemoveAxiom(ont, ax)).forEach(changes::add);
            }
            getOWLModelManager().applyChanges(changes);
        } catch (Exception ex) {
            LOGGER.error("An error occurred whilst attempting to remove all disjoint classes axioms.", ex);
        }
    }

    private void updateState() {
        setEnabled(getOWLModelManager().isActiveOntologyMutable());
    }

    @Override
    public void initialise() throws Exception {
        getOWLModelManager().addListener(listener);
        updateState();
    }

    @Override
    public void dispose() {
        getOWLModelManager().removeListener(listener);
    }
}
