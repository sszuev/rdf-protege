package org.protege.editor.owl.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;


/**
 * TODO: rename
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Jun-2006<br><br>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLObjectTreeNode<N> extends DefaultMutableTreeNode {

    private final ObjectTree<N> tree;
    private volatile boolean isLoaded;

    public OWLObjectTreeNode(Object userObject, ObjectTree<N> tree) {
        super(userObject);
        this.tree = tree;
    }

    public Set<N> getEquivalentObjects() {
        N o = getOWLObject();
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
    public N getOWLObject() {
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
    public OWLObjectTreeNode<N> getChildAt(int childIndex) {
        loadChildrenIfNecessary();
        return (OWLObjectTreeNode<N>) super.getChildAt(childIndex);
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
