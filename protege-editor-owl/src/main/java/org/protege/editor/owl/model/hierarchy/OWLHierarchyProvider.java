package org.protege.editor.owl.model.hierarchy;

import org.protege.editor.core.Disposable;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An interface to an object that can provide a hierarchy of objects,
 * for example a class, property or individual hierarchy.
 * <p>
 *
 * @param <N> - any ontology object
 */
public interface OWLHierarchyProvider<N> extends Disposable, HasFilter<N>, HierarchyProvider<N> {

    /**
     * Sets the ontologies that this hierarchy provider should use in order to determine the hierarchy.
     *
     * @param ontologies a {@code Set} of {@link OWLOntology ontologies}
     */
    void setOntologies(Set<OWLOntology> ontologies);

    void addListener(HierarchyProviderListener<N> listener);

    void removeListener(HierarchyProviderListener<N> listener);

    void dispose(); // override as previous implementations did not implement Disposable and did not throw an exception

    @Override
    default void clearFilter() {
        // Do nothing
    }

    @Override
    default Predicate<N> getFilter() {
        // No filtering - everything gets through.
        return n -> true;
    }

    @Override
    default void setFilter(Predicate<N> filter) {
        // Do nothing
    }

    default void setOntology(OWLOntology ont) {
        setOntologies(Collections.singleton(ont));
    }
}
