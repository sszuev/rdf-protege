package org.protege.editor.owl.model.selection;

import javax.annotation.Nullable;

/**
 * Created by @ssz on 07.01.2021.
 */
public interface SelectionModel {
    void setSelectedObject(@Nullable Object object);

    Object getSelectedObject();
}
