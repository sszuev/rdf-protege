package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owlapi.model.*;
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
public class RemoveLocalDisjointAxiomsAction extends SelectedOWLClassAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveAllDisjointAxiomsAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            UIHelper uiHelper = new UIHelper(getOWLEditorKit());
            OWLModelManager m = getOWLModelManager();
            int result = uiHelper.showOptionPane("Include imported ontologies?",
                    "Do you want to remove the disjoint classes axioms from " +
                            "imported ontologies?",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            Set<OWLOntology> ontologies = new HashSet<>();
            if (result == JOptionPane.YES_OPTION) {
                ontologies.addAll(m.getActiveOntologies());
            } else if (result == JOptionPane.NO_OPTION) {
                ontologies = Collections.singleton(m.getActiveOntology());
            }

            List<OWLOntologyChange> changes = new ArrayList<>();
            for (OWLClass desc : m.getOWLHierarchyManager().getOWLClassHierarchyProvider().getChildren(getOWLClass())) {
                changes.addAll(removeDisjointsForClass(desc, ontologies));
            }
            m.applyChanges(changes);
        } catch (Exception ex) {
            LOGGER.error("An error occurred whilst removing the disjoint axioms from the specified ontologies.", ex);
        }
    }

    private List<OWLOntologyChange> removeDisjointsForClass(OWLClass clazz, Set<OWLOntology> ontologies) {
        List<OWLOntologyChange> res = new ArrayList<>();
        ontologies.forEach(ont -> ont.axioms(AxiomType.DISJOINT_CLASSES)
                .filter(x -> x.contains(clazz))
                .forEach(x -> res.add(new RemoveAxiom(ont, x))));
        return res;
    }

    @Override
    protected void initialiseAction() {
        // do nothing
    }
}