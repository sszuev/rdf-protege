package org.protege.editor.owl.ui.ontology.wizard;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.AbstractOWLWizardPanel;
import org.protege.editor.owl.ui.ontology.wizard.merge.MergeTypePage;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>The University Of Manchester<br> Information Management Group<br> Date: 23-Sep-2008<br><br>
 */
public class AbstractSelectOntologiesPage extends AbstractOWLWizardPanel {

    private JList<Object> list;

    public AbstractSelectOntologiesPage(Object ID, OWLEditorKit owlEditorKit, String title) {
        super(ID, title, owlEditorKit);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    protected final void createUI(JComponent parent) {
        parent.setLayout(new BorderLayout());
        list = new JList<>();
        list.setVisibleRowCount(8);
        list.setCellRenderer(getOWLEditorKit().getWorkspace().createOWLCellRenderer());
        java.util.List<OWLOntology> orderedOntologies = new ArrayList<>(getOWLModelManager().getOntologies());
        orderedOntologies.sort(getOWLModelManager().getOWLObjectComparator());
        list.setListData(orderedOntologies.toArray());
        parent.add(new JScrollPane(list), BorderLayout.NORTH);
        updateSelectionMode();
        list.addListSelectionListener(event -> handleSelectionChanged());
    }

    private void handleSelectionChanged() {
        getWizard().setNextFinishButtonEnabled(!getOntologies().isEmpty());
    }

    private void updateSelectionMode() {
        list.getSelectionModel().setSelectionMode(isMultiSelect() ?
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
    }

    protected boolean isMultiSelect() {
        return true;
    }

    @Override
    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();
        updateSelectionMode();
    }

    @Override
    public Object getNextPanelDescriptor() {
        return MergeTypePage.ID;
    }

    @Override
    public void displayingPanel() {
        super.displayingPanel();
        if (list.getSelectedValue() == null) {
            Set<?> defOnts = getDefaultOntologies();
            for (int i = 0; i < list.getModel().getSize(); i++) {
                Object e = list.getModel().getElementAt(i);
                if (defOnts.contains(e)) {
                    list.addSelectionInterval(i, i);
                }
            }
        }
        list.requestFocus();
    }

    /**
     * Overrides to set the ontologies that are first shown.
     *
     * @return the set of ontologies that should be selected the first time this page is shown
     */
    protected Set<OWLOntology> getDefaultOntologies() {
        return Collections.singleton(getOWLModelManager().getActiveOntology());
    }

    public Set<OWLOntology> getOntologies() {
        Set<OWLOntology> ontologies = new HashSet<>();
        for (Object o : list.getSelectedValuesList()) {
            ontologies.add((OWLOntology) o);
        }
        return ontologies;
    }

}
