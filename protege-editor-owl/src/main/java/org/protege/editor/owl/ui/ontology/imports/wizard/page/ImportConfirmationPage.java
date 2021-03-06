package org.protege.editor.owl.ui.ontology.imports.wizard.page;

import org.protege.editor.core.ui.wizard.WizardPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.AbstractOWLWizardPanel;
import org.protege.editor.owl.ui.ontology.imports.wizard.ImportInfo;
import org.protege.editor.owl.ui.ontology.imports.wizard.OntologyImportWizard;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 13-Jun-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ImportConfirmationPage extends AbstractOWLWizardPanel {
    private static final long serialVersionUID = -8146050890918692126L;

    public static final String ID = "ImportConfirmationPage";

    private JComponent importedOntologiesComponent;

    private Object backPanelDescriptor;

    public ImportConfirmationPage(OWLEditorKit owlEditorKit) {
        super(ID, "Confirm imports", owlEditorKit);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        setInstructions("The system will import the following ontologies. " +
                "Press Finish to import these ontologies, " +
                "or Cancel to exit the wizard without importing any ontologies.");
        importedOntologiesComponent = new JPanel(new BorderLayout());
        parent.setLayout(new BorderLayout());
        parent.add(importedOntologiesComponent, BorderLayout.NORTH);
    }

    @Override
    public void displayingPanel() {
        super.displayingPanel();
        fillImportList();
    }

    private void fillImportList() {
        importedOntologiesComponent.removeAll();
        Box box = new Box(BoxLayout.Y_AXIS);
        boolean advanced = ((OntologyImportWizard) getWizard()).isCustomizeImports();
        Set<ImportInfo> parameters = ((OntologyImportWizard) getWizard()).getImports();
        for (ImportInfo parameter : parameters) {
            if (!parameter.isReady()) {
                continue;
            }
            if (advanced) {
                ImportEntryPanel importPanel = new ImportEntryPanel(parameter);
                box.add(importPanel);
            } else {
                box.add(new JLabel(parameter.getPhysicalLocation().toString()));
            }
        }
        importedOntologiesComponent.add(box, BorderLayout.NORTH);
    }

    public void setBackPanelDescriptor(Object backPanelDescriptor) {
        this.backPanelDescriptor = backPanelDescriptor;
    }

    @Override
    public Object getBackPanelDescriptor() {
        if (((OntologyImportWizard) getWizard()).isCustomizeImports()) {
            return SelectImportLocationPage.ID;
        } else {
            return backPanelDescriptor;
        }
    }

    @Override
    public Object getNextPanelDescriptor() {
        return WizardPanel.FINISH;
    }

    private static class ImportEntryPanel extends JPanel {
        private static final long serialVersionUID = -4945897856195350142L;

        public ImportEntryPanel(ImportInfo parameter) {
            setBorder(BorderFactory.createEmptyBorder(1, 0, 4, 0));
            setLayout(new BorderLayout(1, 1));
            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            JLabel physicalLocationLabel = new JLabel("Load import from " + parameter.getPhysicalLocation().toString());
            physicalLocationLabel.setFont(physicalLocationLabel.getFont().deriveFont(10.0f));
            physicalLocationLabel.setAlignmentX(LEFT_ALIGNMENT);
            center.add(physicalLocationLabel);

            OWLOntologyID id = parameter.getOntologyID();
            if (!id.isAnonymous()) {
                IRI iri = id.getOntologyIRI().orElseThrow(IllegalStateException::new);
                JLabel ontologyNameLabel = new JLabel("Imported Ontology Name " + iri);
                ontologyNameLabel.setAlignmentX(LEFT_ALIGNMENT);
                center.add(ontologyNameLabel);

                if (id.getVersionIRI().isPresent()) {
                    JLabel ontologyVersionLabel = new JLabel("Imported Ontology Version " + id.getVersionIRI());
                    ontologyVersionLabel.setAlignmentX(LEFT_ALIGNMENT);
                    center.add(ontologyVersionLabel);
                }
            } else {
                JLabel anonymousLabel = new JLabel("Imported Ontology is anonymous.");
                anonymousLabel.setAlignmentX(LEFT_ALIGNMENT);
                center.add(anonymousLabel);
            }

            center.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
            Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
            TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder,
                    "Import Declaration: " + parameter.getImportLocation().toString());
            setBorder(titledBorder);
            add(center, BorderLayout.CENTER);
        }
    }
}
