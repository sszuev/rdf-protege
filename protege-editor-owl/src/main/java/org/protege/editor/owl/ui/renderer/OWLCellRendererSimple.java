package org.protege.editor.owl.ui.renderer;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Nov-2007<br><br>
 * <p>
 * A simple renderer for trees and lists (where syntax highlighting etc. are
 * not required).
 */
public class OWLCellRendererSimple implements TreeCellRenderer, ListCellRenderer<Object> {

    private final OWLEditorKit owlEditorKit;
    private final DefaultTreeCellRenderer treeCellRendererDelegate;
    private final DefaultListCellRenderer listCellRenderDelegate;

    private boolean displayQuotes = true;

    public OWLCellRendererSimple(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        treeCellRendererDelegate = new DefaultTreeCellRenderer();
        listCellRenderDelegate = new DefaultListCellRenderer();
    }

    /**
     * Specifies whether or not single quotation marks should be displayed.
     * Protege surrounds names containing spaces with single quotes.
     * This setting can be used to suppress this behaviour for this particular render instance.
     * Quotes are displayed by default.
     *
     * @param displayQuotes true if quotes should be displayed, otherwise false
     */
    public void setDisplayQuotes(boolean displayQuotes) {
        this.displayQuotes = displayQuotes;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        JLabel label = (JLabel) treeCellRendererDelegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        prepareRenderer(label, value);
        return label;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        JLabel label = (JLabel) listCellRenderDelegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        prepareRenderer(label, value);
        return label;
    }

    private void prepareRenderer(JLabel label, Object value) {
        final Font font = OWLRendererPreferences.getInstance().getFont();
        label.setFont(font);
        setText(value, label);
        setIcon(value, label);
        boldIfNecessary(value, label);
        if (value == null) {
            // so that null does not render with no height
            label.setPreferredSize(new Dimension(label.getPreferredSize().width, label.getFontMetrics(font).getHeight()));
        }
    }

    private void setText(Object value, JLabel renderer) {
        if (!(value instanceof OWLObject)) {
            return;
        }
        OWLObject obj = (OWLObject) value;
        String rendering = owlEditorKit.getModelManager().getRendering(obj);
        if (!displayQuotes && rendering.length() > 2 && rendering.startsWith("'") && rendering.endsWith("'")) {
            String stripped = rendering.substring(1, rendering.length() - 1);
            renderer.setText(stripped);
        } else {
            renderer.setText(rendering);
        }
    }

    private void setIcon(Object value, JLabel renderer) {
        if (!(value instanceof OWLObject)) {
            return;
        }
        OWLObject obj = (OWLObject) value;
        Icon icon = owlEditorKit.getWorkspace().getOWLIconProvider().getIcon(obj);
        renderer.setIcon(icon);
    }

    private void boldIfNecessary(Object value, JLabel renderer) {
        if (!(value instanceof OWLEntity)) {
            return;
        }
        OWLEntity ent = (OWLEntity) value;
        OWLOntology o = owlEditorKit.getModelManager().getActiveOntology();
        Stream<?> res;
        if (ent instanceof OWLClass) {
            res = o.axioms((OWLClass) ent);
        } else if (ent instanceof OWLObjectProperty) {
            res = o.axioms((OWLObjectProperty) ent);
        } else if (ent instanceof OWLDataProperty) {
            res = o.axioms((OWLDataProperty) ent);
        } else if (ent instanceof OWLIndividual) {
            res = o.axioms((OWLIndividual) ent);
        } else {
            res = Stream.empty();
        }
        if (res.findFirst().isPresent()) {
            makeBold(renderer);
        }
    }

    private static void makeBold(JLabel label) {
        label.setText("<html><body><b>" + label.getText() + "</b></body></html>");
    }
}
