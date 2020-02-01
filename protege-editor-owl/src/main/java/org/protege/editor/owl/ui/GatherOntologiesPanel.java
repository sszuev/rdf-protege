package org.protege.editor.owl.ui;

import com.github.owlcs.ontapi.OntFormat;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLOntologyCellRenderer;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 21-Mar-2007<br><br>
 */
public class GatherOntologiesPanel extends JPanel {

    private final OWLModelManager owlModelManager;
    private JComboBox<Object> formatComboBox;
    private File saveLocation;

    private final Set<OWLOntology> ontologiesToSave;

    public GatherOntologiesPanel(OWLEditorKit kit) {
        this.owlModelManager = kit.getModelManager();
        ontologiesToSave = new HashSet<>();
        createUI();
    }

    private void createUI() {
        JPanel holderPanel = new JPanel(new BorderLayout());
        JPanel comboBoxLabelPanel = new JPanel(new BorderLayout(7, 7));
        List<Object> formats = new ArrayList<>();
        formats.add("Original");
        OntologyFormatPanel.outputFormats()
                .map(OntFormat::createOwlFormat).forEach(formats::add);
        formatComboBox = new JComboBox<>(formats.toArray());
        comboBoxLabelPanel.add(new JLabel("Format"), BorderLayout.WEST);
        comboBoxLabelPanel.add(formatComboBox, BorderLayout.EAST);
        JPanel formatPanelHolder = new JPanel();
        formatPanelHolder.add(comboBoxLabelPanel);
        holderPanel.add(formatPanelHolder, BorderLayout.NORTH);

        Box box = new Box(BoxLayout.Y_AXIS);

        final List<OWLOntology> orderedOntologies = new ArrayList<>(owlModelManager.getOntologies());
        orderedOntologies.sort(owlModelManager.getOWLObjectComparator());
        for (OWLOntology ont : orderedOntologies) {
            ontologiesToSave.add(ont);
            String label = OWLOntologyCellRenderer.getOntologyLabelText(ont, owlModelManager);
            JCheckBox cb = new JCheckBox(new AbstractAction(label) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!ontologiesToSave.contains(ont)) { // wtf?
                        ontologiesToSave.remove(ont);
                    } else {
                        ontologiesToSave.add(ont);
                    }
                }
            });
            cb.setSelected(true);
            cb.setOpaque(false);
            box.add(cb);
            box.add(Box.createVerticalStrut(3));
        }

        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 150));
        box.setBackground(Color.WHITE);
        JPanel boxHolder = new JPanel(new BorderLayout());
        boxHolder.setBorder(ComponentFactory.createTitledBorder("Ontologies"));
        boxHolder.add(new JScrollPane(box));
        boxHolder.setPreferredSize(new Dimension(boxHolder.getPreferredSize().width,
                                                 Math.min(boxHolder.getPreferredSize().height, 300)));
        holderPanel.add(boxHolder, BorderLayout.CENTER);
        holderPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new BorderLayout());
        add(holderPanel, BorderLayout.CENTER);
    }

    public Set<OWLOntology> getOntologiesToSave() {
        return ontologiesToSave;
    }

    public OWLDocumentFormat getOntologyFormat() {
        Object f = formatComboBox.getSelectedItem();
        return f instanceof OWLDocumentFormat ? (OWLDocumentFormat) f : null;
    }

    public File getSaveLocation() {
        return saveLocation;
    }

    public void setSaveLocation(File saveLocation) {
        this.saveLocation = saveLocation;
    }

    public static GatherOntologiesPanel showDialog(OWLEditorKit owlEditorKit) {
        GatherOntologiesPanel panel = new GatherOntologiesPanel(owlEditorKit);
        panel.setPreferredSize(new Dimension(600, 400));

        int res = JOptionPane.showConfirmDialog(null, panel, "Gather ontologies",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return null;
        }
        File file = UIUtil.chooseFolder(owlEditorKit.getWorkspace(), "Select folder to save the ontologies to");
        if (file == null) {
            return null;
        }
        panel.setSaveLocation(file);
        return panel;
    }
}
