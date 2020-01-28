package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.jena.model.OntModel;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.core.ui.menu.PopupMenuId;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewMode;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.action.AbstractOWLTreeAction;
import org.protege.editor.owl.ui.action.ObjectHierarchyDeleter;
import org.protege.editor.owl.ui.renderer.AddChildIcon;
import org.protege.editor.owl.ui.renderer.AddEntityIcon;
import org.protege.editor.owl.ui.renderer.DeleteEntityIcon;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.tree.ObjectTree;
import org.protege.editor.owl.ui.tree.TreeDragAndDropHandler;
import org.protege.editor.owl.ui.view.*;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * TODO: under developing
 * Created by @ssz on 23.11.2019.
 *
 * @see RDFTripleTree
 */
public class TripleHierarchyViewComponent extends AbstractOWLSelectionViewComponent
        implements Findable<Triple>, SelectionDriver,
        CreateNewTarget, CreateNewChildTarget, Deleteable, HasDisplayDeprecatedEntities {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripleHierarchyViewComponent.class);

    private static final Color ICON_COLOR = new Color(0xB51415);
    private static final Icon TRIPLE_ICON = OWLIcons.getIcon("Top.gif");
    private static final Icon ADD_ROOT_ICON = new AddEntityIcon(TRIPLE_ICON, ICON_COLOR);
    private static final Icon ADD_CHILD_ICON = new AddChildIcon(TRIPLE_ICON, ICON_COLOR);
    private static final Icon DELETE_ICON = new DeleteEntityIcon(TRIPLE_ICON, ICON_COLOR);

    private static final String ADD_GROUP = "A";
    private static final String DELETE_GROUP = "B";
    private static final String FIRST_SLOT = "A";
    private static final String SECOND_SLOT = "B";

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
        ObjectTree<Triple> tree = getTree();
        TreeSelectionModel sm = tree.getSelectionModel();
        DisposableAction addRoot = new AbstractOWLTreeAction<Triple>("Add root triple", ADD_ROOT_ICON, sm) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewObject();
            }

            @Override
            protected boolean canPerform(Triple cls) {
                return canCreateNew();
            }
        };

        DisposableAction addChild = new AbstractOWLTreeAction<Triple>("Add triple", ADD_CHILD_ICON, sm) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewChild();
            }

            @Override
            protected boolean canPerform(Triple cls) {
                return canCreateNewChild();
            }
        };

        OntModel model = OWLAdapter.get().asBaseModel(getHierarchyProvider().getOntology()).getBase();
        ObjectHierarchyDeleter<Triple> deleter = createDeleter(model);
        Triple header = model.getID().getMainStatement().asTriple();
        OWLSelectionViewAction delete = new OWLSelectionViewAction("Delete triple", DELETE_ICON) {
            @Override
            public void updateState() {
                Triple t = getTree().getSelectedObject();
                setEnabled(t != null && !header.equals(t));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                deleter.performDeletion();
            }

            @Override
            public void dispose() {
                deleter.dispose();
            }
        };

        addAction(addRoot, ADD_GROUP, FIRST_SLOT);
        addAction(addChild, ADD_GROUP, SECOND_SLOT);
        addAction(delete, DELETE_GROUP, FIRST_SLOT);

        // TODO: right now D'N'D is not allowed
        tree.setDragAndDropHandler(new TreeDragAndDropHandler<Triple>() {
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
        tree.setPopupMenuId(new PopupMenuId("[TripleHierarchy]"));
    }

    protected RDFHierarchyProvider getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getRDFTripleHierarchyProvider();
    }

    @Override
    public boolean canCreateNew() {
        return true;
    }

    @Override
    public boolean canCreateNewChild() {
        Triple t = getTree().getSelectedObject();
        return t != null && (t instanceof RDFHierarchyProvider.RootTriple || t.getObject().isBlank());
    }

    @Override
    public void createNewObject() {
        OntModel ont = OWLAdapter.get().asBaseModel(getHierarchyProvider().getOntology()).getBase();
        AddTriplePanel panel = new AddTriplePanel(createAddTripleModel(ont));
        createTriple("Create Root Triple", panel, ont.getGraph());
    }

    @Override
    public void createNewChild() {
        Triple parent = getTree().getSelectedObject();
        if (parent == null)
            return;
        OntModel ont = OWLAdapter.get().asBaseModel(getHierarchyProvider().getOntology()).getBase();
        AddTripleModel model = createAddTripleModel(ont);
        Node subject;
        if (parent instanceof RDFHierarchyProvider.RootTriple) {
            subject = parent.getSubject();
        } else if (parent.getObject().isBlank()) {
            subject = parent.getObject();
        } else {
            return;
        }
        AddTriplePanel panel = createPanelForSubject(subject, model);
        createTriple("Create Triple", panel, ont.getGraph());
    }

    protected AddTriplePanel createPanelForSubject(Node subject, AddTripleModel model) {
        return new AddTriplePanel(model) {
            @Override
            protected void initSubjectConfiguration() {
                String txt;
                if (subject.isBlank()) {
                    txt = getOWLModelManager().getBlankNodeMapper().apply(subject.getBlankNodeId());
                } else {
                    txt = subject.getURI();
                }
                subjectField.setText(txt);
            }

            @Override
            public Node getSubjectNode() {
                return subject;
            }

            @Override
            protected void addSubjectInputRow(JPanel res) {
                // nothing
            }
        };
    }

    protected void createTriple(String title, AddTriplePanel panel, Graph g) {
        Triple res = addTriple(title, panel, g);
        if (res != null) {
            getTree().setSelectedObject(res);
        }
    }

    protected Triple addTriple(String title, AddTriplePanel panel, Graph g) {
        int res = new UIHelper(getOWLEditorKit()).showValidatingDialog(title, panel, null);
        if (res != JOptionPane.OK_OPTION) {
            return null;
        }
        Triple t = panel.getTriple();
        g.add(t);
        onChange();
        return t;
    }

    protected void onChange() {
        // todo: no need to reload this component
        getOWLModelManager().fireEvent(EventType.ONTOLOGY_RELOADED);
    }

    protected AddTripleModel createAddTripleModel(OntModel ont) {
        return new AddTripleModel() {
            private final PrefixMapping pm = PrefixMapping.Factory.create()
                    .setNsPrefixes(RDFHierarchyProvider.STANDARD_PREFIXES)
                    .setNsPrefixes(ont)
                    .lock();

            @Override
            public String getBaseURI() {
                return ont.getID().getURI();
            }

            @Override
            public PrefixMapping getPrefixMapping() {
                return pm;
            }

            @Override
            public Set<Property> getProperties() {
                return RDFHierarchyProvider.STANDARD_PROPERTIES;
            }

            @Override
            public Set<Resource> getDatatypes() {
                return RDFHierarchyProvider.STANDARD_DATATYPES;
            }
        };
    }

    protected ObjectHierarchyDeleter<Triple> createDeleter(OntModel model) {
        RDFHierarchyProvider provider = getHierarchyProvider();
        PrefixMapping pm = PrefixMapping.Factory.create()
                .setNsPrefixes(RDFHierarchyProvider.STANDARD_PREFIXES)
                .setNsPrefixes(model);
        Function<Object, String> map = getOWLModelManager().getBlankNodeMapper();
        return new ObjectHierarchyDeleter<Triple>(getOWLEditorKit(),
                provider, () -> tree.getSelectedObjects(), "Triples") {
            @Override
            protected String getRendering(Triple st) {
                String res = String.format("[%s, %s, %s]",
                        toString(st.getSubject(), false),
                        st.getPredicate().toString(pm, false),
                        toString(st.getObject(), true));
                return res.length() > 100 ? "this triple" : res;
            }

            private String toString(Node n, boolean quoting) {
                return n.isBlank() ? map.apply(n.getBlankNodeId()) : n.toString(pm, quoting);
            }

            @Override
            protected void delete(Collection<Triple> nodes) {
                Graph g = model.getGraph();
                nodes.forEach(x -> {
                    LOGGER.debug("Delete triple '{}'", x);
                    g.delete(x);
                });
                onChange();
            }
        };
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
        getTree().setSelectedObject(entity);
    }

    public Triple getSelectedNode() {
        return getTree().getSelectedObject();
    }

    public Set<Triple> getSelectedEntities() {
        return new HashSet<>(getTree().getSelectedObjects());
    }

    private void ensureSelection() {
        SwingUtilities.invokeLater(() -> {
            Triple entity = getSelectedNode();
            if (entity == null) {
                return;
            }
            Triple t = getTree().getSelectedObject();
            if (Objects.equals(t, entity)) {
                return;
            }
            getTree().setSelectedObject(entity);
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
        updateHeader(triple);
    }

    protected Triple updateView(Triple selEntity) {
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
        return !getTree().getSelectedObjects().isEmpty();
    }

    @Override
    public void show(Triple owlEntity) {
        getTree().setSelectedObject(owlEntity);
    }

    @Override
    public void setShowDeprecatedEntities(boolean showDeprecatedEntities) {
        Predicate<Triple> filter;
        if (showDeprecatedEntities) {
            filter = e -> true;
        } else {
            filter = this::isNotDeprecated;
        }
        getHierarchyProvider().setFilter(filter);
    }

    private boolean isNotDeprecated(Triple e) {
        //return !OWLUtilities.isDeprecated(getOWLModelManager(), e);
        return false;
    }
}

