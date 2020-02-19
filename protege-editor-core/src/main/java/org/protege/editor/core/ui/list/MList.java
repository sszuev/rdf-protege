package org.protege.editor.core.ui.list;

import org.protege.editor.core.ui.util.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 24-Feb-2007<br>
 * <br>
 *
 * @param <E> the type of the elements of this list
 */
public class MList<E> extends JList<E> {

    private static final Stroke BUTTON_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color FRAME_SECTION_HEADER_FOREGROUND = Color.GRAY;
    private static final Color FRAME_SECTION_HEADER_HIGH_CONTRAST_FOREGROUND = new Color(40, 40, 40);
    private static final Color ITEM_BACKGROUND_COLOR = Color.WHITE;

    private static final int BUTTON_DIMENSION = 16;
    private static final int BUTTON_MARGIN = 2;

    private final ActionListener deleteActionListener = e -> handleDelete();
    private final ActionListener addActionListener = e -> handleAdd();
    private final ActionListener editActionListener = e -> handleEdit();

    private final MListDeleteButton deleteButton = new MListDeleteButton(this.deleteActionListener) {
        @Override
        public String getName() {
            String name = "<html><body>" + super.getName();
            String rowName = MList.this.getRowName(this.getRowObject());
            if (rowName != null) {
                name += " " + rowName.toLowerCase();
            }
            return name + "</body></html>";
        }
    };

    private final MListAddButton addButton = new MListAddButton(this.addActionListener);
    private final MListEditButton editButton = new MListEditButton(this.editActionListener);
    private final MListCellRenderer<E> ren;

    private final List<MListButton> editAndDeleteButtonList = Arrays.asList(editButton, deleteButton);
    private final List<MListButton> editButtonList = Collections.singletonList(editButton);
    private final List<MListButton> deleteButtonList = Collections.singletonList(deleteButton);
    public int lastMousePositionCellIndex = 0;
    private Font sectionHeaderFont = new Font("Lucida Grande", Font.PLAIN, 10);
    private boolean mouseDown;

