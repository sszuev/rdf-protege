package org.protege.editor.owl.model.hierarchy;

/**
 * TODO: rename
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Jun-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public interface OWLObjectHierarchyProviderListener<N> {

    /**
     * Notifies the listener that the parents and or children
     * of the specified node might have changed.  This is usually
     * called in response to an add/remove axiom change.
     */
    void nodeChanged(N node);

    /**
     * Notifies the listener that the complete hierarchy
     * has changed.
     */
    void hierarchyChanged();
}
