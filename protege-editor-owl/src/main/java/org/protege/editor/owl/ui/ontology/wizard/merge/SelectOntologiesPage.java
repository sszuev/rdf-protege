package org.protege.editor.owl.ui.ontology.wizard.merge;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.AbstractOWLWizardPanel;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 02-Jul-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class SelectOntologiesPage extends AbstractOWLWizardPanel {

    public static final String ID = "SelectOntologiesPage";

    private JList<OWLOntology> list;

    public SelectOntologiesPage(OWLEditorKit owlEditorKit, String title) {
        super(ID, title, owlEditorKit);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    protected void createUI(JComponent parent) {
        parent.setLayout(new BorderLayout());
        list = new JList<>();
        list.setVisibleRowCount(8);
        list.setCellRenderer(getOWLEditorKit().getWorkspace().createOWLCellRenderer());
        OWLModelManager m = getOWLModelManager();
        list.setListData(m.getOntologies().stream().sorted(m.getOWLObjectComparator()).toArray(OWLOntology[]::new));
        parent.add(new JScrollPane(list), BorderLayout.NORTH);
    }

    @Override
    public Object getNextPanelDescriptor() {
        return MergeTypePage.ID;
    }

    @Override
    public void displayingPanel() {
        super.displayingPanel();
        if (list.getSelectedValue() != null) {
            list.requestFocus();
            return;
        }
        Set<OWLOntology> defOnts = getDefaultOntologies();
        for (int i = 0; i < list.getModel().getSize(); i++) {
            if (defOnts.contains(list.getModel().getElementAt(i))) {
                list.addSelectionInterval(i, i);
            }
        }
        list.requestFocus();
    }

    /**
     * Override to set the ontologies that are first shown
     *
     * @return the set of ontologies that should be selected the first time this page is shown
     */
    protected Set<OWLOntology> getDefaultOntologies() {
        return Collections.singleton(getOWLModelManager().getActiveOntology());
    }

    public Set<OWLOntology> getOntologies() {
        return new HashSet<>(list.getSelectedValuesList());
    }
}
