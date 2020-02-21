package org.protege.editor.owl.ui.ontology.wizard.move;

import org.protege.editor.core.ui.wizard.WizardPanel;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 23-Sep-2008<br><br>
 */
public class SelectActionPanel extends AbstractMoveAxiomsWizardPanel {

    public static final String ID = "SelectActionPanel";

    private JRadioButton moveAxiomsButton;
    private JRadioButton copyAxiomsButton;
    private JRadioButton deleteAxiomsButton;

    public SelectActionPanel(OWLEditorKit owlEditorKit) {
        super(ID, "Copy, move or delete axioms", owlEditorKit);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        parent.setLayout(new BorderLayout());

        copyAxiomsButton = new JRadioButton("Copy axioms (to another ontology)", true);
        moveAxiomsButton = new JRadioButton("Move axioms (to another ontology)");
        deleteAxiomsButton = new JRadioButton("Delete axioms");

        final ActionListener actionListener = event -> getWizard().resetButtonStates();
        copyAxiomsButton.addActionListener(actionListener);
        moveAxiomsButton.addActionListener(actionListener);
        deleteAxiomsButton.addActionListener(actionListener);

        ButtonGroup bg = new ButtonGroup();
        bg.add(copyAxiomsButton);
        bg.add(moveAxiomsButton);
        bg.add(deleteAxiomsButton);

        Box box = new Box(BoxLayout.Y_AXIS);
        box.add(copyAxiomsButton);
        box.add(moveAxiomsButton);
        box.add(deleteAxiomsButton);

        parent.add(box);

        setInstructions("Specify whether you want to copy, move or delete the axioms from the source ontology.");
    }

    @Override
    public Object getBackPanelDescriptor() {
        return getWizard().getLastPanelIDForKit();
    }

    @Override
    public Object getNextPanelDescriptor() {
        if (moveAxiomsButton.isSelected() || copyAxiomsButton.isSelected()) {
            return SelectTargetOntologyTypePanel.ID;
        }
        return WizardPanel.FINISH;
    }

    @Override
    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();

    }

    @Override
    public void aboutToHidePanel() {
        super.aboutToHidePanel();
        MoveType t;
        if (deleteAxiomsButton.isSelected()) {
            t = MoveType.DELETE;
        } else if (moveAxiomsButton.isSelected()) {
            t = MoveType.MOVE;
        } else {
            t = MoveType.COPY;
        }
        getWizard().setMoveType(t);
    }
}
