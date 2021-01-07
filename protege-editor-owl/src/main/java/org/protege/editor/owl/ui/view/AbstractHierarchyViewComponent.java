package org.protege.editor.owl.ui.view;

/**
 * Created by @ssz on 07.01.2021.
 */
public abstract class AbstractHierarchyViewComponent<E>
        extends AbstractOWLSelectionViewComponent implements Findable<E>, Deleteable {

    protected abstract void transmitSelection();
}
