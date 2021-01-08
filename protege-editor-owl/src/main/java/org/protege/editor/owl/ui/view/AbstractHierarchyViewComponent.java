package org.protege.editor.owl.ui.view;

import org.protege.editor.owl.model.hierarchy.HierarchyProvider;
import org.protege.editor.owl.ui.tree.ObjectTree;

/**
 * Created by @ssz on 07.01.2021.
 */
public abstract class AbstractHierarchyViewComponent<E>
        extends AbstractOWLSelectionViewComponent implements Findable<E>, Deleteable {

    protected abstract ObjectTree<E> getTree();

    protected abstract HierarchyProvider<E> getHierarchyProvider();

    protected abstract void transmitSelection();
}
