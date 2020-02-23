package org.protege.editor.owl.ui;

import org.protege.editor.core.OWLSource;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.core.ui.util.LoadSettingsPanel;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.protege.editor.owl.ui.list.OWLEntityListPanel;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.protege.editor.owl.ui.selector.*;
import org.protege.editor.owl.ui.util.OWLComponentFactory;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Apr 24, 2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
@SuppressWarnings("unused")
public class UIHelper {

    public static final Set<String> OWL_EXTENSIONS;

    static {
        Set<String> extensions = new HashSet<>();
        extensions.add("owl");
        extensions.add("ofn");
        extensions.add("omn");
        extensions.add("owx");
        extensions.add("rdf");
        extensions.add("xml");
        extensions.add("obo");
        extensions.add("n3");
        extensions.add("ttl");
        extensions.add("turtle");
        extensions.add("pom");
        OWL_EXTENSIONS = Collections.unmodifiableSet(extensions);
    }

    private OWLEditorKit owlEditorKit;

    public UIHelper(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
    }

    private JComponent getParent() {
        return owlEditorKit.getWorkspace();
    }

    private OWLModelManager getOWLModelManager() {
        return owlEditorKit.getModelManager();
    }

    private OWLComponentFactory getOWLComponentFactory() {
        return owlEditorKit.getOWLWorkspace().getOWLComponentFactory();
    }

    public URI getURI(String title, String message) throws URISyntaxException {
        String uriString = JOptionPane.showInputDialog(getParent(), message, title, JOptionPane.INFORMATION_MESSAGE);
        if (uriString == null) {
            return null;
        }
        return new URI(uriString);
    }

    public OWLClass pickOWLClass() {
        OWLClassSelectorPanel clsPanel = new OWLClassSelectorPanel(owlEditorKit);
        int ret = showDialog("Select a class", clsPanel);
        clsPanel.dispose();
        if (ret != JOptionPane.OK_OPTION) {
            return null;
        }
        return clsPanel.getSelectedObject();
    }

    public OWLIndividual pickOWLIndividual() {
        OWLIndividualSelectorPanel indPanel = getOWLComponentFactory().getOWLIndividualSelectorPanel();
        int ret = showDialog("Select an individual", indPanel);
        if (ret != JOptionPane.OK_OPTION) {
            return null;
        }
        OWLIndividual ind = indPanel.getSelectedObject();
        indPanel.dispose();
        return ind;
    }

    public OWLOntology pickOWLOntology() {
        OWLOntologySelectorPanel ontPanel = new OWLOntologySelectorPanel(owlEditorKit);
        ontPanel.setMultipleSelectionEnabled(false);
        int ret = showDialog("Select an ontology", ontPanel);
        return ret == JOptionPane.OK_OPTION ? ontPanel.getSelectedOntology() : null;
    }

    public Set<OWLOntology> pickOWLOntologies() {
        OWLOntologySelectorPanel ontPanel = new OWLOntologySelectorPanel(owlEditorKit);
        int ret = showDialog("Select ontologies", ontPanel);
        return ret == JOptionPane.OK_OPTION ? ontPanel.getSelectedOntologies() : Collections.emptySet();
    }

