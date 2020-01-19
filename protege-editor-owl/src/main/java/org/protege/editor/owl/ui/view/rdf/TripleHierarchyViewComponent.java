package org.protege.editor.owl.ui.view.rdf;

import org.apache.jena.graph.Triple;
import org.protege.editor.core.ui.menu.PopupMenuId;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewMode;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.ui.action.AbstractOWLTreeAction;
import org.protege.editor.owl.ui.renderer.AddChildIcon;
import org.protege.editor.owl.ui.renderer.AddSiblingIcon;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.renderer.OWLClassIcon;
import org.protege.editor.owl.ui.tree.OWLTreeDragAndDropHandler;
import org.protege.editor.owl.ui.tree.OWLTreePreferences;
import org.protege.editor.owl.ui.tree.ObjectTree;
import org.protege.editor.owl.ui.view.*;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * TODO:
 * Created by @ssz on 23.11.2019.
 *
 * @see org.protege.editor.owl.ui.view.cls.ToldOWLClassHierarchyViewComponent - example
 */
public class TripleHierarchyViewComponent extends SelectionViewComponent
        implements Findable<Triple>, SelectionDriver,
        CreateNewTarget, CreateNewChildTarget, CreateNewSiblingTarget, Deleteable, HasDisplayDeprecatedEntities {

    private static final OWLClassIcon OWL_CLASS_ICON = new OWLClassIcon();
    private static final Icon ADD_SUB_ICON = new AddChildIcon(OWL_CLASS_ICON);
    private static final Icon ADD_SIBLING_ICON = new AddSiblingIcon(OWL_CLASS_ICON);
    private static final String ADD_GROUP = "A";
    private static final String DELETE_GROUP = "B";
    private static final String FIRST_SLOT = "A";
    private static final String SECOND_SLOT = "B";
    private final ViewModeComponent<ObjectTree<Triple>> viewModeComponent = new ViewModeComponent<>();
    private ObjectTree<Triple> tree;
    private TreeSelectionListener listener;
    private Deleter hierarchyDeleter;
    private ChangeListenerMediator deletableChangeListenerMediator = new ChangeListenerMediator();

    @Override
    protected OWLObject updateView() {
        //return updateView(getOWLWorkspace().getOWLSelectionModel().getLastSelectedClass());
        return null;
    }

    @Override
    public List<Triple> find(String match) {
        //return new ArrayList<>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLClasses(match));
        return null;
    }

    @Override
    public final void initialiseView() {
        setLayout(new BorderLayout(0, 0));
        add(viewModeComponent, BorderLayout.CENTER);
        tree = new RDFTripleTree(getOWLEditorKit(), getHierarchyProvider());

        // render keywords should be on now for class expressions
        final TreeCellRenderer treeCellRenderer = tree.getCellRenderer();
        if (treeCellRenderer instanceof OWLCellRenderer) {
            ((OWLCellRenderer) treeCellRenderer).setHighlightKeywords(true);
        }

        viewModeComponent.add(tree, ViewMode.ASSERTED, true);

        performExtraInitialisation();
        Triple entity = getSelectedNode();
        if (entity != null) {
            // todo:
            //setGlobalSelection(entity);
        }
        TreeModelListener treeModelListener = new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                ensureSelection();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                ensureSelection();
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                ensureSelection();
            }
        };
        tree.getModel().addTreeModelListener(treeModelListener);

        tree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                transmitSelection();
            }
        });

        /*hierarchyDeleter = new Deleter(getOWLEditorKit(),
                getHierarchyProvider(),
                () -> assertedTree.getSelectedOWLObjects().stream(),
                getCollectiveTypeName());*/
        listener = e -> transmitSelection();
        tree.addTreeSelectionListener(listener);
    }

    public void performExtraInitialisation() {
        // Add in the manipulation actions - we won't need to keep track
        // of these, as this will be done by the view - i.e. we won't
        // need to dispose of these actions.

        AbstractOWLTreeAction<Triple> addSubTripleAction =
                new AbstractOWLTreeAction<Triple>("Add subtriple",
                        ADD_SUB_ICON,
                        getTree().getSelectionModel()) {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        createNewChild();
                    }

                    @Override
                    protected boolean canPerform(Triple cls) {
                        return canCreateNewChild();
                    }
                };

        addAction(addSubTripleAction, ADD_GROUP, FIRST_SLOT);

        AbstractOWLTreeAction<Triple> addSiblingTripleAction =
                new AbstractOWLTreeAction<Triple>("Add sibling triple",
                        ADD_SIBLING_ICON,
                        getTree().getSelectionModel()) {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        createNewSibling();
                    }

                    @Override
                    protected boolean canPerform(Triple cls) {
                        return canCreateNewSibling();
                    }
                };

        addAction(addSiblingTripleAction, ADD_GROUP, SECOND_SLOT);

        /*DeleteClassAction deleteClassAction =
                new DeleteClassAction(getOWLEditorKit(),
                        () -> getTree().getSelectedOWLObjects().stream()) {
                    @Override
                    public void updateState() {
                        super.updateState();
                        if (isEnabled()) {
                            setEnabled(canDelete());
                        }
                    }
                };

        addAction(deleteClassAction, DELETE_GROUP, FIRST_SLOT);*/

        getTree().setDragAndDropHandler(new OWLTreeDragAndDropHandler<Triple>() {
            @Override
            public boolean canDrop(Object child, Object parent) { //todo:
                //return OWLTreePreferences.getInstance().isTreeDragAndDropEnabled() && child instanceof OWLClass;
                return false;
            }

            @Override
            public void move(Triple child, Triple fromParent, Triple toParent) {
                if (!OWLTreePreferences.getInstance().isTreeDragAndDropEnabled()) {
                    return;
                }
                handleMove(child, fromParent, toParent);
            }

            @Override
            public void add(Triple child, Triple parent) {
                if (!OWLTreePreferences.getInstance().isTreeDragAndDropEnabled()) {
                    return;
                }
                handleAdd(child, parent);
            }
        });
        getTree().setPopupMenuId(new PopupMenuId("[TripleHierarchy]"));
    }

    private void handleAdd(Triple child, Triple parent) {
        // TODO:
    }

    private void handleMove(Triple child, Triple fromParent, Triple toParent) {
        // TODO:
    }

    protected RDFHierarchyProvider getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getRDFTripleHierarchyProvider();
    }

    @Override
    public boolean canCreateNew() {
        return false;
    }

    @Override
    public boolean canCreateNewChild() {
        return canCreateNew() && !getSelectedEntities().isEmpty();
    }

    @Override
    public boolean canCreateNewSibling() {
        return canCreateNew() && !getSelectedEntities().isEmpty();
    }

    @Override
    public void createNewChild() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createNewObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createNewSibling() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public Optional<OWLObject> getSelection() {
        //return Optional.ofNullable(getSelectedEntity());
        return Optional.empty();
    }

    public void setSelectedEntity(Triple entity) {
        getTree().setSelectedOWLObject(entity);
    }

    public Triple getSelectedNode() {
        return getTree().getSelectedOWLObject();
    }

    public Set<Triple> getSelectedEntities() {
        return new HashSet<>(getTree().getSelectedOWLObjects());
    }

    private void ensureSelection() {
        SwingUtilities.invokeLater(() -> {
            Triple entity = getSelectedNode();
            if (entity != null) {
                Triple treeSel = getTree().getSelectedOWLObject();
                if (treeSel == null || !treeSel.equals(entity)) {
                    getTree().setSelectedOWLObject(entity);
                }
            }
        });
    }

    @Override
    public boolean requestFocusInWindow() {
        return getTree().requestFocusInWindow();
    }

    protected ObjectTree<Triple> getTree() {
        Optional<ViewMode> viewMode = getView().getViewMode();
        return viewModeComponent.getComponentForViewMode(viewMode);
    }

    protected void transmitSelection() {
        deletableChangeListenerMediator.fireStateChanged(this);
        Triple triple = getSelectedNode();
        if (triple != null) {
            final View view = getView();
            if (view != null && !view.isPinned()) {
                view.setPinned(true); // so that we don't follow the selection
                // todo:
                //setGlobalSelection(selEntity);
                view.setPinned(false);
            } else {
                // todo:
                //setGlobalSelection(selEntity);
            }
        } else {
            setGlobalSelection(null);
        }
        //updateHeader(selEntity);
    }

    protected Triple updateView(Triple selEntity) {
        if (getTree().getSelectedOWLObject() == null) {
            if (selEntity != null) {
                getTree().setSelectedOWLObject(selEntity);
            }
        } else {
            if (!getTree().getSelectedOWLObject().equals(selEntity)) {
                getTree().setSelectedOWLObject(selEntity);
            }
        }
        return selEntity;
    }

    @Override
    public void disposeView() {
        // Dispose of the assertedTree selection listener
        if (tree != null) {
            tree.removeTreeSelectionListener(listener);
            tree.dispose();
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        deletableChangeListenerMediator.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        deletableChangeListenerMediator.removeChangeListener(listener);
    }

    @Override
    public void handleDelete() {
        hierarchyDeleter.performDeletion();
    }

    @Override
    public boolean canDelete() {
        return !getTree().getSelectedOWLObjects().isEmpty();
    }

    @Override
    public void show(Triple owlEntity) {
        getTree().setSelectedOWLObject(owlEntity);
    }

    @Override
    public void setShowDeprecatedEntities(boolean showDeprecatedEntities) {
        if (showDeprecatedEntities) {
            getHierarchyProvider().setFilter(e -> true);
        } else {
            getHierarchyProvider().setFilter(this::isNotDeprecated);
        }
    }

    private boolean isNotDeprecated(Triple e) {
        //return !OWLUtilities.isDeprecated(getOWLModelManager(), e);
        return false;
    }

    /**
     * TODO:
     *
     * @see org.protege.editor.owl.ui.action.OWLObjectHierarchyDeleter
     */
    static class Deleter {

        public Deleter() {
        }

        public void performDeletion() {

        }
    }

}

