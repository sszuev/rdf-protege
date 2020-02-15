package org.protege.editor.owl.ui.ontology.imports.missing;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.AbstractOWLWizardPanel;

import javax.swing.*;
import java.awt.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 17-Oct-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class CopyOptionPanel extends AbstractOWLWizardPanel {

    public static final String ID = "CopyOptionPanel";

    public CopyOptionPanel(OWLEditorKit owlEditorKit) {
        super(ID, "Copy file to root ontology folder?", owlEditorKit);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        setInstructions("Would you like to copy the file to to root ontology folder? "
                + "Ontologies are only editable if they are loaded from the same folder that the root importing "
                + "ontology was loaded from.");

        JCheckBox copyCheckBox = new JCheckBox("Copy to imports root folder", true);
        parent.setLayout(new BorderLayout());
        parent.add(copyCheckBox, BorderLayout.NORTH);
    }

    @Override
    public Object getBackPanelDescriptor() {
        return SpecifyFilePathPanel.ID;
    }
}
