package org.protege.editor.owl.ui.view.rdf;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.tree.OWLTreePreferences;
import org.protege.editor.owl.ui.tree.ObjectTree;
import org.protege.editor.owl.ui.tree.ObjectTreeNode;

import javax.swing.*;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a {@link javax.swing.JTree}-component for rendering {@link Triple}s-tree.
 * TODO: currently it is not fu`lly ready: it is read-only and ugly.
 * <p>
 * Created by @ssz on 23.11.2019.
 *
 * @see RDFHierarchyProvider
 */
@SuppressWarnings("WeakerAccess")
public class RDFTripleTree extends ObjectTree<Triple> {

    public RDFTripleTree(OWLEditorKit kit, RDFHierarchyProvider provider) {
        super(kit, provider);
        initialise(kit);
    }

    @Override
    public Comparator<? super Triple> getCopyComparator() {
        return null;
    }

    @Override
    protected String getRendering(Triple obj) {
        return ((CellRenderer) getCellRenderer()).getRendering(obj);
    }

    private void initialise(OWLEditorKit kit) {
        OWLObjectTreeCellRenderer renderer = createCellRenderer(kit);
        renderer.setWrap(false);
        setCellRenderer(renderer);
        setHighlightKeywords(false);
        setupListener();
        installPopupMenu();
        setRowHeight(-1);
        autoExpandTree();
    }

    protected OWLObjectTreeCellRenderer createCellRenderer(OWLEditorKit kit) {
        return new CellRenderer(kit);
    }

    @Override
    public RDFHierarchyProvider getProvider() {
        return (RDFHierarchyProvider) provider;
    }

    @Override
    public void reload() {
        super.reload();
        autoExpandTree();
    }

    public void setHighlightKeywords(boolean b) {
        TreeCellRenderer ren = getCellRenderer();
        if (ren instanceof OWLObjectTreeCellRenderer) {
            ((OWLObjectTreeCellRenderer) ren).setHighlightKeywords(b);
        }
    }

    private void autoExpandTree() {
        OWLTreePreferences prefs = getTreePreferences();
        if (!prefs.isAutoExpandEnabled()) {
            return;
        }
        getProvider().roots().forEach(x -> autoExpand(x, 0));
    }

    private void autoExpand(Triple node, int currentDepth) {
        OWLTreePreferences prefs = getTreePreferences();
        int maxDepth = prefs.getAutoExpansionDepthLimit();
        if (currentDepth >= maxDepth) {
            return;
        }
        OWLHierarchyProvider<Triple> prov = getProvider();
        int childCountLimit = prefs.getAutoExpansionChildLimit();
        Collection<Triple> children = prov.getChildren(node);
        if (children.size() <= childCountLimit) {
            for (ObjectTreeNode<Triple> treeNode : getNodes(node)) {
                TreePath path = new TreePath(treeNode.getPath());
                expandPath(path);
            }
            for (Triple child : children) {
                autoExpand(child, currentDepth + 1);
            }
        }
    }

    private void setupListener() {
        refreshEntityRenderer();
    }

    private void refreshEntityRenderer() {
        invalidate();
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

    @SuppressWarnings("unused")
    protected void handleMouseEvent(MouseEvent e) {
        /*if (!e.isPopupTrigger()) {
            return;
        }
        TreePath treePath = getPathForLocation(e.getX(), e.getY());
        if (treePath != null) {
            handlePopupMenuInvoked(treePath, e.getPoint());
        }*/
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    protected class CellRenderer extends OWLObjectTreeCellRenderer {

        protected CellRenderer(OWLEditorKit kit) {
            super(kit);
        }

        @Override
        protected void setToolTipText(JTree tree, Object value) {
            String txt;
            if (value instanceof Triple) {
                Triple t = (Triple) value;
                String s = toString(t.getSubject());
                String p = t.getPredicate().toString();
                String o = toString(t.getObject());
                txt = String.format("<html>%s<br/>%s<br/>%s</html>", s, p, o);
            } else {
                txt = value != null ? value.toString() : "";
            }
            tree.setToolTipText(txt);
        }

        @Override
        protected Map<String, Color> getColorMap() {
            return Collections.emptyMap();
        }

        @Override
        protected void prepareStyles() {
            super.prepareStyles();
            StyledDocument doc = textPane.getStyledDocument();
            RDFStyle.styles().forEach(s -> StyleConstants.setForeground(doc.addStyle(s.name(), null), s.getColor()));
        }

        @Override
        protected void prepareTextPane(Object value, boolean selected) {
            textPane.setBorder(null);
            StyledDocument doc = textPane.getStyledDocument();
            resetStyles(doc);
            if (value instanceof Triple) {
                setTripleText((Triple) value, doc);
            } else {
                textPane.setText(value == null ? "" : String.valueOf(value));
            }
            if (selected) {
                doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
            } else {
                doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
            }
            textPane.setFont(plainFont);
        }

        private void setTripleText(Triple t, StyledDocument doc) {
            boolean root = t instanceof RDFHierarchyProvider.RootTriple;
            Node s = t.getSubject();
            Node p = t.getPredicate();
            Node o = t.getObject();
            PrefixMapping pm = getProvider().getPrefixes();
            Function<Object, String> bm = getOWLModelManager().getBlankNodeMapper();

            String s_txt = root ? toSubjectTxt(s, pm, bm) : "";
            String p_txt = toPredicateTxt(p, pm);
            String o_txt = toObjectTxt(o, pm, bm);
            String txt = String.format("%s %s %s", s_txt, p_txt, o_txt);
            int s_start = 0;
            int s_length = s_txt.length();
            int p_start = s_txt.length() + 1;
            int p_length = p_txt.length();
            int o_start = s_txt.length() + +p_txt.length() + 2;
            int o_length = o_txt.length();
            textPane.setText(txt);
            setStyle(doc, s, s_start, s_length);
            setStyle(doc, p, p_start, p_length);
            setStyle(doc, o, o_start, o_length);
            if (root)
                doc.setCharacterAttributes(0, txt.length(), boldStyle, false);
        }

        private void setStyle(StyledDocument doc, Node n, int start, int length) {
            RDFStyle.styles().filter(x -> x.belong(n)).findFirst()
                    .map(x -> doc.getStyle(x.name()))
                    .ifPresent(x -> doc.setCharacterAttributes(start, length, x, true));
        }

        @Override
        protected String getRendering(Object object) {
            throw new IllegalStateException();
        }

        @Override
        protected void highlightText(StyledDocument doc, boolean selected) {
            throw new IllegalStateException();
        }

        private String toSubjectTxt(Node s, PrefixMapping pm, Function<Object, String> bm) {
            return PrintUtils.printSubject(s, pm, bm);
        }

        private String toPredicateTxt(Node p, PrefixMapping pm) {
            return PrintUtils.printPredicate(p, pm);
        }

        private String toObjectTxt(Node o, PrefixMapping pm, Function<Object, String> bm) {
            return PrintUtils.printObject(o, pm, bm, isWrap());
        }

        private String toString(Node n) {
            return PrintUtils.toString(n, getOWLModelManager().getBlankNodeMapper());
        }
    }
}
