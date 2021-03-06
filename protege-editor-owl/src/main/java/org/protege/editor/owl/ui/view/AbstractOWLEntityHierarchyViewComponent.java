package org.protege.editor.owl.ui.view;

import org.protege.editor.core.HasUpdateState;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewAction;
import org.protege.editor.core.ui.view.ViewMode;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.model.util.OWLUtilities;
import org.protege.editor.owl.ui.OWLObjectComparatorAdapter;
import org.protege.editor.owl.ui.action.OWLObjectHierarchyDeleter;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.ObjectTree;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Apr 23, 2009<br><br>
 */
public abstract class AbstractOWLEntityHierarchyViewComponent<E extends OWLEntity>
        extends AbstractHierarchyViewComponent<E> implements Findable<E>, Deleteable, HasDisplayDeprecatedEntities {

    private ObjectTree<E> assertedTree;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ObjectTree<E>> inferredTree;

    private TreeSelectionListener listener;

    private OWLObjectHierarchyDeleter<E> hierarchyDeleter;

    private final Logger logger = LoggerFactory.getLogger(AbstractOWLEntityHierarchyViewComponent.class);

    private final ViewModeComponent<ObjectTree<E>> viewModeComponent = new ViewModeComponent<>();

    @Override
    final public void initialiseView() throws Exception {
        setLayout(new BorderLayout(0, 0));
        add(viewModeComponent, BorderLayout.CENTER);
        assertedTree = new OWLModelManagerTree<>(getOWLEditorKit(), getHierarchyProvider());

        // ordering based on default, but putting Nothing at the top
        OWLObjectComparatorAdapter<OWLObject> treeNodeComp = createComparator(getOWLModelManager());
        assertedTree.setObjectComparator(treeNodeComp);


        // render keywords should be on now for class expressions
        final TreeCellRenderer treeCellRenderer = assertedTree.getCellRenderer();
        if (treeCellRenderer instanceof OWLCellRenderer){
            ((OWLCellRenderer) treeCellRenderer).setHighlightKeywords(true);
        }

        viewModeComponent.add(assertedTree, ViewMode.ASSERTED, true);


        performExtraInitialisation();
        E entity = getSelectedEntity();
        if (entity != null) {
            setGlobalSelection(entity);
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
        assertedTree.getModel().addTreeModelListener(treeModelListener);

        assertedTree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                transmitSelection();
            }
        });


        try {
            Optional<OWLHierarchyProvider<E>> inferredHierarchyProvider = getInferredHierarchyProvider();
            if (inferredHierarchyProvider.isPresent()) {
                inferredTree = Optional.of(new OWLModelManagerTree<>(getOWLEditorKit(), inferredHierarchyProvider.get()));
                inferredTree.get().setBackground(OWLFrameList.INFERRED_BG_COLOR);
                inferredTree.get().setObjectComparator(treeNodeComp);
                inferredTree.get().getModel().addTreeModelListener(treeModelListener);
                inferredTree.get().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        transmitSelection();
                    }
                });
                viewModeComponent.add(inferredTree.get(), ViewMode.INFERRED, true);
                getView().addViewMode(ViewMode.ASSERTED);
                getView().addViewMode(ViewMode.INFERRED);
                getView().addViewModeChangedHandler(this::switchViewMode);
            }
            else {
                inferredTree = Optional.empty();
            }
        } catch (Exception e) {
            logger.error("An error occurred whilst getting the inferred hierarchy provider", e);
        }

        hierarchyDeleter = new OWLObjectHierarchyDeleter<>(getOWLEditorKit(),
                getHierarchyProvider(),
                () -> assertedTree.getSelectedObjects().stream(),
                getCollectiveTypeName());
        listener = e -> transmitSelection();
        assertedTree.addTreeSelectionListener(listener);
        inferredTree.ifPresent(eObjectTree -> eObjectTree.addTreeSelectionListener(listener));
    }

    protected boolean isInAssertedMode() {
        return getView().getViewMode().equals(Optional.of(ViewMode.ASSERTED));
    }

    private static OWLObjectComparatorAdapter<OWLObject> createComparator(OWLModelManager modelManager) {
        final Comparator<OWLObject> comp = modelManager.getOWLObjectComparator();
        return new OWLObjectComparatorAdapter<OWLObject>(comp) {
            public int compare(OWLObject o1, OWLObject o2) {
                if (modelManager.getOWLDataFactory().getOWLNothing().equals(o1)) {
                    return -1;
                }
                else if (modelManager.getOWLDataFactory().getOWLNothing().equals(o2)) {
                    return 1;
                }
                else {
                    boolean deprecated1 = OWLUtilities.isDeprecated(modelManager, o1);
                    boolean deprecated2 = OWLUtilities.isDeprecated(modelManager, o2);
                    if (deprecated1 != deprecated2) {
                        return deprecated1 ? 1 : -1;
                    }
                    return comp.compare(o1, o2);
                }
            }
        };
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void switchViewMode(Optional<ViewMode> viewMode) {
        E sel = viewModeComponent.getComponentForCurrentViewMode().getSelectedObject();
        viewModeComponent.setViewMode(viewMode);
        if(sel != null) {
            setSelectedEntity(sel);
        }
        updateViewActions();
        updateView();
    }

    private void updateViewActions() {
        for(ViewAction viewAction : getView().getViewActions()) {
            if(viewAction instanceof HasUpdateState) {
                ((HasUpdateState) viewAction).updateState();
            }
        }
    }

    protected abstract void performExtraInitialisation() throws Exception;

    @Override
    protected abstract OWLHierarchyProvider<E> getHierarchyProvider();

    protected abstract Optional<OWLHierarchyProvider<E>> getInferredHierarchyProvider();

    /**
     * Override with the name of the entities to be used in the Edit | Delete menu - eg "classes"
     * @return String the name of the entities
     */
    @SuppressWarnings("WeakerAccess")
    protected String getCollectiveTypeName(){
        return "entities";
    }

    public void setSelectedEntity(E entity) {
        getTree().setSelectedObject(entity);
    }

    @SuppressWarnings("WeakerAccess")
    public ObjectTree<E> getAssertedTree() {
        return assertedTree;
    }

    public void setSelectedEntities(Set<E> entities) {
        getTree().setSelectedObjects(entities);
    }

    public E getSelectedEntity() {
        return getTree().getSelectedObject();
    }

    public Set<E> getSelectedEntities() {
        return new HashSet<>(getTree().getSelectedObjects());
    }

    private void ensureSelection() {
        SwingUtilities.invokeLater(() -> {
            final E entity = getSelectedEntity();
            if (entity != null) {
                E treeSel = getTree().getSelectedObject();
                if (treeSel == null || !treeSel.equals(entity)) {
                    getTree().setSelectedObject(entity);
                }
            }
        });
    }

    @Override
    public boolean requestFocusInWindow() {
        return getTree().requestFocusInWindow();
    }

    @Override
    protected ObjectTree<E> getTree() {
        Optional<ViewMode> viewMode= getView().getViewMode();
        return viewModeComponent.getComponentForViewMode(viewMode);
    }

    @Override
    protected void transmitSelection() {
        deletableChangeListenerMediator.fireStateChanged(this);

        E selEntity = getSelectedEntity();
        if (selEntity != null) {
            final View view = getView();
            if (view != null && !view.isPinned()){
                view.setPinned(true); // so that we don't follow the selection
                setGlobalSelection(selEntity);
                view.setPinned(false);
            } else{
                setGlobalSelection(selEntity);
            }
        } else {
            setGlobalSelection(null);
        }

        updateHeader(selEntity);
    }

    protected E updateView(E selEntity) {
        if (getTree().getSelectedObject() == null) {
            if (selEntity != null) {
                getTree().setSelectedObject(selEntity);
            }
        } else {
            if (!getTree().getSelectedObject().equals(selEntity)) {
                getTree().setSelectedObject(selEntity);
            }
        }
        return selEntity;
    }

    @Override
    public void disposeView() {
        // Dispose of the assertedTree selection listener
        if (assertedTree != null) {
            assertedTree.removeTreeSelectionListener(listener);
            assertedTree.dispose();
        }
        if (inferredTree.isPresent()) {
            inferredTree.get().removeTreeSelectionListener(listener);
            inferredTree.get().dispose();
        }
    }

    @Override
    protected OWLObject getObjectToCopy() {
        return getTree().getSelectedObject();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //
    // Implementation of Deleteable
    //
    /////////////////////////////////////////////////////////////////////////////////////

    private final ChangeListenerMediator deletableChangeListenerMediator = new ChangeListenerMediator();

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
        return !getTree().getSelectedObjects().isEmpty();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //
    // Implementation of Findable
    //
    /////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void show(E owlEntity) {
        getTree().setSelectedObject(owlEntity);
    }

    @Override
    public void setShowDeprecatedEntities(boolean showDeprecatedEntities) {
        if(showDeprecatedEntities) {
            getHierarchyProvider().setFilter(e -> true);
        }
        else {
            getHierarchyProvider().setFilter(this::isNotDeprecated);
        }
    }

    private boolean isNotDeprecated(E e) {
        return !OWLUtilities.isDeprecated(getOWLModelManager(), e);
    }
}
