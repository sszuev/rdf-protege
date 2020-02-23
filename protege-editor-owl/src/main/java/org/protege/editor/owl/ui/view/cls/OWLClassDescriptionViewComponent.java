package org.protege.editor.owl.ui.view.cls;

import org.protege.editor.owl.ui.frame.cls.OWLClassDescriptionFrame;
import org.protege.editor.owl.ui.frame.cls.OWLSubClassAxiomFrameSectionRow;
import org.protege.editor.owl.ui.framelist.CreateClosureAxiomAction;
import org.protege.editor.owl.ui.framelist.CreateNewEquivalentClassAction;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.protege.editor.owl.ui.framelist.OWLFrameListPopupMenuAction;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 04-Feb-2007<br><br>
 */
public class OWLClassDescriptionViewComponent extends AbstractOWLClassViewComponent {
    private static final long serialVersionUID = -7899828024396593253L;
    private OWLFrameList<OWLClass> list;

    @Override
    public void initialiseClassView() {
        list = new OWLFrameList<>(getOWLEditorKit(), new OWLClassDescriptionFrame(getOWLEditorKit()));
        setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(list);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp);
        list.addToPopupMenu(new ConvertSelectionToEquivalentClassAction());
        list.addToPopupMenu(new CreateNewEquivalentClassAction<>());
        list.addToPopupMenu(new CreateClosureAxiomAction());
    }

    /**
     * This method is called to request that the view is updated with the specified class.
     *
     * @param selectedClass The class that the view should be updated with.
     *                      Note that this may be <code>null</code>, which indicates that the view should be cleared
     * @return The actual class that the view is displaying after it has been updated (may be <code>null</code>)
     */
    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        list.setRootObject(selectedClass);
        return selectedClass;
    }

    @Override
    public void disposeView() {
        list.dispose();
    }


    private class ConvertSelectionToEquivalentClassAction extends OWLFrameListPopupMenuAction<OWLClass> {
        @Override
        protected void initialise() throws Exception {
        }

        @Override
        protected void dispose() throws Exception {
        }

        @Override
        protected String getName() {
            return "Convert selected rows to defined class";
        }

        @Override
        protected void updateState() {
            if (list.getSelectedValue() == null) {
                setEnabled(false);
                return;
            }
            for (Object selVal : list.getSelectedValuesList()) {
                if (!(selVal instanceof OWLSubClassAxiomFrameSectionRow)) {
                    setEnabled(false);
                    return;
                }
            }
            setEnabled(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            convertSelectedRowsToDefinedClass();
        }
    }

    private void convertSelectedRowsToDefinedClass() {
        List<Object> selVals = list.getSelectedValuesList();
        if (selVals.isEmpty()) {
            return;
        }
        Set<OWLClassExpression> descriptions = new HashSet<>();
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (Object selVal : selVals) {
            if (selVal instanceof OWLSubClassAxiomFrameSectionRow) {
                OWLSubClassAxiomFrameSectionRow row = (OWLSubClassAxiomFrameSectionRow) selVal;
                changes.add(new RemoveAxiom(row.getOntology(), row.getAxiom()));
                descriptions.add(row.getAxiom().getSuperClass());
            }
        }
        OWLClassExpression equivalentClass = descriptions.size() == 1
                ? descriptions.iterator().next() : getOWLDataFactory().getOWLObjectIntersectionOf(descriptions);
        Set<OWLClassExpression> axiomOperands = CollectionFactory.createSet(list.getRootObject(), equivalentClass);
        changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(),
                getOWLDataFactory().getOWLEquivalentClassesAxiom(axiomOperands)));
        getOWLModelManager().applyChanges(changes);
    }
}
