package org.protege.editor.owl.ui.frame;

import org.protege.editor.core.ui.list.MListItem;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * An <code>OWLFrameSectionRow</code> constitutes a row in a frame section, which represents an axiom.
 * <p>
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 *
 * @param <R> - anything
 * @param <A> - {@link OWLAxiom}
 * @param <E> - anything
 */
public interface OWLFrameSectionRow<R, A extends OWLAxiom, E> extends OWLFrameObject<E>, MListItem {

    /**
     * Gets the frame section which this row belongs to.
     *
     * @return {@link OWLFrameSection}
     */
    OWLFrameSection<R, A, E> getFrameSection();

    /**
     * Gets the root object of the frame that this row belongs to.
     *
     * @return {@link R}
     */
    R getRoot();

    /**
     * Gets the axiom that the row holds.
     *
     * @return {@link A}
     */
    A getAxiom();

    /**
     * This row represents an assertion in a particular ontology.
     * This gets the ontology that the assertion belongs to.
     *
     * @return {@link OWLOntology}
     */
    OWLOntology getOntology();

    OWLOntologyManager getOWLOntologyManager();

    /**
     * Determines if this row is editable.
     * If a row is editable then it may be deleted/removed or edited.
     * A delete corresponds to the axiom that the row contains being removed from an ontology that contains it.
     *
     * @return {@code true} if the row is editable, {@code false} if the row is not editable
     */
    boolean isEditable();

    boolean isDeleteable();

    boolean isInferred();

    /**
     * Gets the changes which are required to delete this row.
     * If the row cannot be deleted this list will be empty.
     *
     * @return {@code Collection}
     */
    Collection<? extends OWLOntologyChange> getDeletionChanges();

    /**
     * Gets a list of objects contained in this row.
     * These objects could be placed on the clip board during a copy operation, or navigated to etc.
     *
     * @return {@code Stream}
     */
    Stream<? extends OWLObject> manipulatableObjects();

    @Deprecated
    default Collection<? extends OWLObject> getManipulatableObjects() {
        return manipulatableObjects().collect(Collectors.toSet());
    }
}
