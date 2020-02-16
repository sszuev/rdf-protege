package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An abstraction for hierarchy provider that has ontologies.
 *
 * @param <N> - {@link OWLObject}
 */
public abstract class AbstractOWLOntologyObjectHierarchyProvider<N extends OWLObject>
        extends AbstractOWLObjectHierarchyProvider<N> implements OWLOntologyObjectHierarchyProvider<N> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOWLOntologyObjectHierarchyProvider.class);

    private final Collection<OWLOntology> ontologies;
    private final OWLOntologyChangeListener listener;

    protected AbstractOWLOntologyObjectHierarchyProvider(OWLOntologyManager manager) {
        super(manager);
        ontologies = createOntologyCollection();
        listener = createOntologyChangeListener();
        getManager().addOntologyChangeListener(listener);
    }

    protected Collection<OWLOntology> createOntologyCollection() {
        return new HashSet<>();
    }

    @SuppressWarnings("NullableProblems")
    protected OWLOntologyChangeListener createOntologyChangeListener() {
        return new OWLOntologyChangeListener() {
            @Override
            public String toString() {
                return "OntologyChangeListenerFor{" + AbstractOWLOntologyObjectHierarchyProvider.this + "}";
            }

            @Override
            public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
                try {
                    AbstractOWLOntologyObjectHierarchyProvider.this.handleChanges(changes);
                } catch (Exception e) {
                    LOGGER.error("Can't handle changes {}: '{}'", changes, e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        this.ontologies.clear();
        this.ontologies.addAll(ontologies);
        rebuild();
    }

    protected void rebuild() {
        fireHierarchyChanged();
    }

    public Stream<OWLOntology> ontologies() {
        return ontologies.stream();
    }

    protected boolean contains(OWLOntology ont) {
        return ontologies.contains(ont);
    }

    public void dispose() {
        super.dispose();
        getManager().removeOntologyChangeListener(listener);
    }
}
