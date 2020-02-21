package org.protege.editor.owl.ui.ontology.wizard.move;

import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * User: nickdrummond Date: May 20, 2008
 */
public class SelectKitPanel extends AbstractMoveAxiomsWizardPanel {

    public static final String ID = "AxiomSelectionStrategyPanel";
    public static final int DEFAULT_PREFERRED_WIDTH = 1200;
    public static final int DEFAULT_PREFERRED_HEIGHT = 800;

    private ButtonGroup bGroup;
    private JPanel holder;
    private boolean createdButtons;

    public SelectKitPanel(OWLEditorKit editorKit) {
        super(ID, "Select method", editorKit);
        setInstructions("Select the method of choosing axioms from the source ontology.");
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_PREFERRED_WIDTH, DEFAULT_PREFERRED_HEIGHT);
    }

    public void aboutToDisplayPanel() {
        createStrategyRadioButtons();
    }

    private void createStrategyRadioButtons() {
        if (createdButtons) {
            return;
        }
        for (MoveAxiomsKit kit : getWizard().getMoveAxiomsKits()) {
            JRadioButton cb = new JRadioButton(new AbstractAction(kit.getName()) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    getWizard().setSelectedKit(kit);
                }
            });
            cb.setOpaque(false);
            cb.setSelected(getWizard().getSelectedKit() == kit);
            bGroup.add(cb);
            holder.add(cb);
        }
        createdButtons = true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        parent.setLayout(new BorderLayout());
        holder = new JPanel();
        holder.setOpaque(false);
        holder.setLayout(new BoxLayout(holder, BoxLayout.PAGE_AXIS));
        bGroup = new ButtonGroup();
        parent.add(holder, BorderLayout.NORTH);
    }

    @Override
    public Object getBackPanelDescriptor() {
        return SelectSourceOntologiesPanel.ID;
    }

    @Override
    public Object getNextPanelDescriptor() {
        return getWizard().getFirstPanelIDForKit();
    }
}
