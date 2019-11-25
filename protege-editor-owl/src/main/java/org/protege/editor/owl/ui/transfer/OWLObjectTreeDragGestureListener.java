package org.protege.editor.owl.ui.transfer;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.tree.ObjectTree;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * TODO: rename
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 04-Jun-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLObjectTreeDragGestureListener<N> extends OWLObjectDragGestureListener<N> {

    private final ObjectTree<N> tree;

    public OWLObjectTreeDragGestureListener(OWLEditorKit owlEditorKit, ObjectTree<N> tree) {
        super(owlEditorKit, tree);
        this.tree = tree;
    }

    @Override
    protected List<N> getSelectedObjects() {
        return new ArrayList<N>(tree.getSelectedOWLObjects());
    }

    @Override
    protected JComponent getRendererComponent() {
        return (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(tree,
                tree.getSelectionPath().getLastPathComponent(),
                false,
                true,
                true,
                0,
                false);
    }

    @Override
    protected Dimension getRendererComponentSize() {
        Rectangle bounds = tree.getRowBounds(tree.getRowForPath(tree.getSelectionPath()));
        return bounds.getSize();
    }

    @Override
    protected Point getImageOffset() {
        TreePath selPath = tree.getSelectionPath();
        Rectangle rowBounds = tree.getRowBounds(tree.getRowForPath(selPath));
        Point pt = tree.getMousePosition();
        return new Point(rowBounds.x - pt.x, rowBounds.y - pt.y);
    }

    @Override
    protected boolean canPerformDrag() {
        // Can always drag from a tree (if not always drop on it)
        return true;
    }
}
