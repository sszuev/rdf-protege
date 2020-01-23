package org.protege.editor.owl.ui.transfer;

import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;


/**
 * TODO: handle unused methods
 *
 * @param <X> - any object
 */
public abstract class ObjectDragGestureListener<X> implements DragGestureListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectDragGestureListener.class);

    private final Cursor dragCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    private final JComponent component;
    private final OWLEditorKit owlEditorKit;

    protected ObjectDragGestureListener(OWLEditorKit owlEditorKit, JComponent component) {
        this.component = Objects.requireNonNull(component);
        this.owlEditorKit = Objects.requireNonNull(owlEditorKit);
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        if (!canPerformDrag()) {
            return;
        }
        if (getSelectedObjects().isEmpty()) {
            return;
        }
        TransferableOWLObject to = new TransferableOWLObject(owlEditorKit.getModelManager(), getSelectedObjects());
        setupDragOriginator();
        try {
            dge.startDrag(dragCursor, to, new OWLDragSourceAdapter(component));
        } catch (InvalidDnDOperationException e) {
            LOGGER.debug("Invalid drop operation");
        }
    }

    protected boolean canPerformDrag() {
        return true;
    }

    protected abstract List<X> getSelectedObjects();

    protected abstract JComponent getRendererComponent();

    protected abstract Dimension getRendererComponentSize();

    protected abstract Point getImageOffset();

    protected Image createImage() {
        JComponent component = getRendererComponent();
        component.setSize(getRendererComponentSize());
        // component.setOpaque(false);
        BufferedImage img = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.6f));
        component.paint(g2);
        return img;
    }

    private void setupDragOriginator() {
        if (component instanceof OWLObjectDragSource) {
            ((OWLObjectDragSource) component).setDragOriginater(true);
        }
    }

    private static class OWLDragSourceAdapter extends DragSourceAdapter {

        private Component component;

        public OWLDragSourceAdapter(Component component) {
            this.component = component;
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (component instanceof OWLObjectDragSource) {
                ((OWLObjectDragSource) component).setDragOriginater(false);
            }
        }
    }
}
