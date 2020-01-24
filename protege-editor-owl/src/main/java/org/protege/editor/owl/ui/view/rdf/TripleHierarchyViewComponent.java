package org.protege.editor.owl.ui.view.rdf;

import org.apache.jena.graph.Triple;
import org.protege.editor.core.ui.menu.PopupMenuId;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewMode;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.action.AbstractOWLTreeAction;
import org.protege.editor.owl.ui.renderer.DeleteEntityIcon;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.renderer.OWLClassIcon;
import org.protege.editor.owl.ui.renderer.OWLEntityIcon;
import org.protege.editor.owl.ui.tree.OWLTreeDragAndDropHandler;
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
import java.util.List;
import java.util.*;

/**
 * TODO: under developing
 * Created by @ssz on 23.11.2019.
 *
 * @see RDFTripleTree
 */
public class TripleHierarchyViewComponent extends AbstractOWLSelectionViewComponent
        implements Findable<Triple>, SelectionDriver, CreateNewTarget, Deleteable, HasDisplayDeprecatedEntities {

    private static final Icon ADD_ICON = OWLIcons.getIcon("ontology.png");
    private static final Icon DELETE_ICON = new DeleteEntityIcon(new OWLClassIcon(OWLClassIcon.Type.PRIMITIVE, OWLEntityIcon.FillType.HOLLOW));

    private static final String ADD_GROUP = "A";
    private static final String DELETE_GROUP = "B";
    private static final String FIRST_SLOT = "A";

    private final ViewModeComponent<ObjectTree<Triple>> viewModeComponent = new ViewModeComponent<>();
    private ObjectTree<Triple> tree;
    private TreeSelectionListener listener;
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
        TreeCellRenderer treeCellRenderer = tree.getCellRenderer();
        if (treeCellRenderer instanceof OWLCellRenderer) {
            ((OWLCellRenderer) treeCellRenderer).setHighlightKeywords(true);
        }

        viewModeComponent.add(tree, ViewMode.ASSERTED, true);

        performExtraInitialisation();
        // todo:
        //Triple entity = getSelectedNode();
        //if (entity != null) {
        //setGlobalSelection(entity);
        //}
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
        DisposableAction add = new AbstractOWLTreeAction<Triple>("Add root triple", ADD_ICON, getTree().getSelectionModel()) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewObject();
            }

            @Override
            protected boolean canPerform(Triple cls) {
                return canCreateNew();
            }
        };

        addAction(add, ADD_GROUP, FIRST_SLOT);

        // TODO:
        OWLSelectionViewAction delete = new OWLSelectionViewAction("Delete triple", DELETE_ICON) {
            @Override
            public void updateState() {

            }

            @Override
            public void actionPerformed(ActionEvent e) {

            }

            @Override
            public void dispose() {

            }
        };

        addAction(delete, DELETE_GROUP, FIRST_SLOT);

        // TODO: right now D'N'D is not allowed
        getTree().setDragAndDropHandler(new OWLTreeDragAndDropHandler<Triple>() {
            @Override
            public boolean canDrop(Object child, Object parent) {
                return false;
            }

            @Override
            public void move(Triple child, Triple fromParent, Triple toParent) {
            }

            @Override
            public void add(Triple child, Triple parent) {
            }
        });
        getTree().setPopupMenuId(new PopupMenuId("[TripleHierarchy]"));
    }

    protected RDFHierarchyProvider getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getRDFTripleHierarchyProvider();
    }

    @Override
    public boolean canCreateNew() {
        return false;
    }

    @Override
    public void createNewObject() {
        // TODO: not ready
        /*TreePath path = getTree().getSelectionModel().getSelectionPath();
        OWLObjectTreeNode<Triple> t = (OWLObjectTreeNode<Triple>) path.getLastPathComponent(); // todo: npe if unselected
        AddTriplePanel panel = new AddTriplePanel();
        int res = new UIHelper(getOWLEditorKit()).showValidatingDialog("Create a new triple", panel, null);
        if (res == JOptionPane.OK_OPTION) {
        }*/
        throw new UnsupportedOperationException();
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public Optional<OWLObject> getSelection() {
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
            if (entity == null) {
                return;
            }
            Triple t = getTree().getSelectedOWLObject();
            if (Objects.equals(t, entity)) {
                return;
            }
            getTree().setSelectedOWLObject(entity);
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
            View view = getView();
            if (view != null && !view.isPinned()) {
                view.setPinned(true); // so that we don't follow the selection
                // todo:
                //setGlobalSelection(selEntity);
                view.setPinned(false);
            }
            // todo:
            /*else {
                setGlobalSelection(selEntity);
            }*/
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
        // TODO:
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
}

