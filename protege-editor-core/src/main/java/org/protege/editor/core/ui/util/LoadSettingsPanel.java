package org.protege.editor.core.ui.util;

import org.protege.editor.core.OWLSource;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * UI-element (panel) to configure loading.
 * Created by @ssz on 09.01.2020.
 */
public class LoadSettingsPanel extends JPanel {

    private final JCheckBox withTRSelector;

    public LoadSettingsPanel(boolean withTR) {
        super();
        setLayout(new GridBagLayout());
        add(new JLabel("Transform to OWL:  "));
        withTRSelector = new JCheckBox();
        withTRSelector.setSelected(withTR);
        this.add(withTRSelector);
    }

    public boolean isTRSelected() {
        return withTRSelector.isSelected();
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> res = new HashMap<>();
        res.put(OWLSource.LOAD_FILE_WITH_TR_CONFIG_KEY, isTRSelected());
        return Collections.unmodifiableMap(res);
    }
}
