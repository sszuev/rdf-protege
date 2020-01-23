package org.protege.editor.owl.ui.tree;

import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.core.ui.menu.MenuBuilder;
import org.protege.editor.core.ui.menu.PopupMenuId;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.HierarchyProviderListener;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.transfer.OWLObjectDragSource;
import org.protege.editor.owl.ui.transfer.ObjectDropTarget;
import org.protege.editor.owl.ui.transfer.ObjectTreeDragGestureListener;
import org.protege.editor.owl.ui.view.Copyable;
import org.protege.editor.owl.ui.view.HasCopySubHierarchyToClipboard;
import org.protege.editor.owl.ui.view.HasExpandAll;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.*;

/**
 * Created by @ssz on 23.11.2019.
 *
 * @param <N> - anything
 */
@SuppressWarnings({"WeakerAccess"})
public abstract class ObjectTree<N> extends JTree
        implements ObjectDropTarget<N>, Copyable<N>,
        OWLObjectDragSource, HasExpandAll, HasCopySubHierarchyToClipboard, RefreshableComponent {

    protected final OWLEditorKit eKit;
    protected final OWLHierarchyProvider<N> provider;
    private final Map<N, Set<ObjectTreeNode<N>>> nodeMap;
    private final HierarchyProviderListener<N> listener;
    protected Comparator<? super N> comparator;
    private OWLTreeDragAndDropHandler<N> dragAndDropHandler;

    // todo: unused fields:
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean dragOriginator;
    @SuppressWarnings("unused")
    private Point mouseDownPos;

    private PopupMenuId popupMenuId;
    private int dropRow = -1;
    /**
     * A timer that is used to automatically expand nodes if the
     * mouse hovers over a node during a drag and drop operation.
     */
    private Timer expandNodeTimer = new Timer(800, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (dropRow != -1) {
                TreePath path = getPathForRow(dropRow);
                expandPath(path);
                expandNodeTimer.stop();
            }
        }
    });
    private Stroke s = new BasicStroke(2.0f);

    public ObjectTree(OWLEditorKit eKit, OWLHierarchyProvider<N> provider) {
        this(eKit, provider, provider.getRoots(), null);
    }

    public ObjectTree(OWLEditorKit eKit,
                      OWLHierarchyProvider<N> provider,
                      Collection<N> roots,
                      Comparator<? super N> owlObjectComparator) {
        this.eKit = eKit;
        setupLineStyle();
        ToolTipManager.sharedInstance().registerComponent(this);
        this.comparator = owlObjectComparator;
        this.provider = provider;
        this.nodeMap = new HashMap<>();
        listener = new HierarchyProviderListener<N>() {
            @Override
            public void hierarchyChanged() {
                reload();
            }

            @Override
            public void nodeChanged(N node) {
                updateNode(node);
            }
        };
        provider.addListener(listener);
        setModel(new DefaultTreeModel(new RootNode(roots)));
        setShowsRootHandles(true);
        setRootVisible(false);
        setRowHeight(18);
        setScrollsOnExpand(true);
        setAutoscrolls(true);
        setExpandsSelectedPaths(true);
        //DropTarget dt = new DropTarget(this, new OWLObjectTreeDropTargetListener(this, getTreePreferences()));
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY_OR_MOVE,
                new ObjectTreeDragGestureListener<>(eKit, this));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 3 && e.isControlDown() && e.isShiftDown()) {
                    reload();
                }
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
                if (e.isAltDown()) {
                    expandDescendantsOfRowAt(e.getX(), e.getY());
                }
            }
        });

        getSelectionModel().addTreeSelectionListener(event -> scrollPathToVisible(event.getNewLeadSelectionPath()));
    }

    protected OWLTreePreferences getTreePreferences() {
        return OWLTreePreferences.getInstance();
    }

    private void setupLineStyle() {
        putClientProperty("JTree.lineStyle", getTreePreferences().isPaintLines() ? "Angled" : "None");
    }

    private void expandDescendantsOfRowAt(final int x, int y) {
        // It's necessary to traverse all rows to find the path where the user clicked.  This is because
        // the getRowAt(X,Y) call only returns a row index if the actual node rendering is clicked.  We
        // Want to detect if the node handle is clicked (or anywhere in the white space of a row).
        for (int i = 0; i < getRowCount(); i++) {
            Rectangle rowBounds = getRowBounds(i);
            if (rowBounds == null || rowBounds.y > y || y > rowBounds.y + rowBounds.height) {
                continue;
            }
            expandDescendantsOfRow(i);
            break;
        }
    }

    /**
     * Clears the data displayed by the component and
     * reloads data.
     */
    @Override
    public final void refreshComponent() {
        setupLineStyle();
        reload();
    }

    /**
     * Clears the popupMenuId for this tree.
     * todo: unused method
     */
    public void clearPopupMenuId() {
        this.popupMenuId = null;
    }

    private Optional<PopupMenuId> getPopupMenuId() {
        return Optional.ofNullable(popupMenuId);
    }

    /**
     * Sets the popupMenuId for this tree.
     *
     * @param popupMenuId The id.  Not {@code null}.
     */
    public void setPopupMenuId(PopupMenuId popupMenuId) {
        this.popupMenuId = popupMenuId;
    }

    private void showPopupMenu(MouseEvent e) {
        if (!getPopupMenuId().isPresent()) {
            return;
        }
        MenuBuilder menuBuilder = new MenuBuilder(eKit);
        PopupMenuId popupMenuId = getPopupMenuId().get();
        JPopupMenu popupMenu = menuBuilder.buildPopupMenu(popupMenuId);
        popupMenu.show(this, e.getX(), e.getY());
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        N obj = getOWLObjectAtMousePosition(event);
        if (obj instanceof OWLEntity) {
            return ((OWLEntity) obj).getIRI().toString();
        }
        return null;
    }

    private void updateNode(N node) {
        // This method is called when the parents or children of
        // a node might have changed.  We handle the following possibilities
        // 1) The node had a child added
        // 2) The node had a child removed
        // If we are displaying the node which had the child added or removed
        // then we update the node

        Set<ObjectTreeNode<N>> treeNodes = nodeMap.get(node);

        // The parents/children might have changed
        if (treeNodes == null || treeNodes.isEmpty()) {
            // Might be a new root!
            if (provider.hasRoot(node)) {
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getModel().getRoot();
                DefaultMutableTreeNode nn = createTreeNode(node);
                ((DefaultTreeModel) getModel()).insertNodeInto(nn, rootNode, 0);
                expandPath(new TreePath(rootNode.getPath()));
            }
            return;
        }
        // Remove children that aren't there any more
        Collection<N> children = provider.getChildren(node);

        Set<ObjectTreeNode<N>> nodesToRemove = new HashSet<>();
        for (ObjectTreeNode<N> treeNode : treeNodes) {
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                ObjectTreeNode<N> childTreeNode = treeNode.getChildAt(i);
                if (children.contains(childTreeNode.getObjectNode())) {
                    continue;
                }
                nodesToRemove.add(childTreeNode);
            }
        }

        for (ObjectTreeNode<N> nodeToRemove : nodesToRemove) {
            // update the nodeMap to remove this parent from the child
            final Set<ObjectTreeNode<N>> childNodes = getNodes(nodeToRemove.getObjectNode());
            final Set<ObjectTreeNode<N>> updatedChildNodes = new HashSet<>();
            for (ObjectTreeNode<N> childNode : childNodes) {
                @SuppressWarnings("unchecked") ObjectTreeNode<N> p = (ObjectTreeNode<N>) childNode.getParent();
                if (!treeNodes.contains(p)) {
                    updatedChildNodes.add(childNode);
                }
            }
            nodeMap.put(nodeToRemove.getObjectNode(), updatedChildNodes);
            ((DefaultTreeModel) getModel()).removeNodeFromParent(nodeToRemove);
        }

        // Add new children
        Set<N> existingChildren = new HashSet<>();
        for (ObjectTreeNode<N> treeNode : treeNodes) {
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                existingChildren.add(treeNode.getChildAt(i).getObjectNode());
            }
        }

        for (ObjectTreeNode<N> treeNode : treeNodes) {
            for (N child : children) {
                if (existingChildren.contains(child)) {
                    continue;
                }
                ObjectTreeNode<N> childTreeNode = createTreeNode(child);
                ((DefaultTreeModel) getModel()).insertNodeInto(childTreeNode, treeNode, 0);
            }
        }

        if (provider.hasRoot(node)) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) getModel().getRoot();
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                @SuppressWarnings("unchecked")
                ObjectTreeNode<N> n = (ObjectTreeNode<N>) treeNode.getChildAt(i);
                if (n.getObjectNode().equals(node)) {
                    return;
                }
            }
            ((DefaultTreeModel) getModel()).insertNodeInto(createTreeNode(node), treeNode, 0);
            return;
        }
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getModel().getRoot();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            @SuppressWarnings("unchecked")
            ObjectTreeNode<N> n = (ObjectTreeNode<N>) rootNode.getChildAt(i);
            if (n.getObjectNode().equals(node)) {
                ((DefaultTreeModel) getModel()).removeNodeFromParent(n);
                return;
            }
        }
    }

    public void dispose() {
        provider.removeListener(listener);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setRowHeight(getFontMetrics(getFont()).getHeight() + 4);
    }

    /**
     * Causes the tree to be reloaded.
     * Note that this will collapse all expanded paths except for the current selection.
     */
    public void reload() {
        N current = getSelectedOWLObject();
        // Reload the tree
        nodeMap.clear();
        // TODO: getRoots needs to be changed - the user might have specified specific roots
        ((DefaultTreeModel) getModel()).setRoot(new RootNode(provider.getRoots()));
        setSelectedOWLObject(current);
    }

    private void expandDescendantsOfRow(int row) {
        if (row == -1) {
            return;
        }
        TreePath pathToExpand = getPathForRow(row);
        if (pathToExpand == null) {
            return;
        }
        Stack<TreePath> stack = new Stack<>();
        stack.push(pathToExpand);

        while (!stack.isEmpty()) {
            TreePath path = stack.pop();
            for (int i = 0; i < getModel().getChildCount(path.getLastPathComponent()); i++) {
                Object curChild = getModel().getChild(path.getLastPathComponent(), i);
                TreePath childPath = path.pathByAddingChild(curChild);
                expandPath(childPath);
                stack.push(childPath);
            }
        }
    }

    public void setDragAndDropHandler(OWLTreeDragAndDropHandler<N> dragAndDropHandler) {
        this.dragAndDropHandler = dragAndDropHandler;
    }

    /**
     * @return the hierarchy provider that this tree uses to generate its branches
     */
    public OWLHierarchyProvider<N> getProvider() {
        return provider;
    }

    public abstract Comparator<? super N> getCopyComparator();

