package org.protege.editor.owl.ui.ontology.wizard.move;

import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import java.awt.*;

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 19-Sep-2008<br><br>
 */
public class MoveAxiomsWizardKitConfigurationPanel extends AbstractMoveAxiomsWizardPanel {

    private final MoveAxiomsKitConfigurationPanel content;
    private final Object prevId;
    private final Object nextId;

    private JPanel holder;

    public MoveAxiomsWizardKitConfigurationPanel(Object prevId,
                                                 Object nextId,
                                                 MoveAxiomsKitConfigurationPanel content,
                                                 OWLEditorKit owlEditorKit) {
        super(content.getID(), content.getTitle(), owlEditorKit);
        this.content = content;
        this.prevId = prevId;
        this.nextId = nextId;
        holder.add(content);
        setInstructions(content.getInstructions());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        parent.setLayout(new BorderLayout());
        parent.add(holder = new JPanel(new BorderLayout()));
    }

    @Override
    public void aboutToDisplayPanel() {
        super.aboutToDisplayPanel();
        content.update();
        setComponentTransparency(content);
    }

    @Override
    public void aboutToHidePanel() {
        super.aboutToHidePanel();
        content.commit();
    }

    @Override
    public Object getBackPanelDescriptor() {
        return prevId;
    }

    @Override
    public Object getNextPanelDescriptor() {
        return nextId;
    }
}
