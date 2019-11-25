package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.jena.utils.BuiltIn;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RDF styles.
 * Created by @ssz on 25.11.2019.
 */
public enum RDFStyle {
    URI_RDF(RDF.getURI(), new Color(10, 94, 168)),
    URI_RDFS(RDFS.getURI(), new Color(16, 40, 181, 227)),
    URI_XSD(XSD.getURI(), new Color(50, 70, 44, 246)),
    URI_OWL(OWL.getURI(), new Color(178, 0, 178)),
    BLANK(null, new Color(156, 5, 5, 241)) {
        @Override
        public boolean belong(Node node) {
            return node.isBlank();
        }
    },
    LITERAL(null, new Color(178, 178, 178)) {
        @Override
        public boolean belong(Node node) {
            return node.isLiteral();
        }
    };

    private final String ns;
    private final Color color;
    private Set<String> uris;

    RDFStyle(String ns, Color color) {
        this.ns = ns;
        this.color = Objects.requireNonNull(color);
    }

    public boolean belong(Node node) {
        return node.isURI() && getURIs().contains(node.getURI());
    }

    public Color getColor() {
        return color;
    }

    private Set<String> getURIs() {
        return uris == null ? uris = collect() : uris;
    }

    private Set<String> collect() {
        return BuiltIn.get().reserved().stream()
                .map(Resource::getURI)
                .filter(x -> x.startsWith(ns)).collect(Collectors.toSet());
    }

    public static Stream<RDFStyle> styles() {
        return Arrays.stream(values());
    }
}