    public int showDialog(String title, JComponent component) {
        return JOptionPaneEx.showConfirmDialog(getParent(), title, component,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
    }

    public int showDialog(String title, JComponent component, int options) {
        return JOptionPaneEx.showConfirmDialog(getParent(), title, component,
                JOptionPane.PLAIN_MESSAGE, options, null);
    }


    public int showDialog(String title, JComponent component, JComponent focusedComponent) {
        return JOptionPaneEx.showConfirmDialog(getParent(), title, component,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, focusedComponent);
    }


    public int showValidatingDialog(String title, JComponent component, JComponent focusedComponent) {
        return JOptionPaneEx.showValidatingConfirmDialog(getParent(), title, component,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, focusedComponent);
    }

    public OWLObjectProperty pickOWLObjectProperty() {
        OWLObjectPropertySelectorPanel p = getOWLComponentFactory().getOWLObjectPropertySelectorPanel();
        return showDialog("Select an object property", p) == JOptionPane.OK_OPTION ? p.getSelectedObject() : null;
    }

    public OWLDataProperty pickOWLDataProperty() {
        OWLDataPropertySelectorPanel p = getOWLComponentFactory().getOWLDataPropertySelectorPanel();
        return showDialog("Select an object property", p) == JOptionPane.OK_OPTION ? p.getSelectedObject() : null;
    }

    public OWLDatatype pickOWLDatatype() {
        OWLDataTypeSelectorPanel panel = new OWLDataTypeSelectorPanel(owlEditorKit);
        return showDialog("Select a datatype", panel) == JOptionPane.OK_OPTION ? panel.getSelectedObject() : null;
    }

    public <E extends OWLEntity> E pickOWLEntity(String message, Set<E> entities, OWLModelManager owlModelManager) {
        OWLEntityListPanel<E> panel = new OWLEntityListPanel<>(message, entities, owlEditorKit);
        return showDialog("Select an entity", panel) == JOptionPane.OK_OPTION ? panel.getSelectedObject() : null;
    }

    public OWLAnnotationProperty pickAnnotationProperty() {
        OWLAnnotationPropertySelectorPanel panel = new OWLAnnotationPropertySelectorPanel(owlEditorKit);
        try {
            return showDialog("Select an annotation property", panel) == JOptionPane.OK_OPTION ?
                    panel.getSelectedObject() : null;
        } finally {
            panel.dispose();
        }
    }

    public String getHTMLOntologyList(Collection<OWLOntology> ontologies) {
        StringBuilder result = new StringBuilder();
        for (OWLOntology ont : ontologies) {
            Optional<IRI> defaultDocumentIRI = ont.getOntologyID().getDefaultDocumentIRI();
            if (defaultDocumentIRI.isPresent()) {
                if (getOWLModelManager().getActiveOntology().equals(ont)) {
                    result.append("<font color=\"0000ff\"><b>");
                    result.append(defaultDocumentIRI.get());
                    result.append("</font></b>");
                } else {
                    result.append(defaultDocumentIRI);
                }
            }
            if (!getOWLModelManager().isMutable(ont)) {
                result.append("&nbsp;");
                result.append(" <font color=\"ff0000\">(Not editable)</font>");
            }
            result.append("<br>");
        }
        return result.toString();
    }

    public int showOptionPane(String title, String message, int optionType, int messageType) {
        return JOptionPane.showConfirmDialog(getParent(), message, title, optionType, messageType);
    }

    public OWLSource chooseOWLFile(String title) {
        JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, getParent());
        if (frame == null) {
            frame = new JFrame();
        }
        LoadSettingsPanel panel = new LoadSettingsPanel(true);
        File file = UIUtil.openFile(frame, title, "OWL File", OWL_EXTENSIONS, panel);
        Map<String, Object> props = panel.getProperties();
        return OWLSource.create(file, props);
    }

    public File saveOWLFile(String title) {
        return UIUtil.saveFile((JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, getParent()),
                title, "OWL File", OWL_EXTENSIONS);
    }

    public OWLLiteral createConstant() {
        return null;
    }

    public JComboBox<String> getLanguageSelector() {
        JComboBox<String> c = new JComboBox<>();
        c.setSelectedItem(null);
        c.setEditable(true);
        c.setModel(new DefaultComboBoxModel<>(new String[]{null, "en", "de", "es", "fr", "pt"}));
        return c;
    }

    public JComboBox<OWLDatatype> getDatatypeSelector() {
        OWLModelManager m = getOWLModelManager();
        List<OWLDatatype> datatypeList = new OWLDataTypeUtils(m.getOWLOntologyManager())
                .knownDatatypes(m.getActiveOntologies())
                .sorted(m.getOWLObjectComparator())
                .collect(Collectors.toList());
        datatypeList.add(0, null);
        JComboBox<OWLDatatype> c = new JComboBox<>(new DefaultComboBoxModel<>(datatypeList.toArray(new OWLDatatype[0])));
        c.setPreferredSize(new Dimension(120, c.getPreferredSize().height));
        c.setRenderer(new OWLCellRendererSimple(owlEditorKit));
        return c;
    }

}
