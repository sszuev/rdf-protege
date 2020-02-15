package org.protege.editor.owl.ui.action;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 15-Feb-2007<br><br>
 */
public class ConvertToDefinedClassAction extends SelectedOWLClassAction {

    @Override
    protected void initialiseAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLClass clazz = getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
        if (clazz == null) {
            return;
        }
        List<OWLOntologyChange> changes = new ArrayList<>();
        Set<OWLClassExpression> operands = new HashSet<>();
        getOWLModelManager().getActiveOntologies().forEach(ont -> ont.subClassAxiomsForSubClass(clazz).forEach(ax -> {
            changes.add(new RemoveAxiom(ont, ax));
            operands.add(ax.getSuperClass());
        }));
        if (operands.isEmpty()) {
            return;
        }
        OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
        OWLClassExpression equivalent;
        if (operands.size() == 1) {
            equivalent = operands.iterator().next();
        } else {
            equivalent = df.getOWLObjectIntersectionOf(operands);
        }
        OWLAxiom ax = df.getOWLEquivalentClassesAxiom(CollectionFactory.createSet(clazz, equivalent));
        changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        getOWLModelManager().applyChanges(changes);
    }
}
