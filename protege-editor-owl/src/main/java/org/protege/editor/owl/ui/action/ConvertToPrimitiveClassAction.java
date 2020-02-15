package org.protege.editor.owl.ui.action;

import org.semanticweb.owlapi.model.*;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 24-Aug-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ConvertToPrimitiveClassAction extends SelectedOWLClassAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO: Factor this out into some kind of API util
        OWLClass clazz = getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass();
        if (clazz == null) {
            return;
        }
        OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
        List<OWLOntologyChange> changes = new ArrayList<>();
        getOWLModelManager().getActiveOntologies().forEach(ont -> ont.equivalentClassesAxioms(clazz).forEach(ax -> {
            changes.add(new RemoveAxiom(ont, ax));
            ax.classExpressions().filter(desc -> !desc.equals(clazz)).forEach(desc -> {
                if (desc instanceof OWLObjectIntersectionOf) {
                    ((OWLObjectIntersectionOf) desc).operands()
                            .map(op -> new AddAxiom(ont, df.getOWLSubClassOfAxiom(clazz, op)))
                            .forEach(changes::add);
                    return;
                }
                changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(clazz, desc)));
            });
        }));
        getOWLModelManager().applyChanges(changes);
    }

    @Override
    protected void initialiseAction() {
    }

    @Override
    public void dispose() {
    }
}
