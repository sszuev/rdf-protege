package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.Collection;
import java.util.Set;

/**
 * An abstraction for hierarchy providers.
 *
 * @param <N> - any {@link OWLObject}
 */
public interface OWLOntologyObjectHierarchyProvider<N extends OWLObject> extends OWLHierarchyProvider<N> {

    @Override
    void setOntologies(Set<OWLOntology> ontologies);

    void handleChanges(Collection<? extends OWLOntologyChange> changes);
}
