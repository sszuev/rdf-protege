package org.protege.editor.owl.ui.view.rdf.utils;

import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.shared.PrefixMapping;

import java.util.function.Function;

/**
 * Created by @ssz on 07.01.2021.
 */
public class PrintUtils {

    public static String printTriple(Triple triple, PrefixMapping pm, Function<Object, String> bm, boolean wrap) {
        return String.format("%s %s %s",
                printSubject(triple.getSubject(), pm, bm),
                printPredicate(triple.getPredicate(), pm),
                printObject(triple.getObject(), pm, bm, wrap));
    }

    public static String printSubject(Node s, PrefixMapping pm, Function<Object, String> bm) {
        return s.isURI() ? toString(s.getURI(), pm) : toString(bm, s.getBlankNodeId());
    }

    public static String printPredicate(Node p, PrefixMapping pm) {
        return toString(p.getURI(), pm);
    }

    public static String printObject(Node o, PrefixMapping pm, Function<Object, String> bm, boolean wrap) {
        if (o.isURI())
            return toString(o.getURI(), pm);
        if (o.isBlank())
            return toString(bm, o.getBlankNodeId());
        return toString(o.getLiteral(), pm, wrap);
    }

    private static String toString(String uri, PrefixMapping pm) {
        String res = pm.shortForm(uri);
        if (res.isEmpty() || ":".equals(res) || res.equals(uri)) {
            return "<" + uri + ">";
        }
        return res;
    }

    public static String toString(Node node, Function<Object, String> bm) {
        if (node.isURI()) {
            return node.getURI();
        }
        if (node.isBlank()) {
            return toString(bm, node.getBlankNodeId());
        }
        return node.toString();
    }

    private static String toString(Function<Object, String> blankNodeMapper, BlankNodeId id) {
        return blankNodeMapper.apply(id);
    }

    public static String toString(LiteralLabel label, PrefixMapping pm, boolean wrap) {
        String txt = formatMessage(label.getLexicalForm(), wrap);
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

    private static String formatMessage(String txt, boolean wrap) {
        if (wrap) {
            return "\"" + txt + "\"";
        }
        // inline:
        return txt.replace('\n', ' ').replaceAll("\\s+", " ");
    }
}
