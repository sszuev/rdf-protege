package org.protege.editor.owl.ui.renderer;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 9 Aug 16
 */
public class AddChildIcon implements Icon {

    private final Icon icon;
    private final Color color;
    private final int baseSize;

    public AddChildIcon(OWLEntityIcon entityIcon) {
        this(entityIcon, entityIcon.getEntityColor(), entityIcon.getBaseSize());
    }

    public AddChildIcon(Icon icon, Color color) {
        this(icon, color, (icon.getIconHeight() + icon.getIconWidth()) / 2);
    }

    public AddChildIcon(Icon icon, Color color, int baseSize) {
        this.icon = Objects.requireNonNull(icon);
        this.color = Objects.requireNonNull(color);
        this.baseSize = baseSize;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            EntityActionIcon.setupAlpha(c, g2);
            Color oldColor = g2.getColor();
            Stroke oldStroke = g2.getStroke();

            int iconWidth = 18;
            int iconHeight = 18;

            int parCX = x + iconWidth / 4;
            int parCY = y + iconHeight / 4;

            int childCX = x +  (iconWidth * 3) / 4;
            int childCY = y + (iconHeight * 3) / 4;

            g2.setColor(color);

            g2.drawLine(parCX, parCY, parCX, childCY);
            g2.drawLine(parCX, childCY, childCX, childCY);

            int addX = x + iconWidth - 4;
            int addY = y + 4;
            int addLegLen = 2;

            g2.setStroke(EntityActionIcon.ACTION_STROKE);
            g2.drawLine(addX - addLegLen, addY, addX + addLegLen, addY);
            g2.drawLine(addX, addY - addLegLen, addX, addY + addLegLen);

            g2.setStroke(oldStroke);
            g2.setColor(oldColor);

            g2.translate(parCX, parCY);

            double parentChildScaleFactor = 0.6;
            g2.scale(parentChildScaleFactor, parentChildScaleFactor);
            int parX = -baseSize / 2;
            int parY = -baseSize / 2;
            icon.paintIcon(c, g2, parX, parY);
            g2.scale(1 / parentChildScaleFactor, 1 / parentChildScaleFactor);
            g2.translate(-parCX, -parCY);

            g2.translate(childCX, childCY);
            int childX = -baseSize / 2;
            int childY = -baseSize / 2;
            g2.scale(parentChildScaleFactor, parentChildScaleFactor);
            icon.paintIcon(c, g2, childX, childY);
            g2.scale(1 / parentChildScaleFactor, 1 / parentChildScaleFactor);
            g2.translate(-childCX, -childCY);
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
