package org.protege.editor.owl.ui.framelist;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.core.ui.util.VerifyingOptionPane;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.util.OWLAxiomInstance;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.axiom.AxiomAnnotationPanel;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.frame.*;
import org.protege.editor.owl.ui.preferences.GeneralPreferencesPanel;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.transfer.OWLObjectDataFlavor;
import org.protege.editor.owl.ui.view.*;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br>
 * <br>
 * An OWLFrameList is a common component that displays sections and
 * section content. Most of the standard component in protege use this.
 */
public class OWLFrameList<R> extends MList<Object>
        implements LinkedObjectComponent, DropTargetListener, Copyable<OWLObject>,
        Pasteable, Cuttable, Deleteable, RefreshableComponent {

    public static final Color INFERRED_BG_COLOR = new Color(255, 255, 215);
    private static final Logger LOGGER = LoggerFactory.getLogger(OWLFrameList.class);
    private static final int EDITOR_SCREEN_MARGIN = 100;
    private static final Border inferredBorder = new OWLFrameListInferredSectionRowBorder();
    private final OWLModelManagerListener modelManagerListener;
    private final OWLEditorKit editorKit;
    private final OWLFrame<R> frame;
    private final LinkedObjectComponentMediator mediator;
    private final List<MListButton> inferredRowButtons;
    private final AxiomAnnotationButton axiomAnnotationButton;
    private final ChangeListenerMediator changeListenerMediator;
    private final ListSelectionListener selListener = this::handleSelectionEvent;

    private OWLFrameListener listener;
    private JPopupMenu popupMenu;
    private List<OWLFrameListPopupMenuAction<R>> actions;

    public OWLFrameList(OWLEditorKit editorKit, OWLFrame<R> frame) {
        this.editorKit = Objects.requireNonNull(editorKit);
        this.frame = Objects.requireNonNull(frame);
        OWLFrameListRenderer cellRenderer = new OWLFrameListRenderer(editorKit);
        setCellRenderer(cellRenderer);

        getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        mediator = new LinkedObjectComponentMediator(editorKit, this);

        setupFrameListener();
        setupKeyboardHandlers();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setFixedCellWidth(OWLFrameList.this.getWidth());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                repaint();
            }
        });

        createPopupMenu();

        inferredRowButtons = new ArrayList<>();
        inferredRowButtons.add(new ExplainButton(e -> invokeExplanationHandler()));

        axiomAnnotationButton = new AxiomAnnotationButton(event -> invokeAxiomAnnotationHandler());

        changeListenerMediator = new ChangeListenerMediator();
        addListSelectionListener(selListener);

        setUI(new OWLFrameListUI());

        modelManagerListener = event -> {
            if (event.isType(EventType.ONTOLOGY_CLASSIFIED) || event.isType(EventType.REASONER_CHANGED)) {
                setRootObject(getRootObject());
            }
        };
        editorKit.getOWLModelManager().addListener(modelManagerListener);
    }


    public void refreshComponent() {
        refillRows();
    }

    public OWLFrame<R> getFrame() {
        return frame;
    }

    private void setupFrameListener() {
        listener = this::refillRows;
        frame.addFrameListener(listener);
    }

    @Override
    public void updateUI() {
    }

    @Override
    protected Border createListItemBorder(JList<?> list,
                                          Object value,
                                          int index,
                                          boolean isSelected,
                                          boolean cellHasFocus) {
        Border border = super.createListItemBorder(list, value, index, isSelected, cellHasFocus);
        if (value instanceof OWLFrameSectionRow) {
            OWLFrameSectionRow<?, ?, ?> row = (OWLFrameSectionRow<?, ?, ?>) value;
            if (row.isInferred()) {
                border = BorderFactory.createCompoundBorder(border, inferredBorder);
            }
        }
        return border;
    }

    @Override
    protected List<MListButton> getButtons(Object value) {
        List<MListButton> buttons = new ArrayList<>(super.getButtons(value));
        if (value instanceof OWLFrameSectionRow) {
            OWLFrameSectionRow<?, ?, ?> frameRow = (OWLFrameSectionRow<?, ?, ?>) value;
            buttons.add(axiomAnnotationButton);
            axiomAnnotationButton.setAnnotationPresent(isAnnotationPresent(frameRow));

            if (getExplanationManager().hasExplanation(frameRow.getAxiom())) {
                buttons.addAll(inferredRowButtons);
            }
        }
        if (value instanceof AbstractOWLFrameSectionRow) {
            List<MListButton> additional = ((AbstractOWLFrameSectionRow<?, ?, ?>) value).getAdditionalButtons();
            if (!additional.isEmpty()) {
                buttons.addAll(additional);
            }
        }
        if (value instanceof AbstractOWLFrameSection) {
            buttons.addAll(((AbstractOWLFrameSection<?, ?, ?>) value).getAdditionalButtons());
        }
        return buttons;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected String getRowName(Object rowObject) {
        if (rowObject instanceof OWLFrameSectionRow) {
            OWLFrameSectionRow obj = (OWLFrameSectionRow) rowObject;
            return obj.getFrameSection().getRowLabel(obj);
        }
        return null;
    }

    @Override
    protected Color getItemBackgroundColor(MListItem item) {
        if (item instanceof AbstractOWLFrameSectionRow) {
            if (((AbstractOWLFrameSectionRow<?, ?, ?>) item).isInferred()) {
                return INFERRED_BG_COLOR;
            }
        }
        return super.getItemBackgroundColor(item);
    }

    private void setupKeyboardHandlers() {
        InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_SEL");
        am.put("DELETE_SEL", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDelete();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ADD");
        am.put("ADD", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEdit();
            }
        });
    }

    private void showPopupMenu(MouseEvent e) {
        for (OWLFrameListPopupMenuAction<?> action : actions) {
            action.updateState();
        }
        popupMenu.show(this, e.getX(), e.getY());
    }

    private void createPopupMenu() {
        actions = new ArrayList<>();
        popupMenu = new JPopupMenu();
        addToPopupMenu(new SwitchToDefiningOntologyAction<>());
        addToPopupMenu(new PullIntoActiveOntologyAction<>());
        addToPopupMenu(new MoveAxiomsToOntologyAction<>());
    }

    public void addToPopupMenu(OWLFrameListPopupMenuAction<R> action) {
        setupMenuItem(action);
        popupMenu.add(action);
    }

    private void setupMenuItem(OWLFrameListPopupMenuAction<R> action) {
        action.setup(editorKit, this);
        try {
            action.initialise();
            actions.add(action);
        } catch (Exception e) {
            LOGGER.debug("An error occurred whilst setting up a menu item in the popup menu in a Frame List. " +
                    "The menu item will not be displayed in the popup menu.", e);
        }
    }

    public R getRootObject() {
        return frame.getRootObject();
    }

    public void setRootObject(R rootObject) {
        frame.setRootObject(rootObject);
        changeListenerMediator.fireStateChanged(this);
    }

    public void dispose() {
        removeListSelectionListener(selListener);
        frame.removeFrameListener(listener);
        for (OWLFrameListPopupMenuAction<R> action : actions) {
            try {
                action.dispose();
            } catch (Exception e) {
                LOGGER.debug("An error occurred whilst disposing of a menu item in the popup menu in a Frame List", e);
            }
        }
        frame.dispose();
        editorKit.getOWLModelManager().removeListener(modelManagerListener);
    }

    private void refillRows() {
        List<OWLFrameObject<?>> rows = new ArrayList<>();
        for (OWLFrameSection<? super R, ?, ?> section : frame.getFrameSections()) {
            rows.add(section);
            rows.addAll(section.getRows());
        }
        setListData(rows.toArray());
    }

    @Override
    public boolean canDelete() {
        return getSelectedIndex() != -1;
    }

    @Override
    public void handleDelete() {
        int[] selIndices = getSelectedIndices();
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (int selIndex : selIndices) {
            Object val = getModel().getElementAt(selIndex);
            if (val instanceof OWLFrameSectionRow) {
                OWLFrameSectionRow<?, ?, ?> row = (OWLFrameSectionRow<?, ?, ?>) val;
                changes.addAll(row.getDeletionChanges());
            }
        }
        editorKit.getModelManager().applyChanges(changes);
    }

    @Override
    protected void handleAdd() {
        handleEdit();
    }

    @Override
    protected void handleEdit() {
        if (getRootObject() == null) {
            return;
        }
        Object val = getSelectedValue();
        if (!(val instanceof OWLFrameObject)) {
            return;
        }
        if (val instanceof OWLFrameSection) {
            if (!((OWLFrameSection<?, ?, ?>) val).canAdd()) {
                return;
            }
        } else {
            if (!((OWLFrameSectionRow<?, ?, ?>) val).isEditable()) {
                return;
            }
        }
        OWLFrameObject<?> row = (OWLFrameObject<?>) val;
        showEditorDialog(row, editor -> editor.getHandler().handleEditingFinished(editor.getEditedObjects()));
    }

    protected void handleSelectionEvent(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        Object sel = getSelectedValue();
        if (sel instanceof OWLFrameSectionRow) {
            OWLFrameSectionRow<?, ?, ?> row = (OWLFrameSectionRow<?, ?, ?>) sel;
            OWLAxiom ax = row.getAxiom();
            if (ax != null && row.getOntology() != null) {
                editorKit.getWorkspace().getOWLSelectionModel().setSelectedAxiom(new OWLAxiomInstance(ax, row.getOntology()));
            }
        }
        changeListenerMediator.fireStateChanged(OWLFrameList.this);
    }

    private <X> void showEditorDialog(OWLFrameObject<X> frameObject, EditHandler<X> handler) {
        // If we don't have any editing component then just return
        final boolean isRowEditor = frameObject instanceof OWLFrameSectionRow;
        OWLObjectEditor<X> editor = frameObject.getEditor();
        if (editor == null) {
            return;
        }
        if (editor instanceof JWindow) {
            ((JWindow) editor).setVisible(true);
            return;
        }
        if (editor instanceof Wizard) {
            int ret = ((Wizard) editor).showModalDialog();
            if (ret == Wizard.FINISH_RETURN_CODE) {
                handler.handleEditFinished(editor);
            }
            return;
        }
        // Create the editing component dialog - we use an option pane
        // so that the buttons and keyboard actions are what are expected by the user.
        final JComponent editorComponent = editor.getEditorComponent();
        final VerifyingOptionPane optionPane = new VerifyingOptionPane(editorComponent) {

            @Override
            public void selectInitialValue() {
                // This is overriden so that the option pane dialog default button doesn't get the focus.
            }
        };
        final InputVerificationStatusChangedListener verificationListener =
                verified -> optionPane.setOKEnabled(verified && frameObject.checkEditorResults(editor));
        // if the editor is verifying, will need to prevent the OK button from
        // being available
        if (editor instanceof VerifiedInputEditor) {
            ((VerifiedInputEditor) editor).addStatusChangedListener(verificationListener);
        }
        final Component parent = getDialogParent();
        final JDialog dlg = optionPane.createDialog(parent, null);
        // The editor shouldn't be modal (or should it?)
        dlg.setModal(false);
        dlg.setResizable(true);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentHidden(ComponentEvent e) {
                Object retVal = optionPane.getValue();
                editorComponent.setPreferredSize(editorComponent.getSize());
                if (retVal != null && retVal.equals(JOptionPane.OK_OPTION)) {
                    handler.handleEditFinished(editor);
                }
                setSelectedValue(frameObject, true);
                if (editor instanceof VerifiedInputEditor) {
                    ((VerifiedInputEditor) editor).removeStatusChangedListener(verificationListener);
                }
                // editor.dispose();
                if (isRowEditor) {
                    editor.dispose();
                }
            }
        });

        Object rootObject = null;

        if (frameObject instanceof OWLFrameSectionRow) {
            rootObject = ((OWLFrameSectionRow<?, ?, ?>) frameObject).getFrameSection().getRootObject();
        } else if (frameObject instanceof OWLFrameSection) {
            rootObject = ((OWLFrameSection<?, ?, ?>) frameObject).getRootObject();
        }

        if (rootObject instanceof OWLObject) {
            dlg.setTitle(editorKit.getModelManager().getRendering(rootObject));
        } else if (rootObject != null) {
            dlg.setTitle(rootObject.toString());
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dlgSize = dlg.getSize();
        if (dlg.getHeight() > screenSize.height - EDITOR_SCREEN_MARGIN) {
            dlgSize.height = screenSize.height - EDITOR_SCREEN_MARGIN;
        }
        if (dlg.getWidth() > screenSize.width - EDITOR_SCREEN_MARGIN) {
            dlgSize.width = screenSize.width - EDITOR_SCREEN_MARGIN;
        }
        dlg.setSize(dlgSize);
        dlg.setVisible(true);
    }

    private Component getDialogParent() {
        // @@TODO move prefs somewhere more central
        Preferences prefs = PreferencesManager.getInstance().getApplicationPreferences(ProtegeApplication.ID);
        return prefs.getBoolean(GeneralPreferencesPanel.DIALOGS_ALWAYS_CENTRED, false)
                ? SwingUtilities.getAncestorOfClass(Frame.class, getParent())
                : getParent();
    }

    protected void invokeExplanationHandler() {
        Object obj = getSelectedValue();
        if (!(obj instanceof OWLFrameSectionRow)) {
            return;
        }
        OWLFrameSectionRow<?, ?, ?> row = (OWLFrameSectionRow<?, ?, ?>) obj;
        OWLAxiom ax = row.getAxiom();
        if (getExplanationManager().hasExplanation(ax)) {
            JFrame frame = ProtegeManager.getInstance().getFrame(editorKit.getWorkspace());
            getExplanationManager().handleExplain(frame, ax);
        }
    }

    protected ExplanationManager getExplanationManager() {
        return editorKit.getModelManager().getExplanationManager();
    }

    private void invokeAxiomAnnotationHandler() {
        Object obj = getSelectedValue();
        if (!(obj instanceof OWLFrameSectionRow)) {
            return;
        }
        OWLFrameSectionRow<?, ?, ?> row = (OWLFrameSectionRow<?, ?, ?>) obj;
        OWLAxiom ax = row.getAxiom();

        AxiomAnnotationPanel axiomAnnotationPanel;
        axiomAnnotationPanel = new AxiomAnnotationPanel(editorKit);

        OWLOntology ontology = row.getOntology();
        final OWLAxiomInstance axiomInstance;
        if (ontology != null) {
            axiomInstance = new OWLAxiomInstance(ax, ontology);
        } else {
            OWLOntology activeOntology = editorKit.getOWLModelManager().getActiveOntology();
            axiomInstance = new OWLAxiomInstance(ax, activeOntology);
        }
        axiomAnnotationPanel.setAxiomInstance(axiomInstance);
        new UIHelper(editorKit).showDialog("Annotations for " + ax.getAxiomType().toString(),
                axiomAnnotationPanel, JOptionPane.CLOSED_OPTION);
        axiomAnnotationPanel.dispose();
    }

    private boolean isAnnotationPresent(OWLFrameSectionRow<?, ?, ?> row) {
        return row.getAxiom().isAnnotated();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public OWLObject getLinkedObject() {
        return mediator.getLinkedObject();
    }

    public void setLinkedObject(OWLObject object) {
        mediator.setLinkedObject(object);
    }

    /**
     * Gets the location of the mouse relative to the rendering cell that it is over.
     */
    @Override
    public Point getMouseCellLocation() {
        Point mouseLoc = getMousePosition();
        if (mouseLoc == null) {
            return null;
        }
        int index = locationToIndex(mouseLoc);
        Rectangle cellRect = getCellBounds(index, index);
        return new Point(mouseLoc.x - cellRect.x, mouseLoc.y - cellRect.y);
    }

    @Override
    public Rectangle getMouseCellRect() {
        Point loc = getMousePosition();
        if (loc == null) {
            return null;
        }
        int index = locationToIndex(loc);
        return getCellBounds(index, index);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        repaint();
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        repaint();
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        repaint();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (!dtde.getTransferable().isDataFlavorSupported(OWLObjectDataFlavor.OWL_OBJECT_DATA_FLAVOR)) {
            return;
        }
        try {
            List<OWLObject> object = (List<OWLObject>) dtde.getTransferable().getTransferData(OWLObjectDataFlavor.OWL_OBJECT_DATA_FLAVOR);
            OWLFrameObject<?> frameObject;
            frameObject = (OWLFrameObject<?>) getModel().getElementAt(locationToIndex(dtde.getLocation()));
            dtde.dropComplete(frameObject.dropObjects(object));
        } catch (Exception e) {
            LOGGER.error("Ex:", e);
        }
    }

    @Override
    public boolean canPaste(List<OWLObject> objects) {
        if (getRootObject() == null) {
            return false;
        }
        return getSelectedValue() instanceof OWLFrameSection
                && ((OWLFrameSection<?, ?, ?>) getSelectedValue()).canAcceptDrop(objects);
    }

    @Override
    public void pasteObjects(List<OWLObject> objects) {
        Object selObject = getSelectedValue();
        if (!(selObject instanceof OWLFrameSection)) {
            return;
        }
        OWLFrameSection<?, ?, ?> section = (OWLFrameSection<?, ?, ?>) selObject;
        if (section.canAcceptDrop(objects)) {
            section.dropObjects(objects);
        }
    }

    @Override
    public boolean canCopy() {
        return getRootObject() != null && getSelectedIndex() != -1;
    }

    @Override
    public List<OWLObject> getObjectsToCopy() {
        return getCuttableObjects();
    }

    @Override
    public boolean canCut() {
        return !getCuttableObjects().isEmpty();
    }

    private List<OWLObject> getCuttableObjects() {
        return selectedValues().flatMap(OWLFrameSectionRow::manipulatableObjects).collect(Collectors.toList());
    }

    private Stream<OWLFrameSectionRow<?, ?, ?>> selectedValues() {
        return getSelectedValuesList().stream()
                .filter(x -> x instanceof OWLFrameSectionRow)
                .map(x -> (OWLFrameSectionRow<?, ?, ?>) x);
    }

    @Override
    public List<OWLObject> cutObjects() {
        List<OWLObject> res = new ArrayList<>();
        List<OWLOntologyChange> changes = new ArrayList<>();
        selectedValues().filter(OWLFrameSectionRow::isInferred).forEach(row -> {
            row.manipulatableObjects().forEach(res::add);
            changes.add(new RemoveAxiom(row.getOntology(), row.getAxiom()));
        });
        editorKit.getModelManager().applyChanges(changes);
        return res;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeListenerMediator.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeListenerMediator.removeChangeListener(changeListener);
    }

    @Override
    public void setLayoutOrientation(int layoutOrientation) {
        throw new OWLRuntimeException("NOT ALLOWED");
    }

    private interface EditHandler<X> {

        void handleEditFinished(OWLObjectEditor<X> editor);
    }

    /**
     * An override of the BasicListUI.
     * This is necessary because of the very poor performance of the default Java implementation.
     * Also, this list UI uses a hybrid fixed/non-fixed cell size approach - specific to AbstractOWLFrameSectionRow.
     */
    public class OWLFrameListUI extends BasicListUI {

        private Point lastMouseDownPoint;
        private int[] cumulativeCellHeight;


        // As BasicListUI is implemented with windows keystrokes, we need to
        // return a mouse listener that ignores the (bad) default toggle behaviour when Ctrl is pressed.
        // This would prevent mac users from using this very common key combination (right-click)
        // instead, add handling for the context menu and double click editing
        // Also must implement discontiguous multi-selection
        @Override
        protected MouseInputListener createMouseInputListener() {
            return new MouseInputHandler() {

                boolean showingPopup = false;

                @Override
                public void mousePressed(MouseEvent e) {
                    showingPopup = false;
                    lastMouseDownPoint = e.getPoint();
                    if (e.isPopupTrigger()) {
                        showingPopup = true;
                        showPopupMenu(e);
                    } else if ((e.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0) {
                        int sel = locationToIndex(OWLFrameList.this, lastMouseDownPoint);
                        handleModifiedSelectionEvent(sel);
                    } else {
                        super.mousePressed(e);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showingPopup = true;
                        showPopupMenu(e);
                    } else if (e.getClickCount() == 2) {
                        if (!showingPopup) {
                            handleEdit();
                        }
                    } else {
                        super.mouseReleased(e);
                    }
                }
            };
        }


        private void handleModifiedSelectionEvent(int index) {
            if (isSelectedIndex(index)) {
                removeSelectionInterval(index, index);
            } else if (getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION || getSelectedIndex() == -1) {
                addSelectionInterval(index, index);
            }
        }

        protected boolean isFixedCellHeightRow(int index) {
            Object value = getModel().getElementAt(index);
            if (!(value instanceof AbstractOWLFrameSectionRow)) {
                return false;
            }
            AbstractOWLFrameSectionRow<?, ?, ?> row = (AbstractOWLFrameSectionRow<?, ?, ?>) value;
            if (index < getModel().getSize() - 1) {
                if (getModel().getElementAt(index + 1) instanceof AbstractOWLFrameSection) {
                    return false;
                }
            }
            return row.isFixedHeight();
        }

        @Override
        protected void updateLayoutState() {
            cumulativeCellHeight = new int[list.getModel().getSize()];
            /*
             * If both JList fixedCellWidth and fixedCellHeight have been set,
             * then initialize cellWidth and cellHeight, and set cellHeights to null.
             */
            int fixedCellHeight = list.getFixedCellHeight();
            int fixedCellWidth = list.getFixedCellWidth();
            cellWidth = fixedCellWidth;
            if (fixedCellHeight != -1) {
                cellHeight = fixedCellHeight;
                cellHeights = null;
            } else {
                cellHeight = -1;
                cellHeights = new int[list.getModel().getSize()];
            }
            /*
             * If either of JList fixedCellWidth and fixedCellHeight haven't been set,
             * then initialize cellWidth and cellHeights by scanning through the entire model.
             * Note: if the renderer is null, we just set cellWidth and cellHeights[*] to zero,
             * if they're not set already.
             */
            if (fixedCellWidth != -1 && fixedCellHeight != -1) {
                return;
            }
            @SuppressWarnings("unchecked") JList<Object> _list = list;
            ListModel<Object> dataModel = _list.getModel();
            int dataModelSize = dataModel.getSize();
            ListCellRenderer<Object> renderer = _list.getCellRenderer();
            if (renderer == null) {
                if (cellWidth == -1) {
                    cellWidth = 0;
                }
                if (cellHeights == null) {
                    cellHeights = new int[dataModelSize];
                }
                for (int index = 0; index < dataModelSize; index++) {
                    cellHeights[index] = 0;
                }
                return;
            }
            int cumulativeHeight = 0;
            for (int index = 0; index < dataModelSize; index++) {
                Object value = dataModel.getElementAt(index);
                if (isFixedCellHeightRow(index)) {
                    if (fixedCellHeight == -1) {
                        cellHeights[index] = 22;
                    }
                } else {
                    Component c = renderer.getListCellRendererComponent(_list, value, index, false, false);
                    rendererPane.add(c);
                    Dimension cellSize = c.getPreferredSize();
                    if (fixedCellWidth == -1) {
                        cellWidth = Math.max(cellSize.width, cellWidth);
                    }
                    if (fixedCellHeight == -1) {
                        cellHeights[index] = cellSize.height;
                    }
                }
                cumulativeHeight += cellHeights[index];
                cumulativeCellHeight[index] = cumulativeHeight;
            }
        }

        @Override
        public Rectangle getCellBounds(JList list, int index1, int index2) {
            maybeUpdateLayoutState();
            int minIndex = Math.min(index1, index2);
            int maxIndex = Math.max(index1, index2);
            if (minIndex >= list.getModel().getSize()) {
                return null;
            }
            Rectangle minBounds = getCellBounds(list, minIndex);
            if (minBounds == null) {
                return null;
            }
            if (minIndex == maxIndex) {
                return minBounds;
            }
            Rectangle maxBounds = getCellBounds(list, maxIndex);
            if (maxBounds != null) {
                if (minBounds.x != maxBounds.x) {
                    // Different columns
                    minBounds.y = 0;
                    minBounds.height = list.getHeight();
                }
                minBounds.add(maxBounds);
            }
            return minBounds;
        }

        /**
         * Gets the bounds of the specified model index, returning the resulting
         * bounds, or null if <code>index</code> is not valid.
         */
        private Rectangle getCellBounds(JList<?> list, int index) {
            if (index < 0) {
                return new Rectangle();
            }
            maybeUpdateLayoutState();
            if (index >= cumulativeCellHeight.length) {
                return null;
            }
            Insets insets = list.getInsets();
            int x, w, y, h;
            x = insets.left;
            if (index >= cellHeights.length) {
                y = 0;
            } else {
                y = cumulativeCellHeight[index] - cellHeights[index];
            }
            w = list.getWidth() - (insets.left + insets.right);
            h = cellHeights[index];
            return new Rectangle(x, y, w, h);
        }

        /**
         * Paint one List cell: compute the relevant state,
         * get the "rubber stamp" cell renderer component, and then use the CellRendererPane to paint it.
         * Subclasses may want to override this method rather than paint().
         *
         * @see #paint
         */
        @Override
        protected void paintCell(Graphics g,
                                 int row,
                                 Rectangle rowBounds,
                                 ListCellRenderer cellRenderer,
                                 ListModel dataModel,
                                 ListSelectionModel selModel,
                                 int leadIndex) {
            Object value = dataModel.getElementAt(row);
            boolean cellHasFocus = list.hasFocus() && row == leadIndex;
            boolean isSelected = selModel.isSelectedIndex(row);
            @SuppressWarnings("unchecked")
            Component rendererComponent = cellRenderer.getListCellRendererComponent(list, value, row, isSelected, cellHasFocus);
            int cx = rowBounds.x;
            int cy = rowBounds.y;
            int cw = rowBounds.width;
            int ch = rowBounds.height;
            rendererPane.paintComponent(g, rendererComponent, list, cx, cy, cw, ch, true);
        }
    }
}
