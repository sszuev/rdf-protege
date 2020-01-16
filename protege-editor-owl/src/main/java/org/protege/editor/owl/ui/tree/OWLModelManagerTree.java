package org.protege.editor.owl.ui.tree;

import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Jun-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLModelManagerTree<N extends OWLObject> extends OWLObjectTree<N> implements RefreshableComponent {

    private OWLModelManagerListener listener;
    private OWLEntityRendererListener rendererListener;
    protected OWLModelManagerEntityRenderer currentRenderer = null; // only use to clean up old listeners

    public OWLModelManagerTree(OWLEditorKit owlEditorKit, OWLHierarchyProvider<N> provider) {
        super(owlEditorKit, provider);
        initialise(owlEditorKit);
    }

    private void initialise(OWLEditorKit owlEditorKit) {
        final OWLObjectTreeCellRenderer renderer = new OWLObjectTreeCellRenderer(owlEditorKit);
        renderer.setWrap(false);
        setCellRenderer(renderer);
        setHighlightKeywords(false);
        setupListener();
        installPopupMenu();
        setRowHeight(-1);
        autoExpandTree();
    }

    @Override
    public void reload() {
        super.reload();
        autoExpandTree();
    }

    protected void setHighlightKeywords(boolean b) {
        TreeCellRenderer ren = getCellRenderer();
        if (ren instanceof OWLObjectTreeCellRenderer) {
            ((OWLObjectTreeCellRenderer) ren).setHighlightKeywords(b);
        }
    }

    private void autoExpandTree() {
        OWLTreePreferences prefs = OWLTreePreferences.getInstance();
        if (!prefs.isAutoExpandEnabled()) {
            return;
        }
        getProvider().roots().forEach(x -> autoExpand(x, 0));
    }

    private void autoExpand(N node, int currentDepth) {
        OWLTreePreferences prefs = OWLTreePreferences.getInstance();
        int maxDepth = prefs.getAutoExpansionDepthLimit();
        if (currentDepth >= maxDepth) {
            return;
        }
        OWLHierarchyProvider<N> prov = getProvider();
        int childCountLimit = prefs.getAutoExpansionChildLimit();
        Set<N> children = prov.getChildren(node);
        if (children.size() > childCountLimit) {
            return;
        }
        for (OWLObjectTreeNode<N> treeNode : getNodes(node)) {
            TreePath path = new TreePath(treeNode.getPath());
            expandPath(path);
        }
        for (N child : children) {
            autoExpand(child, currentDepth + 1);
        }
    }

    private void setupListener() {
        listener = event -> {
            if (event.isType(EventType.ENTITY_RENDERER_CHANGED)) {
                refreshEntityRenderer();
            }
        };
        getOWLModelManager().addListener(listener);
        rendererListener = (entity, renderer) -> handleRenderingChanged(entity);
        refreshEntityRenderer();
    }

    @SuppressWarnings("unchecked")
    private void handleRenderingChanged(OWLEntity entity) {
        Set<OWLObjectTreeNode<N>> res;
        try {
            res = getNodes((N) entity);
        } catch (ClassCastException ignore) {
            return;
        }
        for (OWLObjectTreeNode<N> node : res) {
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.nodeStructureChanged(node);
        }
    }

    private void refreshEntityRenderer() {
        invalidate();
        if (currentRenderer != null) {
            currentRenderer.removeListener(rendererListener);
        }
        currentRenderer = getOWLModelManager().getOWLEntityRenderer();
        currentRenderer.addListener(rendererListener);
    }

    private void installPopupMenu() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
            }
        });
    }

    protected void handleMouseEvent(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        TreePath treePath = getPathForLocation(e.getX(), e.getY());
        if (treePath != null) {
            handlePopupMenuInvoked(treePath, e.getPoint());
        }
    }

    @SuppressWarnings("unused")
    protected void handlePopupMenuInvoked(TreePath path, Point pt) { // TODO: wtf ?
    }

    @Override
    public void dispose() {
        super.dispose();
        getOWLModelManager().removeListener(listener);
        getOWLModelManager().getOWLEntityRenderer().removeListener(rendererListener);
    }
}
