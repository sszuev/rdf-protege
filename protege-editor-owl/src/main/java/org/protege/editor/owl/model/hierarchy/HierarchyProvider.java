package org.protege.editor.owl.model.hierarchy;

import org.protege.editor.core.Disposable;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An interface to an object that can provide a hierarchy of objects,
 * for example a class, property or individual hierarchy.
 * <p>
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Jun-2006<br><br>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 *
 * @param <N> - any ontology object
 */
public interface HierarchyProvider<N> extends Disposable, HasFilter<N> {


    /**
     * Sets the ontologies that this hierarchy provider should use in order to determine the hierarchy.
     *
     * @param ontologies a {@code Set} of {@link OWLOntology ontologies}
     */
    void setOntologies(Set<OWLOntology> ontologies);

    /**
     * Gets the objects that represent the roots of the hierarchy.
     *
     * @return a {@code Set} of {@link OWLOntology ontologies}
     */
    Set<N> getRoots();

    Set<N> getChildren(N object);

    Set<N> getDescendants(N object);

    Set<N> getParents(N object);

    Set<N> getAncestors(N object);

    Set<N> getEquivalents(N object);

    Set<List<N>> getPathsToRoot(N object);

    boolean containsReference(N object);

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