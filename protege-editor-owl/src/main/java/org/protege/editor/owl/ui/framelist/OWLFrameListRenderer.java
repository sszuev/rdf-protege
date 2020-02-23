package org.protege.editor.owl.ui.framelist;

import org.protege.editor.core.ui.list.RendererWithInsets;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.protege.editor.owl.ui.renderer.*;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.protege.editor.owl.ui.renderer.InlineAnnotationRendering.DO_NOT_RENDER_COMPOUND_ANNOTATIONS_INLINE;
import static org.protege.editor.owl.ui.renderer.InlineAnnotationRendering.RENDER_COMPOUND_ANNOTATIONS_INLINE;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLFrameListRenderer implements ListCellRenderer<Object>, RendererWithInsets {

    private final OWLEditorKit kit;
    private final OWLCellRenderer owlCellRenderer;
    private final ListCellRenderer<Object> separatorRenderer;
    private final OWLAnnotationCellRenderer2 annotationRenderer;
    private final Set<OWLEntity> crossedOutEntities;

    private boolean highlightKeywords;
    private boolean highlightUnsatisfiableClasses;
    private boolean annotationRendererEnabled;

    public OWLFrameListRenderer(OWLEditorKit kit) {
        this.kit = Objects.requireNonNull(kit);
        owlCellRenderer = new OWLCellRenderer(kit);
        separatorRenderer = new DefaultListCellRenderer();
        annotationRenderer = new OWLAnnotationCellRenderer2(kit);
        highlightKeywords = true;
        highlightUnsatisfiableClasses = true;
        annotationRendererEnabled = true;
        crossedOutEntities = new HashSet<>();
    }

    public OWLEditorKit getOWLEditorKit() {
        return kit;
    }

    public void setHighlightKeywords(boolean highlightKeywords) {
        this.highlightKeywords = highlightKeywords;
    }

    public OWLCellRenderer getOWLCellRenderer() {
        return owlCellRenderer;
    }

    protected OWLRendererPreferences getOWLRendererPreferences() {
        return OWLRendererPreferences.getInstance();
    }

    /**
     * Returns a component that has been configured to display the specified
     * value. That component's <code>paint</code> method is then called to
     * "render" the cell.  If it is necessary to compute the dimensions
     * of a list because the list cells do not have a fixed size, this method
     * is called to generate a component on which <code>getPreferredSize</code>
     * can be invoked.
     *
     * @param list         The JList we're painting.
     * @param value        The value returned by list.getModel().getElementAt(index).
     * @param index        The cells index.
     * @param isSelected   True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     * @see javax.swing.JList
     * @see javax.swing.ListSelectionModel
     * @see javax.swing.ListModel
     */
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {


        if (value instanceof OWLFrameSection) {
            JLabel label = (JLabel) separatorRenderer.getListCellRendererComponent(list, " ", index, isSelected, cellHasFocus);
            label.setVerticalAlignment(JLabel.TOP);
            return label;
        }

        AbstractOWLFrameSectionRow<?, ?, ?> row = (AbstractOWLFrameSectionRow<?, ?, ?>) value;
        OWLAxiom axiom = row.getAxiom();
        if (axiom instanceof OWLAnnotationAssertionAxiom && annotationRendererEnabled) {
            OWLAnnotationAssertionAxiom annotationAssertionAxiom = (OWLAnnotationAssertionAxiom) axiom;
            annotationRenderer.setInlineAnnotationRendering(getRenderAnnotationAnnotationsInline());
            annotationRenderer.setInlineDatatypeRendering(getAnnotationLiteralDatatypeRendering());
            annotationRenderer.setThumbnailRendering(getInlineThumbnailRendering());
            return annotationRenderer.getListCellRendererComponent(list, annotationAssertionAxiom, index, isSelected, cellHasFocus);
        }

        owlCellRenderer.setCommentedOut(false);
        Object valueToRender = getValueToRender(value);
        owlCellRenderer.setIconObject(getIconObject(value));
        owlCellRenderer.setOntology(((OWLFrameSectionRow<?, ?, ?>) value).getOntology());
        owlCellRenderer.setInferred(((OWLFrameSectionRow<?, ?, ?>) value).isInferred());
        owlCellRenderer.setHighlightKeywords(highlightKeywords);
        owlCellRenderer.setHighlightUnsatisfiableClasses(highlightUnsatisfiableClasses);
        owlCellRenderer.setCrossedOutEntities(crossedOutEntities);
        return owlCellRenderer.getListCellRendererComponent(list, valueToRender, index, isSelected, cellHasFocus);
    }

    private InlineDatatypeRendering getAnnotationLiteralDatatypeRendering() {
        return getOWLRendererPreferences().isDisplayLiteralDatatypesInline()
                ? InlineDatatypeRendering.RENDER_DATATYPE_INLINE : InlineDatatypeRendering.DO_NOT_RENDER_DATATYPE_INLINE;
    }

    private InlineAnnotationRendering getRenderAnnotationAnnotationsInline() {
        return getOWLRendererPreferences().isDisplayAnnotationAnnotationsInline()
                ? RENDER_COMPOUND_ANNOTATIONS_INLINE : DO_NOT_RENDER_COMPOUND_ANNOTATIONS_INLINE;
    }

    private InlineThumbnailRendering getInlineThumbnailRendering() {
        return getOWLRendererPreferences().isDisplayThumbnailsInline()
                ? InlineThumbnailRendering.DISPLAY_THUMBNAILS_INLINE : InlineThumbnailRendering.DO_NOT_DISPLAY_THUMBNAILS_INLINE;
    }

    protected OWLObject getIconObject(Object value) {
        if (!(value instanceof AbstractOWLFrameSectionRow)) {
            return null;
        }
        AbstractOWLFrameSectionRow<?, ?, ?> row = (AbstractOWLFrameSectionRow<?, ?, ?>) value;
        return row.manipulatableObjects().findFirst().orElse(null);
    }

    protected Object getValueToRender(Object value) {
        if (value instanceof AbstractOWLFrameSectionRow) {
            value = ((AbstractOWLFrameSectionRow<?, ?, ?>) value).getRendering();
        }
        return value;
    }
}