//    private void removeDescendantNodesFromMap(OWLObjectTreeNode<N> parentNode) {
//        Enumeration e = parentNode.depthFirstEnumeration();
//        while (e.hasMoreElements()) {
//            OWLObjectTreeNode<N> curNode = (OWLObjectTreeNode<N>) e.nextElement();
//            getNodes(curNode.getOWLObject()).remove(curNode);
//        }
//    }

    protected Comparator<? super N> getRootNodeComparator() {
        return comparator;
    }

    protected Comparator<? super N> getChildNodeComparator() {
        return comparator;
    }

    /**
     * Sets the tree ordering and reloads the tree contents.
     *
     * @param owlObjectComparator the comparator that is used to order sibling tree nodes
     */
    public void setOWLObjectComparator(Comparator<? super N> owlObjectComparator) {
        this.comparator = owlObjectComparator;
        reload();
    }

    protected List<ObjectTreeNode<N>> getChildNodes(ObjectTreeNode<N> parent) {
        List<ObjectTreeNode<N>> result = new ArrayList<>();
        Set<N> parents = getParentObjectsForNode(parent);
        Collection<N> children = provider.getChildren(parent.getObjectNode());
        Comparator<? super N> comp = getChildNodeComparator();
        if (comp != null) {
            List<N> sorted = new ArrayList<>(children);
            sorted.sort(comp);
            children = sorted;
        }
        for (N child : children) {
            if (!parents.contains(child)) {
                result.add(createTreeNode(child));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<N> getParentObjectsForNode(ObjectTreeNode<N> node) {
        Set<N> parents = new HashSet<>();
        ObjectTreeNode<N> parentNode = node;
        while ((parentNode = (ObjectTreeNode<N>) parentNode.getParent()) != null) {
            if (parentNode.getObjectNode() != null) {
                parents.add(parentNode.getObjectNode());
            }
        }
        return parents;
    }

    /**
     * Gets the set of nodes that represent the specified object.
     *
     * @param n The object whose nodes are to be retrieved.
     * @return The nodes that represent the specified object.
     */
    protected Set<ObjectTreeNode<N>> getNodes(N n) {
        return nodeMap.computeIfAbsent(n, k -> new HashSet<>());
    }

    protected ObjectTreeNode<N> createTreeNode(N x) {
        ObjectTreeNode<N> res = new ObjectTreeNode<>(x, this);
        getNodes(x).add(res);
        return res;
    }

    public void setSelectedOWLObject(N selObject, boolean selectAll) {
        if (selObject == null) {
            return;
        }
        setSelectedOWLObjects(Collections.singleton(selObject), selectAll);
    }

    public void setSelectedOWLObjects(Set<N> owlObjects, boolean selectAll) {
        List<N> currentSelection = getSelectedOWLObjects();
        if (currentSelection.containsAll(owlObjects) && owlObjects.containsAll(currentSelection)) {
            return;
        }
        clearSelection();
        if (!owlObjects.isEmpty()) {
            final List<TreePath> paths = new ArrayList<>();
            for (N obj : owlObjects) {
                Set<ObjectTreeNode<N>> nodes = getNodes(obj);
                if (nodes.isEmpty()) {
                    expandAndSelectPaths(obj, selectAll);
                }
                paths.addAll(getPaths(obj, selectAll));
            }
            if (!paths.isEmpty()) {
                setSelectionPaths(paths.toArray(new TreePath[0]));
                // without this the selection never quite makes it onto the screen
                // probably because the component has not been sized yet
                SwingUtilities.invokeLater(() -> scrollPathToVisible(paths.get(0)));
            }
        }
    }

    private List<TreePath> getPaths(N selObject, boolean selectAll) {
        List<TreePath> paths = new ArrayList<>();
        Set<ObjectTreeNode<N>> nodes = getNodes(selObject);
        for (ObjectTreeNode<N> node : nodes) {
            paths.add(new TreePath(node.getPath()));
            if (!selectAll) {
                break;
            }
        }
        return paths;
    }

    private void expandAndSelectPaths(N obj, boolean selectAll) {
        for (List<N> objPath : provider.getPathsToRoot(obj)) {
            expandAndSelectPath(objPath);
            if (!selectAll) {
                break;
            }
        }
    }

    private void expandAndSelectPath(java.util.List<N> objectPath) {
        // Start from the end of the path and search back
        // through the path until we find a node in the tree
        // that represents the object.  If we find a node, then
        // we need to expand the child nodes.
        int index = 0;
        for (int i = objectPath.size() - 1; i > -1; i--) {
            index = i;
            N curObj = objectPath.get(i);
            if (!getNodes(curObj).isEmpty()) {
                break;
            }
        }
        Set<ObjectTreeNode<N>> nodes = getNodes(objectPath.get(index));
        if (nodes.isEmpty()) {
            return;
        }
        ObjectTreeNode<N> curParNode = nodes.iterator().next();
        for (int i = index + 1; i < objectPath.size(); i++) {
            expandPath(new TreePath(curParNode.getPath()));
            for (int j = 0; j < curParNode.getChildCount(); j++) {
                ObjectTreeNode<N> curChild = curParNode.getChildAt(j);
                if (curChild.getObjectNode().equals(objectPath.get(i))) {
                    curParNode = curChild;
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public N getSelectedOWLObject() {
        TreePath path = getSelectionPath();
        if (path == null) {
            return null;
        }
        return ((ObjectTreeNode<N>) path.getLastPathComponent()).getObjectNode();
    }

    /**
     * If the object is contained in a collapsed branch then the branch is expanded.
     *
     * @param selObject the object to select if it exists in the tree
     */
    public void setSelectedOWLObject(N selObject) {
        setSelectedOWLObject(selObject, false);
    }

    @SuppressWarnings("unchecked")
    public List<N> getSelectedOWLObjects() {
        List<N> res = new ArrayList<>();
        TreePath[] selPaths = getSelectionPaths();
        if (selPaths == null) {
            return res;
        }
        for (TreePath path : selPaths) {
            res.add((((ObjectTreeNode<N>) path.getLastPathComponent()).getObjectNode()));
        }
        return res;
    }

    public void setSelectedOWLObjects(Set<N> owlObjects) {
        setSelectedOWLObjects(owlObjects, false);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setDragOriginater(boolean b) {
        dragOriginator = b;
    }

    @Override
    public boolean dropOWLObjects(final java.util.List<N> owlObjects, Point pt, int type) {
        if (dragAndDropHandler == null) {
            return false;
        }

        if (!getTreePreferences().isTreeDragAndDropEnabled()) {
            return false;
        }

        TreePath dropPath = getPathForLocation(pt.x, pt.y);
        if (dropPath == null) {
            // If the object hasn't been dropped on a node, then don't accept drop
            return false;
        }

        @SuppressWarnings("unchecked")
        N dropTargetObj = ((ObjectTreeNode<N>) dropPath.getLastPathComponent()).getObjectNode();

        final Set<N> droppedObjects = new HashSet<>();

        for (N owlObject : owlObjects) {
            if (dropTargetObj.equals(owlObject) || // don't drop on self
                    !dragAndDropHandler.canDrop(owlObject, dropTargetObj)) {
                continue;
            }

            // the object must be in the acceptable bounds for the handler by now
            droppedObjects.add(owlObject);

            TreePath selPath = getSelectionPath();
            N selObj = null;
            N selObjParent = null;
            if (selPath != null) {
                @SuppressWarnings("unchecked")
                ObjectTreeNode<N> selNode = ((ObjectTreeNode<N>) selPath.getLastPathComponent());
                selObj = selNode.getObjectNode();
                @SuppressWarnings("unchecked")
                ObjectTreeNode<N> parentNode = (ObjectTreeNode<N>) selNode.getParent();
                if (parentNode != null) {
                    selObjParent = parentNode.getObjectNode();
                }
            }

            if (selObj == null) {
                // In ADD operation (We can only add here)
                dragAndDropHandler.add(owlObject, dropTargetObj);
            } else {
                if (selObj.equals(owlObject)) {
                    if (selObjParent != null) {
                        dragAndDropHandler.move(owlObject, selObjParent, dropTargetObj);
                    } else {
                        dragAndDropHandler.add(owlObject, dropTargetObj);
                    }
                } else {
                    // ADD op
                    dragAndDropHandler.add(owlObject, dropTargetObj);
                }
            }
        }

        if (droppedObjects.isEmpty()) {
            return false;
        }
        SwingUtilities.invokeLater(() -> {
            Set<N> nodes = new HashSet<>();
            for (N droppedObject : droppedObjects) {
                if (getNodes(droppedObject) != null) { // if this node exists in the tree
                    nodes.add(droppedObject);
                }
            }
            setSelectedOWLObjects(nodes);
        });
        return true;
    }

    @Override
    public OWLModelManager getOWLModelManager() {
        return eKit.getModelManager();
    }

    /**
     * todo: unused method
     *
     * @return int
     */
    public int getDropRow() {
        return dropRow;
    }

    public void setDropRow(int dropRow) {
        expandNodeTimer.restart();
        if (this.dropRow != -1) {
            Rectangle r = getDropRowBounds();
            if (r != null) {
                repaint(r);
            }
            expandNodeTimer.stop();
        }
        this.dropRow = dropRow;
        if (this.dropRow != -1) {
            Rectangle r = getDropRowBounds();
            if (r != null) {
                repaint(r);
            }
            expandNodeTimer.start();
            scrollRowToVisible(dropRow);
        }
    }

    public Rectangle getDropRowBounds() {
        Rectangle r = getRowBounds(dropRow);
        if (r == null) {
            return null;
        }
        r.x = r.x - 2;
        r.y = r.y - 2;
        r.width += 4;
        r.height += 4;
        return r;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Paint drop node
        if (!getTreePreferences().isTreeDragAndDropEnabled() || dropRow == -1) {
            return;
        }
        Rectangle r = getRowBounds(dropRow);
        if (r == null) {
            return;
        }

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ((Graphics2D) g).setStroke(s);
        Color color = UIManager.getDefaults().getColor("Tree.selectionBorderColor");
        g.setColor(color);
        g.drawRoundRect(r.x, r.y, r.width, r.height, 7, 7);
    }

    @Override
    public void expandAll() {
        for (int i = 0; i < getRowCount(); i++) {
            expandPath(getPathForRow(i));
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    protected N getOWLObjectAtMousePosition(MouseEvent event) {
        Point pt = event.getPoint();
        TreePath path = getPathForLocation(pt.x, pt.y);
        if (path == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        ObjectTreeNode<N> node = (ObjectTreeNode<N>) path.getLastPathComponent();
        return node.getObjectNode();
    }

    @Override
    public void copySubHierarchyToClipboard() {
        N selObject = getSelectedOWLObject();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        copySubHierarchyToClipboard(selObject, pw, 0);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sw.toString()), null);
    }

    private void copySubHierarchyToClipboard(N object, PrintWriter printWriter, int depth) {
        for (int i = 0; i < depth; i++) {
            printWriter.print("\t");
        }
        String rendering = getRendering(object);
        printWriter.println(rendering);
        Collection<N> children = provider.getChildren(object);
        Comparator<? super N> comp = getCopyComparator();
        if (comp != null) {
            List<N> sorted = new ArrayList<>(children);
            sorted.sort(comp);
            children = sorted;
        }
        for (N child : children) {
            copySubHierarchyToClipboard(child, printWriter, depth + 1);
        }
    }

    protected abstract String getRendering(N obj);

    @Override
    public boolean canPerformCopySubHierarchyToClipboard() {
        return getSelectedOWLObject() != null;
    }

    @Override
    public boolean canCopy() {
        return !getSelectedOWLObjects().isEmpty();
    }

    @Override
    public List<N> getObjectsToCopy() {
        return new ArrayList<>(getSelectedOWLObjects());
    }

    /**
     * The root {@link ObjectTreeNode}.
     */
    public class RootNode extends ObjectTreeNode<N> {
        private Collection<N> roots;

        RootNode(Collection<N> roots) {
            super(null, ObjectTree.this);
            this.roots = roots;
        }

        @Override
        protected void loadChildrenIfNecessary() {
            if (isLoaded()) {
                return;
            }
            setLoaded();
            Collection<N> res = this.roots;
            Comparator<? super N> comp = getRootNodeComparator();
            if (comp != null) {
                List<N> sorted = new ArrayList<>(res);
                sorted.sort(comp);
                res = sorted;
            }
            for (N root : res) {
                add(createTreeNode(root));
            }
        }
    }
}

