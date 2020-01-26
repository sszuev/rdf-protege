package org.protege.editor.owl.ui.tree;

/**
 * @param <N> - anything
 */
public interface TreeDragAndDropHandler<N> {

    boolean canDrop(Object child, Object parent);

    void move(N child, N fromParent, N toParent);

    void add(N child, N parent);
}
