package org.protege.editor.owl.ui.view.rdf;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * A special triple to describe UI roots.
 */
public class RootTriple extends Triple {
    protected RootTriple(Triple t) {
        this(t.getSubject(), t.getPredicate(), t.getObject());
    }

    protected RootTriple(Node s, Node p, Node o) {
        super(s, p, o);
    }
}
