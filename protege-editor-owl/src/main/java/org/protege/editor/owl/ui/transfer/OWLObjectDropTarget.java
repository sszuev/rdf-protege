package org.protege.editor.owl.ui.transfer;

import org.protege.editor.owl.model.OWLModelManager;

import javax.swing.*;
import java.awt.*;
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
public interface OWLObjectDropTarget<N> {

    JComponent getComponent();

    boolean dropOWLObjects(List<N> owlObjects, Point pt, int type);

    OWLModelManager getOWLModelManager();
}
