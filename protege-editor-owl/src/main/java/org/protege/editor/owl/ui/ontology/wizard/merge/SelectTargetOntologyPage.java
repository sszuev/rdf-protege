package org.protege.editor.owl.ui.ontology.wizard.merge;

import org.protege.editor.core.ui.wizard.WizardPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.AbstractOWLWizardPanel;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 02-Jul-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class SelectTargetOntologyPage extends AbstractOWLWizardPanel {

    public static final String ID = "SelectTargetOntologyPage";

    private JList<Object> list;

    public SelectTargetOntologyPage(OWLEditorKit owlEditorKit, String title) {
        super(ID, title, owlEditorKit);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    protected void createUI(JComponent parent) {
        parent.setLayout(new BorderLayout());
        list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(8);
        OWLModelManager m = getOWLModelManager();
        list.setCellRenderer(getOWLEditorKit().getWorkspace().createOWLCellRenderer());
        list.setListData(m.getOntologies().stream().sorted(m.getOWLObjectComparator()).toArray());
        parent.add(new JScrollPane(list), BorderLayout.NORTH);
    }

    @Override
    public Object getNextPanelDescriptor() {
        return WizardPanel.FINISH;
    }

    @Override
    public void displayingPanel() {
        super.displayingPanel();
        list.requestFocus();
    }

    public OWLOntology getOntology() {
        return (OWLOntology) list.getSelectedValue();
    }
}