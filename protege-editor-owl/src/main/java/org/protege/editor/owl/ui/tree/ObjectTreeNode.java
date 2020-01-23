package org.protege.editor.owl.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;


/**
 * Describes an element of {@link ObjectTree tree}.
 *
 * @param <N> - anything
 */
public class ObjectTreeNode<N> extends DefaultMutableTreeNode {

    private final ObjectTree<N> tree;
    private volatile boolean isLoaded;

    public ObjectTreeNode(Object userObject, ObjectTree<N> tree) {
        super(userObject);
        this.tree = tree;
    }

    public Set<N> getEquivalentObjects() {
        N o = getObjectNode();
        if (o == null) {
            return Collections.emptySet();
        }
        Set<N> res = tree.getProvider().getEquivalents(o);
        res.remove(o);
        return res;
    }

    @Override
    public boolean isRoot() {
        return getUserObject() == null;
    }

    @SuppressWarnings("unchecked")
    public N getObjectNode() {
        return (N) getUserObject();
    }

    protected boolean isLoaded() {
        return isLoaded;
    }

    protected void setLoaded() {
        isLoaded = true;
    }

    protected synchronized void loadChildrenIfNecessary() {
        // todo: not sure why it is synchronized -- need investigate and add documentation
        if (isLoaded) {
            return;
        }
        isLoaded = true;
        tree.getChildNodes(this).forEach(this::add);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ObjectTreeNode<N> getChildAt(int childIndex) {
        loadChildrenIfNecessary();
        return (ObjectTreeNode<N>) super.getChildAt(childIndex);
    }

    @Override
    public int getChildCount() {
        loadChildrenIfNecessary();
        return super.getChildCount();
    }

    @Override
    public TreeNode getParent() {
        return super.getParent();
    }

    @Override
    public int getIndex(TreeNode node) {
        loadChildrenIfNecessary();
        return super.getIndex(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<N> children() {
        loadChildrenIfNecessary();
        return super.children();
    }
}
