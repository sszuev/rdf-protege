package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.jena.utils.Graphs;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;

import java.util.Comparator;

/**
 * Utilities to work with {@link Graph}, {@link Triple}s and {@link Node}s.
 * Created by @ssz on 25.11.2019.
 *
 * @see Graphs
 */
@SuppressWarnings("WeakerAccess")
public class GraphUtils {

    /**
     * Decides is the given graph is big.
     * For big graphs sorting is disabled.
     * Non-memory graphs are considered as big by default.
     *
     * @param g {@link Graph}, possibly {@code null}
     * @return boolean
     */
    public static boolean isBig(Graph g) {
        return !Graphs.baseGraphs(g).allMatch(x -> x instanceof GraphMem) || g.size() > 30_000;
    }

    /**
     * Answers a graph comparator.
     * Ontology header (id) goes first, then there is sorting by subjects, predicates, etc.
     *
     * @param g {@link Graph}, not {@code null}
     * @return {@link Comparator} for {@link Triple}s
     */
    public static Comparator<Triple> getComparator(Graph g) {
        return Comparator.comparing((Triple x) -> isHeader(x, g))
                .thenComparing(GraphUtils::hasBlankSubject)
                .thenComparing(Comparator.comparing(GraphUtils::getSubjectAsString).reversed())
                .thenComparing(GraphUtils::hasRDFType)
                .thenComparing(Comparator.comparing(GraphUtils::getPredicateAsString).reversed())
                .reversed()
                ;
    }

    private static boolean isHeader(Triple t, Graph g) {
        if (hasRDFType(t) && OWL.Ontology.asNode().equals(t.getObject())) {
            return true;
        }
        return g.contains(t.getSubject(), RDF.type.asNode(), OWL.Ontology.asNode());
    }

    private static String getSubjectAsString(Triple t) {
        return asString(t.getSubject());
    }

    private static String getPredicateAsString(Triple t) {
        return asString(t.getPredicate());
    }

    private static boolean hasRDFType(Triple t) {
        return RDF.type.getURI().equals(t.getPredicate().getURI());
    }

    private static boolean hasBlankSubject(Triple t) {
        return t.getSubject().isBlank();
    }

    private static String asString(Node n) {
        return n.isURI() ? n.getURI() : n.getBlankNodeId().toString();
    }

    /**
     * Answers {@code true} if the specified node is in graph's roots.
     *
     * @param graph {@link Graph}, not {@code null}
     * @param s     {@link Node}, not {@code null}
     * @return boolean
     */
    public static boolean isRoot(Graph graph, Node s) {
        return s.isURI() || !graph.contains(Node.ANY, Node.ANY, s);
    }
}
