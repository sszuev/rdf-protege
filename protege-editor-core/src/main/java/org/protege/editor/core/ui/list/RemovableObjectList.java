package org.protege.editor.core.ui.list;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 21-Sep-2008<br><br>
 */
public class RemovableObjectList<O> extends MList<Object> {

    private ListCellRenderer<Object> rendererDelegate;

    @SuppressWarnings("unchecked")
    public RemovableObjectList() {
        super.setModel(new MutableObjectListModel());
        MListCellRenderer<Object> ren = (MListCellRenderer<Object>) getCellRenderer();
        ren.setContentRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = -4512962926323639137L;

            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                return rendererDelegate != null
                        ? rendererDelegate.getListCellRendererComponent(list, extractObject(value), index, isSelected, cellHasFocus)
                        : super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <X> X extractObject(Object value) {
        return value == null ? null : (X) ((RemovableObjectListItem) value).getObject();
    }

    public List<O> getListItems() {
        List<O> result = new ArrayList<>();
        for (int i = 0; i < getModel().getSize(); i++) {
            result.add(extractObject(getModel().getElementAt(i)));
        }
        return result;
    }

    @Override
    public void setCellRenderer(ListCellRenderer<? super Object> renderer) {
        if (!(getCellRenderer() instanceof MListCellRenderer)) {
            super.setCellRenderer(renderer);
        } else {
            rendererDelegate = renderer;
        }
    }

    @Override
    public void setModel(ListModel model) {
        throw new UnsupportedOperationException("Cannot change model in MutableObjectList");
    }

    public void addObject(Collection<O> objects) {
        for (Object o : objects) {
            ((MutableObjectListModel) getModel()).addElement(o);
        }
    }

    /*
     * At SVN revision: 26201, method:
     * public void setListData(final Vector<?> listData)
     * was deleted because it was not compiling against Java 1.7.
     * This method seems to be never called (even by internal Java methods)
     */
    @Override
    public void setListData(final Object[] listData) {
        MutableObjectListModel model = (MutableObjectListModel) getModel();
        model.clear();
        for (Object o : listData) {
            model.addElement(o);
        }
    }

    public void setObjects(Collection<O> objects) {
        MutableObjectListModel model = (MutableObjectListModel) getModel();
        model.clear();
        for (Object o : objects) {
            model.addElement(o);
        }
    }

    @Override
    public O getSelectedValue() {
        return extractObject(super.getSelectedValue());
    }

    public Collection<O> getSelectedObjects() {
        Collection<O> objects = new ArrayList<>();
        for (Object o : getSelectedValuesList()) {
            objects.add(extractObject(o));
        }
        return objects;
    }

    @Override
    protected void handleDelete() {
        MListItem item = (MListItem) super.getSelectedValue();
        item.handleDelete();
    }

    @SuppressWarnings("unchecked")
    private class MutableObjectListModel extends DefaultListModel<Object> {

        @Override
        public void setElementAt(Object obj, int index) {
            super.setElementAt(new RemovableObjectListItem((O) obj), index);
        }

        @Override
        public Object set(int index, Object element) {
            return super.set(index, new RemovableObjectListItem((O) element));
        }

        @Override
        public void addElement(Object obj) {
            super.addElement(new RemovableObjectListItem((O) obj));
        }

        @Override
        public void add(int index, Object element) {
            super.add(index, new RemovableObjectListItem((O) element));
        }
    }

    public class RemovableObjectListItem implements MListItem {
        private final O object;

        public RemovableObjectListItem(O object) {
            this.object = object;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public void handleEdit() {
        }

        @Override
        public boolean isDeleteable() {
            return true;
        }

        @Override
        public String toString() {
            return "LI: " + object.toString();
        }

        @Override
        public boolean handleDelete() {
            MutableObjectListModel model = ((MutableObjectListModel) getModel());
            int index = model.indexOf(this);
            return model.remove(index) != null;
        }

        @Override
        public String getTooltip() {
            return object.toString();
        }

        public O getObject() {
            return object;
        }
    }
}
