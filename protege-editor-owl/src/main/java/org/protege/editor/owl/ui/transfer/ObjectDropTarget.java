package org.protege.editor.owl.ui.transfer;

import org.protege.editor.owl.model.OWLModelManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * @param <N> - anything
 */
public interface ObjectDropTarget<N> {

    JComponent getComponent();

    boolean dropObjects(List<N> owlObjects, Point pt, int type);

    OWLModelManager getOWLModelManager();
}
