package org.protege.editor.owl.ui.framelist;

import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.ui.CreateDefinedClassPanel;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;

import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 24, 2008<br><br>
 */
public class CreateNewEquivalentClassAction<C extends OWLObject> extends OWLFrameListPopupMenuAction<C> {

    @Override
    protected String getName() {
        return "Create new defined class";
    }

    @Override
    protected void initialise() throws Exception {
    }

    @Override
    protected void dispose() throws Exception {
    }

    private OWLClassExpression getSelectedRowDescription() {
        Object selVal = getFrameList().getSelectedValue();
        if (!(selVal instanceof OWLFrameSectionRow)) {
            return null;
        }
        Set<OWLObject> objects = ((OWLFrameSectionRow<?, ?, ?>) selVal)
                .manipulatableObjects().limit(2)
                .collect(Collectors.toSet());
        if (objects.size() == 1) {
            Object o = objects.iterator().next();
            if (o instanceof OWLClassExpression && ((OWLClassExpression) o).isAnonymous()) {
                return (OWLClassExpression) o;
            }
        }
        return null;
    }

    @Override
    protected void updateState() {
        setEnabled(getSelectedRowDescription() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLClassExpression selected = getSelectedRowDescription();
        if (selected == null) {
            return;
        }
        OWLEntityCreationSet<OWLClass> creationSet = CreateDefinedClassPanel.showDialog(selected, getOWLEditorKit());
        if (creationSet == null) {
            return;
        }
        getOWLModelManager().applyChanges(creationSet.getOntologyChanges());
        getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(creationSet.getOWLEntity());
    }
}
