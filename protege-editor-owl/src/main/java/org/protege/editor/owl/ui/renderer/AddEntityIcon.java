package org.protege.editor.owl.ui.renderer;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 9 Aug 16
 */
public class AddEntityIcon implements Icon {

    private final Icon icon;
    private final Color color;

    public AddEntityIcon(OWLEntityIcon entityIcon) {
        this(entityIcon, entityIcon.getEntityColor());
    }

    public AddEntityIcon(Icon icon, Color color) {
        this.icon = Objects.requireNonNull(icon);
        this.color = Objects.requireNonNull(color);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            EntityActionIcon.setupAlpha(c, g2);

            icon.paintIcon(c, g2, x + 1, y + 1);
            int addCrossLegLength = 2;
            int xC = x + icon.getIconWidth() + addCrossLegLength;
            int yC = y + 4;
            g2.setStroke(EntityActionIcon.ACTION_STROKE);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setColor(color);
            g2.drawLine(xC - addCrossLegLength, yC, xC + addCrossLegLength, yC);
            g2.drawLine(xC, yC - addCrossLegLength, xC, yC + addCrossLegLength);
        } finally {
            g2.dispose();
        }

    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth() + 2;
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight() + 2;
    }
}
