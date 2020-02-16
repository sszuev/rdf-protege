package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

/**
 * A basic partial implementation of a hierarchy provider,
 * which handles listeners and event firing, and also provides basic implementations
 * of method such as {@link #getAncestors}, {@link #getDescendants}, etc, which use other core methods.
 * <p>
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Jun-2006<br><br>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public abstract class AbstractOWLObjectHierarchyProvider<N> implements OWLHierarchyProvider<N> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOWLObjectHierarchyProvider.class);
    /*
     * The listeners object synchronizes the listeners data.
     */
    private final List<HierarchyProviderListener<N>> listeners;
    private volatile boolean fireEvents;
    private final OWLOntologyManager manager;

    private Predicate<N> filter = n -> true;

    /*
     * If you expect this or any of its subclasses to be thread safe it must be a WriteSafeOWLOntologyManager.
     * Ideally we would change the interface here but this might break some existing plugin code.  On the other hand,
     * all Protege code will pass in a ProtegeOWLOntologyManager and Web-Protege will pass in another implementation of the
     * WriteSafeOWLOntologyManager.
     */
    protected AbstractOWLObjectHierarchyProvider(OWLOntologyManager owlOntologyManager) {
//        if (!(owlOntologyManager instanceof WriteSafeOWLOntologyManager)) { // I know this is ugly but it fixes problems elsewhere...
//        	throw new IllegalStateException("Hierarchy providers must have a thread safe ontology mananger.");
//        }
        this.manager = owlOntologyManager;
        listeners = new ArrayList<>();
        fireEvents = true;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    @Override
    public void clearFilter() {
        this.filter = n -> true;
        fireHierarchyChanged();
    }

    @Override
    public Predicate<N> getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Predicate<N> filter) {
        this.filter = checkNotNull(filter);
        fireHierarchyChanged();
    }

    @Override
    public void dispose() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

    @Override
    public Set<N> getAncestors(N object) {
        Set<N> results = new HashSet<>();
        getAncestors(results, object);
        return results;
    }

    private void getAncestors(Set<N> results, N object) {
        parents(object)
                .filter(x -> !results.contains(x))
                .forEach(x -> {
                    results.add(x);
                    getAncestors(results, x);
                });
    }

    @Override
    public final Set<N> getChildren(N object) {
        return children(object).collect(toSet());
    }

    @Override
    public final Stream<N> children(N object) {
        return unfilteredChildren(object).filter(filter);
    }

    protected Stream<N> unfilteredChildren(N object) {
        return getUnfilteredChildren(object).stream();
    }

    protected Set<N> getUnfilteredChildren(N object) {
        return Collections.emptySet();
    }

    @Override
    public Set<N> getDescendants(N object) {
        Set<N> results = new HashSet<>();
        getDescendants(results, object);
        return results;
    }

    private void getDescendants(Set<N> results, N object) {
        children(object)
                .filter(child -> !results.contains(child))
                .forEach(child -> {
                    results.add(child);
                    getDescendants(results, child);
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Paths to root stuff
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Gets the paths to the root class for the specified object.
     *
     * @return A <code>Set</code> of <code>List</code>s of <code>N</code>s
     */
    @Override
    public Set<List<N>> getPathsToRoot(N obj) {
        return setOfPaths(obj, new HashSet<>());
    }

    private Set<List<N>> setOfPaths(N obj, Set<N> processed) {
        if (hasRoot(obj)) {
            return getSingleSetOfLists(obj);
        }
        Set<List<N>> paths = new HashSet<>();
        parents(obj)
                .filter(x -> !processed.contains(x))
                .forEach(x -> {
                    processed.add(x);
                    paths.addAll(append(obj, setOfPaths(x, processed)));
                });
        return paths;
    }

    private Set<List<N>> getSingleSetOfLists(N obj) {
        Set<List<N>> set = new HashSet<>();
        List<N> list = new ArrayList<>();
        list.add(obj);
        set.add(list);
        return set;
    }

    private Set<List<N>> append(N obj, Set<List<N>> setOfPaths) {
        for (List<N> path : setOfPaths) {
            path.add(obj);
        }
        return setOfPaths;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    protected void setFireEvents(boolean b) {
        fireEvents = b;
    }

    @Override
    public void addListener(HierarchyProviderListener<N> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(HierarchyProviderListener<N> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private List<HierarchyProviderListener<N>> getListeners() {
        synchronized (listeners) {
            return new ArrayList<>(listeners);
        }
    }

    protected void fireNodeChanged(N node) {
        if (!fireEvents) {
            return;
        }
        for (HierarchyProviderListener<N> listener : getListeners()) {
            try {
                listener.nodeChanged(node);
            } catch (Throwable e) {
                LOGGER.error("{}: Listener {} has thrown an exception: '{}'. Removing bad listener.",
                        getClass().getName(), listener, e.getMessage(), e);
                removeListener(listener);
                throw new RuntimeException(e);
            }
        }
    }

    protected void fireHierarchyChanged() {
        if (!fireEvents) {
            return;
        }
        for (HierarchyProviderListener<N> listener : getListeners()) {
            try {
                listener.hierarchyChanged();
            } catch (Throwable e) {
                LOGGER.error("{}: Listener {} has thrown an exception: '{}'",
                        getClass().getName(), listener, e.getMessage(), e);
            }
        }
    }
}
