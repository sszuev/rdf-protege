package org.protege.editor.owl.ui.view.rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Describes an input for {@link AddTriplePanel}.
 * <p>
 * Created by @ssz on 25.01.2020.
 */
public interface AddTripleModel {

    String getBaseURI();

    PrefixMapping getPrefixMapping();

    Set<Property> getProperties();

    Set<Resource> getDatatypes();

    default String[] getLanguageTags() {
        return Arrays.stream(Locale.getAvailableLocales())
                .map(Locale::getLanguage)
                .filter(x -> Objects.nonNull(x) && !x.isEmpty())
                .distinct()
                .toArray(String[]::new);
    }

    static String[] toArray(Collection<? extends Resource> resources, PrefixMapping pm) {
        return toArray(resources, u -> pm.shortForm(u.getURI()));
    }

    static <X> String[] toArray(Collection<X> resources, Function<X, String> getURI) {
        return toArray(resources.stream(), getURI);
    }

    static <X> String[] toArray(Stream<X> resources, Function<X, String> getURI) {
        return resources.map(x -> Objects.requireNonNull(getURI.apply(x))).toArray(String[]::new);
    }
}
