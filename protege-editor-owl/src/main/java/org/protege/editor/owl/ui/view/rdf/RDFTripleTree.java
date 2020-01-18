package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.protege.editor.owl.ui.tree.OWLTreePreferences;
import org.protege.editor.owl.ui.tree.ObjectTree;

import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a {@link javax.swing.JTree}-component for rendering {@link Triple}s-tree.
 * TODO: currently it is not fully ready: it is read-only and ugly.
 * <p>
 * Created by @ssz on 23.11.2019.
 */
@SuppressWarnings("WeakerAccess")
public class RDFTripleTree extends ObjectTree<Triple> {

    //    private OWLModelManagerListener listener;
//    private OWLEntityRendererListener rendererListener;
//    public OWLModelManagerEntityRenderer currentRenderer = null; // only use to clean up old listeners

    public RDFTripleTree(OWLEditorKit kit, TripleHierarchyProvider provider) {
        super(kit, provider);
        initialise(kit);
    }

    @Override
    public Comparator<? super Triple> getCopyComparator() {
        return null;
    }

    @Override
    protected Comparator<? super Triple> getRootNodeComparator() {
        return getProvider().getComparator();
    }

    @Override
    protected Comparator<? super Triple> getChildNodeComparator() {
        return getProvider().getComparator();
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
    public TripleHierarchyProvider getProvider() {
        return (TripleHierarchyProvider) provider;
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
        Set<Triple> children = prov.getChildren(node);
        if (children.size() <= childCountLimit) {
            for (OWLObjectTreeNode<Triple> treeNode : getNodes(node)) {
                TreePath path = new TreePath(treeNode.getPath());
                expandPath(path);
            }
            for (Triple child : children) {
                autoExpand(child, currentDepth + 1);
            }
        }
    }

    OWLTreePreferences getTreePreferences() {
        return OWLTreePreferences.getInstance();
    }

    private void setupListener() {
//        listener = event -> {
//            if (event.isType(EventType.ENTITY_RENDERER_CHANGED)) {
//                refreshEntityRenderer();
//            }
//        };
//        getOWLModelManager().addListener(listener);
        // rendererListener = (entity, renderer) -> handleRenderingChanged(entity);
        refreshEntityRenderer();
    }

    private void handleRenderingChanged(Triple entity) {
        for (OWLObjectTreeNode<Triple> node : getNodes(entity)) {
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.nodeStructureChanged(node);
        }
    }

    private void refreshEntityRenderer() {
        invalidate();
//        if (currentRenderer != null){
//            currentRenderer.removeListener(rendererListener);
//        }
//        currentRenderer = getOWLModelManager().getOWLEntityRenderer();
//        currentRenderer.addListener(rendererListener);
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

    protected void handlePopupMenuInvoked(TreePath path, Point pt) {
    }

    @Override
    public void dispose() {
        super.dispose();
//        getOWLModelManager().removeListener(listener);
//        getOWLModelManager().getOWLEntityRenderer().removeListener(rendererListener);
    }

    protected class CellRenderer extends OWLObjectTreeCellRenderer {

        protected CellRenderer(OWLEditorKit kit) {
            super(kit);
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
            PrefixMapping pm = getProvider().getPrefixes();
            textPane.setBorder(null);
            StyledDocument doc = textPane.getStyledDocument();
            resetStyles(doc);
            if (value instanceof Triple) {
                Triple t = (Triple) value;
                Node s = t.getSubject();
                Node p = t.getPredicate();
                Node o = t.getObject();
                String s_txt = toSubjectTxt(s, pm);
                String p_txt = toPredicateTxt(p, pm);
                String o_txt = toObjectTxt(o, pm);
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

        private String toSubjectTxt(Node s, PrefixMapping pm) {
            return s.isURI() ? toString(s.getURI(), pm) : toString(s.getBlankNodeId());
        }

        private String toPredicateTxt(Node p, PrefixMapping pm) {
            return toString(p.getURI(), pm);
        }

        private String toObjectTxt(Node o, PrefixMapping pm) {
            if (o.isURI())
                return toString(o.getURI(), pm);
            if (o.isBlank())
                return toString(o.getBlankNodeId());
            return toString(o.getLiteral(), pm);
        }

        private String toString(String uri, PrefixMapping pm) {
            String res = pm.shortForm(uri);
            if (res.isEmpty() || ":".equals(res) || res.equals(uri)) {
                return "<" + uri + ">";
            }
            return res;
        }

        private String toString(BlankNodeId id) {
            return getProvider().getBlankNodeLable(id);
        }

        private String toString(LiteralLabel label, PrefixMapping pm) {
            String txt = formatMessage(label.getLexicalForm());
            String lang = label.language();
            String dt = label.getDatatypeURI();
            if (lang != null && !lang.isEmpty()) {
                return txt + "@" + lang;
            }
            if (RDF.PlainLiteral.getURI().equals(dt)
                    || RDF.langString.getURI().equals(dt) || XSD.xstring.getURI().equals(dt)) {
                return txt;
            }
            return txt + "^^" + toString(dt, pm);
        }

        private String formatMessage(String txt) {
            if (!isWrap()) {
                return txt.replace('\n', ' ').replaceAll("\\s+", " ");
            }
            return txt;
        }
    }
}
