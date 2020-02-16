package org.protege.editor.owl.ui.framelist;

import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 18-Dec-2007<br><br>
 */
public class MoveAxiomsToOntologyAction<R> extends OWLFrameListPopupMenuAction<R> {

    @Override
    protected String getName() {
        return "Move axiom(s) to ontology...";
    }

    @Override
    protected void initialise() throws Exception {
    }

    @Override
    protected void dispose() throws Exception {
    }

    @Override
    protected void updateState() {
        setEnabled(getState());
    }

    private boolean getState() {
        List<?> list = getFrameList().getSelectedValuesList();
        if (list.isEmpty()) return false;
        for (Object val : list) {
            if (!(val instanceof OWLFrameSectionRow)) {
                return false;
            }
            if (((OWLFrameSectionRow<?, ?, ?>) val).getOntology() == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLOntology ont = new UIHelper(getOWLEditorKit()).pickOWLOntology();
        if (ont != null) {
            moveAxiomsToOntology(ont);
        }
    }

    private void moveAxiomsToOntology(OWLOntology ontology) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLFrameSectionRow<?, ?, ?> row : getSelectedRows()) {
            OWLAxiom ax = row.getAxiom();
            OWLOntology currentOnt = row.getOntology();
            changes.add(new RemoveAxiom(currentOnt, ax));
            changes.add(new AddAxiom(ontology, ax));
        }
        getOWLModelManager().applyChanges(changes);
    }
}