    public MList() {
        ListCellRenderer<? super E> renderer = getCellRenderer();
        this.ren = new MListCellRenderer<>();
        this.ren.setContentRenderer(renderer);
        super.setCellRenderer(this.ren);
        MouseMotionListener mouseMovementListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved();
            }
        };
        this.addMouseMotionListener(mouseMovementListener);
        MouseListener mouseButtonListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                MList.this.mouseDown = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                MList.this.handleMouseClick(e);
                MList.this.mouseDown = false;
            }

            @Override
            public void mouseExited(MouseEvent event) {
                // leave the component cleanly
                MList.this.repaint();
            }
        };
        this.addMouseListener(mouseButtonListener);
        attachedListenersForCacheResetting();
    }

    private void attachedListenersForCacheResetting() {
        addHierarchyListener(new HierarchyListener() {
            private boolean showing = isShowing();

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (isShowing() != showing) {
                    showing = isShowing();
                    clearCellHeightCache();
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                clearCellHeightCache();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                clearCellHeightCache();
            }
        });
    }

    /**
     * The ListUI caches cell heights.  Setting a fixed height and then clearing the fixed height resets this cache.
     */
    protected void clearCellHeightCache() {
        setFixedCellHeight(10);
        setFixedCellHeight(-1);
    }

    private void handleMouseMoved() {
        if (getModel().getSize() <= 0) {
            return;
        }
        Point pt = getMousePosition();
        // more efficient than repainting the whole component every time the mouse moves
        if (pt == null) {
            MList.this.repaint();
            lastMousePositionCellIndex = 0;
            return;
        }
        int index = locationToIndex(pt);
        // only repaint all the cells the mouse has moved over
        repaint(getCellBounds(Math.min(index, lastMousePositionCellIndex), Math.max(index, lastMousePositionCellIndex)));
        lastMousePositionCellIndex = index;
    }

    protected String getRowName(Object rowObject) {
        return null;
    }

    @Override
    public void setCellRenderer(ListCellRenderer<? super E> renderer) {
        if (this.ren == null) {
            super.setCellRenderer(renderer);
        } else {
            this.ren.setContentRenderer(renderer);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void uncheckedSetCellRenderer(ListCellRenderer renderer) {
        setCellRenderer(renderer);
    }

    protected void handleAdd() {
        if (!(this.getSelectedValue() instanceof MListItem)) {
            return;
        }
        MListItem item = (MListItem) this.getSelectedValue();
        item.handleEdit();
    }

    protected void handleDelete() {
        if (!(this.getSelectedValue() instanceof MListItem)) {
            return;
        }
        MListItem item = (MListItem) this.getSelectedValue();
        item.handleDelete();
    }

    protected void handleEdit() {
        if (!(this.getSelectedValue() instanceof MListItem)) {
            return;
        }
        MListItem item = (MListItem) this.getSelectedValue();
        item.handleEdit();
    }

    private void handleMouseClick(MouseEvent e) {
        for (MListButton button : this.getButtons(this.locationToIndex(e.getPoint()))) {
            if (button.getBounds().contains(e.getPoint())) {
                button.getActionListener().actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, button.getName()));
                return;
            }
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    protected List<MListButton> getButtons(Object value) {
        if (value instanceof MListSectionHeader) {
            return this.getSectionButtons((MListSectionHeader) value);
        }
        if (value instanceof MListItem) {
            return this.getListItemButtons((MListItem) value);
        }
        return Collections.emptyList();
    }

    protected List<MListButton> getSectionButtons(MListSectionHeader header) {
        List<MListButton> buttons = new ArrayList<>();
        if (header.canAdd()) {
            buttons.add(this.addButton);
        }
        return buttons;
    }

    protected List<MListButton> getListItemButtons(MListItem item) {
        if (item.isDeleteable() && item.isEditable()) {
            return this.editAndDeleteButtonList;
        }
        if (item.isDeleteable()) {
            return deleteButtonList;
        }
        if (item.isEditable()) {
            return editButtonList;
        }
        return Collections.emptyList();
    }

    protected Color getItemBackgroundColor(MListItem item) {
        return ITEM_BACKGROUND_COLOR;
    }

    public int getButtonDimension() {
        Font font = getFont();
        if (font == null) {
            return BUTTON_DIMENSION;
        }
        FontMetrics fontMetrics = getFontMetrics(font);
        int height = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent() + fontMetrics.getLeading();
        if (height < 20) {
            height = 20;
        }
        return height;
    }

    protected Border createPaddingBorder(JList<?> list,
                                         Object value,
                                         int index,
                                         boolean isSelected,
                                         boolean cellHasFocus) {
        int bottomMargin = 0;
        if (list.getFixedCellHeight() == -1) {
            if (this.getModel().getSize() > index + 1) {
                bottomMargin = getRowHeightPadding(index);
            }
        }
        return BorderFactory.createMatteBorder(0, 0, bottomMargin, 0, Color.WHITE);
    }

    private int getRowHeightPadding(int index) {
        int bottomMargin = 0;
        if (isNextRowHeader(index)) {
            bottomMargin = 20;
        }
        return bottomMargin;
    }

    protected Border createListItemBorder(JList<?> list,
                                          Object value,
                                          int index,
                                          boolean isSelected,
                                          boolean cellHasFocus) {
        Border internalPadding = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        Border line = BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240));
        Border externalBorder = BorderFactory.createMatteBorder(0, 20, 0, 0, list.getBackground());
        return BorderFactory.createCompoundBorder(externalBorder,
                BorderFactory.createCompoundBorder(line, internalPadding));
    }

    private boolean isNextRowHeader(int index) {
        if (index + 1 < getModel().getSize()) {
            return getModel().getElementAt(index + 1) instanceof MListSectionHeader;
        }
        return false;
    }

    private List<MListButton> getButtons(int index) {
        if (index < 0) {
            return Collections.emptyList();
        }
        Object obj = this.getModel().getElementAt(index);
        List<MListButton> buttons = this.getButtons(obj);
        Rectangle rowBounds = this.getCellBounds(index, index);
        if (rowBounds == null) {
            return buttons;
        }
        int buttonDimension = getButtonDimension();
        if (obj instanceof MListSectionHeader) {
            MListSectionHeader section = (MListSectionHeader) obj;
            FontMetrics fm = this.getGraphics().getFontMetrics(getSectionHeaderFont());
            Rectangle nameBounds = fm.getStringBounds(section.getName(), this.getGraphics()).getBounds();
            int x = 7 + nameBounds.width + 2;
            for (MListButton button : buttons) {
                button.setLocation(x, rowBounds.y + ((rowBounds.height - getRowHeightPadding(index)) / 2 - buttonDimension / 2));
                button.setSize(buttonDimension);
                x += buttonDimension;
                x += 2;
                button.setRowObject(obj);
            }
            return buttons;
        }
        if (!(obj instanceof MListItem)) {
            return buttons;
        }
        int x = rowBounds.width - 2;
        for (MListButton button : buttons) {
            x -= buttonDimension;
            x -= 2;
            button.setLocation(x, rowBounds.y + 1);
            button.setSize(buttonDimension);
            button.setRowObject(obj);
        }
        return buttons;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Point mousePos = this.getMousePosition();
        if (mousePos == null) {
            return null;
        }
        for (MListButton button : this.getButtons(this.locationToIndex(mousePos))) {
            if (button.getBounds().contains(mousePos)) {
                return button.getName();
            }
        }
        int index = this.locationToIndex(event.getPoint());
        if (index == -1) {
            return null;
        }
        Object val = this.getModel().getElementAt(index);
        if (val instanceof MListItem) {
            return ((MListItem) val).getTooltip();
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color oldColor = g.getColor();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Paint buttons
        Stroke oldStroke = g2.getStroke();
        Rectangle clipBound = g.getClipBounds();
        for (int index = 0; index < this.getModel().getSize(); index++) {
            Rectangle rowBounds = this.getCellBounds(index, index);
            if (rowBounds == null) {
                continue;
            }
            if (rowBounds.intersects(clipBound)) {
                paintRow(g2, clipBound, index, rowBounds);
            }
        }
        g.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    private void paintRow(Graphics2D g2, Rectangle clipBound, int index, Rectangle rowBounds) {
        List<MListButton> buttons = this.getButtons(index);
        int endOfButtonRun = -1;
        for (MListButton button : buttons) {
            endOfButtonRun = paintButton(g2, clipBound, endOfButtonRun, button);
        }
        if (this.getModel().getElementAt(index) instanceof MListSectionHeader) {
            paintSectionHeader(g2, index, rowBounds);
        }
    }

    private void paintSectionHeader(Graphics2D g2, int index, Rectangle rowBounds) {
        MListSectionHeader header = (MListSectionHeader) this.getModel().getElementAt(index);
        if (this.isSelectedIndex(index)) {
            g2.setColor(this.getSelectionForeground());
        } else {
            g2.setColor(getSectionHeaderForeground());
        }
        int baseLine = rowBounds.y
                + (getButtonDimension() + BUTTON_MARGIN - g2.getFontMetrics().getHeight()) / 2
                + g2.getFontMetrics().getAscent();
        Font oldFont = g2.getFont();
        g2.setFont(getSectionHeaderFont());
        g2.drawString(header.getName(), 5, baseLine);
        g2.setFont(oldFont);
    }

    private Color getSectionHeaderForeground() {
        return UIUtil.isHighContrastOn() ? FRAME_SECTION_HEADER_HIGH_CONTRAST_FOREGROUND : FRAME_SECTION_HEADER_FOREGROUND;
    }

    private Font getSectionHeaderFont() {
        Font font = getFont();
        if (font == null) {
            return sectionHeaderFont;
        }
        int size = (int) (font.getSize() * 0.9);
        if (sectionHeaderFont.getSize() != size) {
            sectionHeaderFont = sectionHeaderFont.deriveFont(sectionHeaderFont.getStyle(), size);
        }
        return sectionHeaderFont;
    }

    private int paintButton(Graphics2D g2, Rectangle clipBound, int endOfButtonRun, MListButton button) {
        Rectangle buttonBounds = button.getBounds();
        if (buttonBounds == null || !buttonBounds.intersects(clipBound)) {
            return endOfButtonRun;
        }
        g2.setColor(this.getButtonColor(button));
        g2.fillOval(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
        g2.setColor(Color.WHITE);
        Stroke curStroke = g2.getStroke();
        g2.setStroke(BUTTON_STROKE);
        button.paintButtonContent(g2);
        g2.setStroke(curStroke);
        endOfButtonRun = buttonBounds.x + buttonBounds.width + BUTTON_MARGIN;
        return endOfButtonRun;
    }

    private Color getButtonColor(MListButton button) {
        Point pt = this.getMousePosition();
        if (pt == null) {
            return button.getBackground();
        }
        if (!button.getBounds().contains(pt)) {
            return button.getBackground();
        }
        return this.mouseDown ? Color.DARK_GRAY : button.getRollOverColor();
    }

    public class MListCellRenderer<X extends E> implements ListCellRenderer<X> {

        private ListCellRenderer<? super X> contentRenderer;

        private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

        public void setContentRenderer(ListCellRenderer<? super X> renderer) {
            this.contentRenderer = renderer;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends X> list,
                                                      X value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {

            // We now modify the component so that it has a nice border and
            // background
            if (value instanceof MListSectionHeader) {
                JLabel label = (JLabel) defaultListCellRenderer.getListCellRendererComponent(list, " ", index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createCompoundBorder(createPaddingBorder(list, " ", index, isSelected, cellHasFocus),
                        BorderFactory.createEmptyBorder(3, 3, 2, 2)));
                label.setVerticalTextPosition(SwingConstants.TOP);
                return label;
            }
            JComponent component = (JComponent) contentRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Dimension prefSize = component.getPreferredSize();
            component.setOpaque(true);
            if (value instanceof MListItem) {
                Border paddingBorder = createPaddingBorder(list, value, index, isSelected, cellHasFocus);
                Border itemBorder = createListItemBorder(list, value, index, isSelected, cellHasFocus);
                Border border = BorderFactory.createCompoundBorder(paddingBorder, itemBorder);
                int buttonSpan = getButtons(value).size() * (getButtonDimension() + 2) + BUTTON_MARGIN * 2;
                border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(1, 1, 1, buttonSpan));
                component.setBorder(border);
                if (!isSelected) {
                    component.setBackground(getItemBackgroundColor((MListItem) value));
                }
                if (component instanceof RendererWithInsets) {
                    Insets insets = component.getInsets();
                    prefSize.height = prefSize.height + insets.top + insets.bottom;
                    component.setPreferredSize(prefSize);
                }
            }
            if (isSelected) {
                component.setBackground(list.getSelectionBackground());
            }
            return component;
        }
    }
}
