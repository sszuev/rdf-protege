package org.protege.editor.owl.ui.renderer;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 9 Aug 16
 */
public class AddSiblingIcon implements Icon {

    private final Icon icon;
    private final Color color;

    public AddSiblingIcon(OWLEntityIcon entityIcon) {
        this(entityIcon, entityIcon.getEntityColor());
    }

    public AddSiblingIcon(Icon icon, Color color) {
        this.icon = Objects.requireNonNull(icon);
        this.color = Objects.requireNonNull(color);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            EntityActionIcon.setupAlpha(c, g2);

            int cx0 = x + getIconWidth() / 2 - 2;
            int cy0 = y + getIconHeight() / 4;

            int cx1 = x + getIconWidth() / 2 - 2;
            int cy1 = y + getIconHeight() * 3 / 4;

            g2.setColor(color);

            int backLegX = x + getIconWidth() / 4 - 3;
            g2.drawLine(cx0, cy0, backLegX, cy0);
            g2.drawLine(cx1, cy1, backLegX, cy1);
            g2.drawLine(backLegX, cy0, backLegX, cy1);

            int addX = x + getIconWidth() - 3;
            @SuppressWarnings("UnnecessaryLocalVariable") int addY = cy1;
            int addLegLen = 2;

            g2.setStroke(EntityActionIcon.ACTION_STROKE);
            g2.drawLine(addX - addLegLen, addY, addX + addLegLen, addY);
            g2.drawLine(addX, addY - addLegLen, addX, addY + addLegLen);


            double scaleFactor = 0.6;
            g2.translate(cx0, cy0);
            int topSibX = -icon.getIconWidth() / 2;
            int topSibY = -icon.getIconHeight() / 2;

            g2.scale(scaleFactor, scaleFactor);
            icon.paintIcon(c, g2, topSibX, topSibY);
            g2.scale(1 / scaleFactor, 1 / scaleFactor);
            g2.translate(-cx0, -cy0);

            g2.translate(cx1, cy1);
            int botSibX = -icon.getIconWidth() / 2;
            int botSibY = -icon.getIconHeight() / 2;
            g2.scale(scaleFactor, scaleFactor);
            icon.paintIcon(c, g2, botSibX, botSibY);
            g2.scale(1 / scaleFactor, 1 / scaleFactor);
            g2.translate(-cx1, -cy1);
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
