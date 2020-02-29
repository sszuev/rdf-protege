package org.protege.editor.owl.ui.view.rdf;

import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Describes an input for the {@link AddTriplePanel}.
 * <p>
 * Created by @ssz on 25.01.2020.
 */
public interface TripleModel {

    static String[] toArray(Collection<? extends Resource> resources, PrefixMapping pm) {
        return toArray(resources, u -> pm.shortForm(u.getURI()));
    }

    static <X> String[] toArray(Collection<X> resources, Function<X, String> getURI) {
        return toArray(resources.stream(), getURI);
    }

    static <X> String[] toArray(Stream<X> resources, Function<X, String> getURI) {
        return resources.map(x -> Objects.requireNonNull(getURI.apply(x))).toArray(String[]::new);
    }

    /**
     * Returns a base uri to be used as a prefix when drawing IRI fields.
     *
     * @return {@code String}
     */
    String getBaseURI();

    /**
     * Returns a prefix mapping.
     *
     * @return {@link PrefixMapping}
     */
    PrefixMapping getPrefixMapping();

    /**
     * Returns a collection of built-in (system) properties (e.g. {@code rdf:type}).
     *
     * @return a {@code Collection} of {@link Property}s
     */
    Collection<Property> getProperties();

    /**
     * Returns collection of built-in (system) datatypes  (e.g. {@code xsd:string}).
     *
     * @return a {@code Collection} of {@link Resource}s
     */
    Collection<Resource> getDatatypes();

    /**
     * Returns the model's blank node roots to draw an object input.
     *
     * @return a {@code Collection} of {@link BNode}s
     */
    Collection<BNode> getBlankNodes();

    default String[] getLanguageTags() {
        return Arrays.stream(Locale.getAvailableLocales())
                .map(Locale::getLanguage)
                .filter(x -> Objects.nonNull(x) && !x.isEmpty())
                .distinct()
                .toArray(String[]::new);
    }

    /**
     * Container for the model's nodes.
     */
    class BNode {
        private final BlankNodeId id;
        private final String label;

        public BNode(BlankNodeId id, String label) {
            this.id = Objects.requireNonNull(id);
            this.label = Objects.requireNonNull(label);
        }

        @Override
        public String toString() {
            return label;
        }

        public BlankNodeId getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BNode bNode = (BNode) o;
            return id.equals(bNode.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
