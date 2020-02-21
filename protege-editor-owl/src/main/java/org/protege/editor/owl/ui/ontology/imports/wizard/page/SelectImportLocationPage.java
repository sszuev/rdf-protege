package org.protege.editor.owl.ui.ontology.imports.wizard.page;

import org.protege.editor.core.ui.wizard.AbstractWizardPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.ontology.imports.wizard.ImportInfo;
import org.protege.editor.owl.ui.ontology.imports.wizard.ImportLocationOptionsPanel;
import org.protege.editor.owl.ui.ontology.imports.wizard.OntologyImportWizard;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class SelectImportLocationPage extends AbstractWizardPanel {
    public static final String ID = "SelectImportLocationPage";

    private Box mainBox;
    private Collection<ImportLocationOptionsPanel> optionsPanels = new ArrayList<>();
    private Object backPanelDescriptor;

    public SelectImportLocationPage(OWLEditorKit owlEditorKit) {
        super(ID, "Select URI In Import Statement", owlEditorKit);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        setInstructions("Please choose a value for the imported location:");

        parent.setLayout(new BorderLayout());
        mainBox = new Box(BoxLayout.Y_AXIS);
        parent.add(mainBox, BorderLayout.CENTER);
    }

    @Override
    public void aboutToDisplayPanel() {
        mainBox.removeAll();
        optionsPanels.clear();
        for (ImportInfo parameter : ((OntologyImportWizard) getWizard()).getImports()) {
            OWLOntologyID id = parameter.getOntologyID();
            if (id == null) {
                continue;
            }
            ImportLocationOptionsPanel optionsPanel = new ImportLocationOptionsPanel(parameter);
            Border titledBorder = BorderFactory.createTitledBorder("Physical Location: " + parameter.getPhysicalLocation());
            optionsPanel.setBorder(titledBorder);
            mainBox.add(optionsPanel);
            optionsPanels.add(optionsPanel);
        }
    }

    @Override
    public void displayingPanel() {
        if (optionsPanels.size() == 0) {
            getWizard().setCurrentPanel(getNextPanelDescriptor());
        }
    }

    @Override
    public void aboutToHidePanel() {
        for (ImportLocationOptionsPanel optionsPanel : optionsPanels) {
            optionsPanel.setImportLocation();
        }
    }

    public void setBackPanelDescriptor(Object backPanelDescriptor) {
        this.backPanelDescriptor = backPanelDescriptor;
    }

    @Override
    public Object getBackPanelDescriptor() {
        return backPanelDescriptor;
    }

    public Object getNextPanelDescriptor() {
        return ImportConfirmationPage.ID;
    }
}
