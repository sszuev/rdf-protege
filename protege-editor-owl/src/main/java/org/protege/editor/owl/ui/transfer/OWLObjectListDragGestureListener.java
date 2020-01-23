package org.protege.editor.owl.ui.transfer;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 04-Jul-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLObjectListDragGestureListener extends ObjectDragGestureListener<OWLObject> {

    private final OWLObjectList<OWLObject> list;

    @SuppressWarnings("unchecked")
    public OWLObjectListDragGestureListener(OWLEditorKit kit, OWLObjectList<? extends OWLObject> list) {
        super(kit, list);
        this.list = (OWLObjectList<OWLObject>) Objects.requireNonNull(list);
    }

    @Override
    protected Point getImageOffset() {
        return new Point(0, 0);
    }

    @Override
    protected JComponent getRendererComponent() {
        return (JComponent) list.getCellRenderer()
                .getListCellRendererComponent(list, list.getSelectedValue(), list.getSelectedIndex(), true, true);
    }

    @Override
    protected Dimension getRendererComponentSize() {
        return getRendererComponent().getPreferredSize();
    }

    @Override
    protected List<OWLObject> getSelectedObjects() {
        return list.getSelectedValuesList();
    }
}
