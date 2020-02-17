package org.protege.editor.owl.ui.hierarchy.creation;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.AbstractOWLWizardPanel;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import java.awt.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 17-Jul-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class PickRootClassPanel extends AbstractOWLWizardPanel {
    private static final long serialVersionUID = 3010893357248469815L;
    public static final String ID = "PickRootClassPanel";

    private OWLModelManagerTree<OWLClass> tree;

    public PickRootClassPanel(OWLEditorKit owlEditorKit) {
        super(ID, "Pick root class", owlEditorKit);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        parent.setLayout(new BorderLayout());
        setInstructions("Please select the root class");
        tree = new OWLModelManagerTree<>(getOWLEditorKit(),
                getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider());
        tree.setSelectedObject(getOWLEditorKit().getWorkspace().getOWLSelectionModel().getLastSelectedClass());
        parent.add(ComponentFactory.createScrollPane(tree));
    }

    @Override
    public void displayingPanel() {
        tree.requestFocus();
    }

    public OWLClass getRootClass() {
        OWLClass cls = tree.getSelectedObject();
        return cls == null ? getOWLModelManager().getOWLDataFactory().getOWLThing() : tree.getSelectedObject();
    }

    @Override
    public Object getNextPanelDescriptor() {
        return TabIndentedHierarchyPanel.ID;
    }

    public void dispose() {
        tree.dispose();
    }
}
