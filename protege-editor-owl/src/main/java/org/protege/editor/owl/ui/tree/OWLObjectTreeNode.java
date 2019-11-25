package org.protege.editor.owl.ui.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;


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

    private ObjectTree tree;

    private boolean isLoaded;

    private Set<N> equivalentObjects;

    public OWLObjectTreeNode(Object userObject, ObjectTree tree) {
        super(userObject);
        this.tree = tree;
        isLoaded = false;
        equivalentObjects = new HashSet<>();
    }

    public void addEquivalentObject(N object) {
        equivalentObjects.add(object);
    }

    public Set<N> getEquivalentObjects() {
        if (getUserObject() == null) {
            return Collections.emptySet();
        }
        Set<N> equivalents = tree.getProvider().getEquivalents(getUserObject());
        equivalents.remove(getUserObject());
        return equivalents;
    }

    public OWLObjectTreeNode(ObjectTree tree) {
        this.tree = tree;
        this.equivalentObjects = new HashSet<>();
    }

    @Override
    public boolean isRoot() {
        return getUserObject() == null;
    }

    public N getOWLObject() {
        return (N) getUserObject();
    }

    protected boolean isLoaded() {
        return isLoaded;
    }

    protected void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    protected synchronized void loadChildrenIfNecessary() {
        if (isLoaded) {
            return;
        }
        isLoaded = true;
        // TODO: wtf?
        Object parentObject = null;
        OWLObjectTreeNode<N> parentNode = (OWLObjectTreeNode) getParent();
        if (getParent() != null) {
            parentObject = parentNode.getOWLObject();
        }
        List<OWLObjectTreeNode<N>> nodes = tree.getChildNodes(this);
        for (OWLObjectTreeNode<N> child : nodes) {
//                if (parentObject != null && parentObject.equals(child.getOWLObject())) {
//                    // Cycle!!
//                } else {
            add(child);
//                }
        }
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

    @Override
    public Enumeration children() {
        loadChildrenIfNecessary();
        return super.children();
    }
}
