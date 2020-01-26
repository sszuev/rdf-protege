package org.protege.editor.owl.ui.renderer;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 9 Aug 16
 */
public class DeleteEntityIcon implements Icon {

    private final Icon icon;
    private final Color color;

    public DeleteEntityIcon(OWLEntityIcon entityIcon) {
        this(entityIcon, entityIcon.getEntityColor());
    }

    public DeleteEntityIcon(Icon icon, Color color) {
        this.icon = Objects.requireNonNull(icon);
        this.color = Objects.requireNonNull(color);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        EntityActionIcon.setupAlpha(c, g2);
        try {
            int halfWidth = getIconWidth() / 2;
            int xC = halfWidth + x;
            int halfHeight = getIconHeight() / 2;
            int yC = halfHeight + y;
            icon.paintIcon(c, g2, xC - (icon.getIconWidth() / 2), yC - (icon.getIconHeight() / 2));
            g2.setStroke(EntityActionIcon.ACTION_STROKE);
            g2.setColor(color);
            int crossLegLen = 7;
            g2.drawLine(xC - crossLegLen, yC - crossLegLen, xC + crossLegLen, yC + crossLegLen);
            g2.drawLine(xC - crossLegLen, yC + crossLegLen, xC + crossLegLen, yC - crossLegLen);
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
