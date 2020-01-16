package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import java.awt.event.ActionEvent;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 03-Jul-2006<br><br>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class AddCoveringAxiomAction extends SelectedOWLClassAction {

    @Override
    protected void initialiseAction() {
    }

    @Override
    protected void updateState() {
        OWLClass selectedClass = getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
        setEnabled(selectedClass != null && getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider()
                .getChildren(selectedClass).size() > 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLModelManager m = getOWLModelManager();
        OWLDataFactory df = getOWLDataFactory();
        OWLClass clazz = getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
        if (clazz == null) {
            throw new IllegalStateException();
        }
        Set<OWLClass> children = m.getOWLHierarchyManager().getOWLClassHierarchyProvider().getChildren(clazz);
        OWLClassExpression coveringDesc = df.getOWLObjectUnionOf(children);
        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(clazz, coveringDesc);
        m.applyChange(new AddAxiom(m.getActiveOntology(), ax));
    }

    @Override
    public void dispose() {
    }
}
