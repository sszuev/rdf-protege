package org.protege.editor.owl.ui.action;

import org.protege.editor.core.ui.view.View;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.util.OWLEntityDeleter;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 22-Feb-2007<br><br>
 */
@Deprecated // todo: unused -> delete
public class DeleteEntityAction extends SelectedOWLEntityAction {


    protected void actionPerformed(OWLEntity selectedEntity) {
        boolean containsReferences = false;
        for (OWLOntology ont : getOWLModelManager().getOntologies()) {
            Set<OWLAxiom> referencingAxioms = ont.getReferencingAxioms(selectedEntity);
            for (OWLAxiom ax : referencingAxioms) {
                if (!(ax instanceof OWLDeclarationAxiom)) {
                    containsReferences = true;
                    break;
                }
            }
        }
        if (containsReferences) {
            int ret = showUsageConfirmationDialog();
            if (ret == 1) {
                showUsage();
            }
            else if (ret == 0) {
                deleteEntity();
            }
        }
        else {
            int ret = showConfirmationDialog();
            if (ret == JOptionPane.YES_NO_OPTION) {
                deleteEntity();
            }
        }
    }


    private int showConfirmationDialog() {
        String rendering = getOWLModelManager().getRendering(getSelectedEntity());
        return JOptionPane.showConfirmDialog(getOWLWorkspace(),
                                             "Delete " + rendering + "?",
                                             "Really delete?",
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.WARNING_MESSAGE);
    }


    private int showUsageConfirmationDialog() {
        String rendering = getOWLModelManager().getRendering(getSelectedEntity());
        Object [] OPTIONS = {"Delete", "View usage", "Cancel"};
        return JOptionPane.showOptionDialog(getOWLWorkspace(),
                                            rendering + " is used throught the loaded ontologies.  Delete anyway?",
                                            "Entity is referenced!",
                                            JOptionPane.DEFAULT_OPTION,
                                            JOptionPane.WARNING_MESSAGE,
                                            null,
                                            OPTIONS,
                                            OPTIONS[1]);
    }


    private void deleteEntity() {
        OWLEntityDeleter.deleteEntities(Collections.singleton(getSelectedEntity()), getOWLModelManager());
    }


    private void showUsage() {
        OWLEntity ent = getSelectedEntity();
        ent.accept(new OWLEntityVisitor() {
            public void visit(OWLClass cls) {
                View view = getOWLWorkspace().showResultsView("OWLClassUsageView",
                                                              true,
                                                              OWLWorkspace.BOTTOM_RESULTS_VIEW);
                view.setPinned(true);
            }


            public void visit(OWLDatatype dataType) {
            }


            public void visit(OWLAnnotationProperty owlAnnotationProperty) {
                View view = getOWLWorkspace().showResultsView("OWLAnnotationPropertyUsageView",
                                                              true,
                                                              OWLWorkspace.BOTTOM_RESULTS_VIEW);
                view.setPinned(true);
            }


            public void visit(OWLNamedIndividual individual) {
                View view = getOWLWorkspace().showResultsView("OWLIndividualUsageView",
                                                              true,
                                                              OWLWorkspace.BOTTOM_RESULTS_VIEW);
                view.setPinned(true);
            }


            public void visit(OWLDataProperty property) {
                View view = getOWLWorkspace().showResultsView("OWLDataPropertyUsageView",
                                                              true,
                                                              OWLWorkspace.BOTTOM_RESULTS_VIEW);
                view.setPinned(true);
            }


            public void visit(OWLObjectProperty property) {
                View view = getOWLWorkspace().showResultsView("OWLObjectPropertyUsageView",
                                                              true,
                                                              OWLWorkspace.BOTTOM_RESULTS_VIEW);
                view.setPinned(true);
            }
        });
    }


    protected void disposeAction() throws Exception {
    }
}
