package org.protege.editor.owl.ui.view.rdf.utils;

import org.protege.editor.owl.ui.view.rdf.AddTriplePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by @ssz on 16.01.2021.
 */
public class GridBagUtils {

    public static void addSimpleRow(JPanel res, String label, String delimiter, JComponent field, int index) {
        addLabelCell(res, label, index);
        addDelimiterCell(res, delimiter, index);
        addIRICell(res, field, index);
    }

    public static void addControlCell(JPanel panel, JComponent component, int row) {
        addGridComponent(panel, component, 1, row, 0., GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL);
    }

    public static void addIRICell(JPanel panel, JComponent area, int row) {
        addGridComponent(panel, area, 2, row, 100., GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL);
    }

    public static void addDelimiterCell(JPanel panel, String delimiter, int row) {
        addGridComponent(panel, new JLabel(delimiter), 1, row, 0., GridBagConstraints.CENTER, GridBagConstraints.NONE);
    }

    public static void addLine(JPanel panel, int row) {
        addGridComponent(panel, new JSeparator(), 0, row, 4, 100.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, AddTriplePanel.LINE_INSETS);
    }

    public static void addLabelCell(JPanel panel, String label, int row) {
        addGridComponent(panel, new JLabel(label), 0, row, 0.0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE);
    }

    public static void addGridComponent(JPanel panel,
                                        JComponent component,
                                        int gridx,
                                        int gridy,
                                        double weightx,
                                        int anchor,
                                        int fill) {
        addGridComponent(panel, component, gridx, gridy, 1, weightx, anchor, fill, AddTriplePanel.CELL_INSETS);
    }

    public static void addGridComponent(JPanel panel,
                                        JComponent component,
                                        int gridx,
                                        int gridy,
                                        int gridwidth,
                                        double weightx,
                                        int anchor,
                                        int fill,
                                        Insets insets) {
        panel.add(component, new GridBagConstraints(gridx, gridy, gridwidth, 1, weightx, 0.0, anchor, fill, insets, 0, 0));
    }
}
