package org.protege.editor.owl.model.find;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 16-May-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public interface OWLEntityFinder {

    OWLClass getOWLClass(String rendering);

    OWLObjectProperty getOWLObjectProperty(String rendering);

    OWLDataProperty getOWLDataProperty(String rendering);

    OWLAnnotationProperty getOWLAnnotationProperty(String rendering);

    OWLNamedIndividual getOWLIndividual(String rendering);

    OWLDatatype getOWLDatatype(String rendering);

    OWLEntity getOWLEntity(String rendering);

    Set<OWLClass> getMatchingOWLClasses(String match);

    Set<OWLClass> getMatchingOWLClasses(String match, boolean fullRegExp);

    Set<OWLObjectProperty> getMatchingOWLObjectProperties(String match);

    Set<OWLObjectProperty> getMatchingOWLObjectProperties(String match, boolean fullRegExp);

    Set<OWLDataProperty> getMatchingOWLDataProperties(String match);

    Set<OWLDataProperty> getMatchingOWLDataProperties(String match, boolean fullRegExp);

    Set<OWLNamedIndividual> getMatchingOWLIndividuals(String match);

    Set<OWLNamedIndividual> getMatchingOWLIndividuals(String match, boolean fullRegExp);

    Set<OWLDatatype> getMatchingOWLDatatypes(String match);

    Set<OWLDatatype> getMatchingOWLDatatypes(String match, boolean fullRegExp);

    Set<OWLAnnotationProperty> getMatchingOWLAnnotationProperties(String match);

    Set<OWLAnnotationProperty> getMatchingOWLAnnotationProperties(String match, boolean fullRegExp);

    Set<OWLEntity> getEntities(IRI iri);

    /**
     * Searches for an entity of the specified type with the specified rendering.
     *
     * @param type      - the type of entity to search for
     * @param rendering - the rendering of the entity to search for
     * @param <E>       - subtype of {@link OWLEntity}
     * @return the entity that has the specified rendering and the specified type;
     * an empty value will be returned if no such entity was found.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    default <E extends OWLEntity> Optional<E> getOWLEntity(@Nonnull EntityType<E> type, @Nonnull String rendering) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(rendering);
        if (type == EntityType.CLASS) {
            return Optional.ofNullable((E) getOWLClass(rendering));
        }
        if (type == EntityType.OBJECT_PROPERTY) {
            return Optional.ofNullable((E) getOWLObjectProperty(rendering));
        }
        if (type == EntityType.DATA_PROPERTY) {
            return Optional.ofNullable((E) getOWLDataProperty(rendering));
        }
        if (type == EntityType.ANNOTATION_PROPERTY) {
            return Optional.ofNullable((E) getOWLAnnotationProperty(rendering));
        }
        if (type == EntityType.NAMED_INDIVIDUAL) {
            return Optional.ofNullable((E) getOWLIndividual(rendering));
        }
        if (type == EntityType.DATATYPE) {
            return Optional.ofNullable((E) getOWLDatatype(rendering));
        }
        throw new IllegalArgumentException("Unknown EntityType: " + type);
    }
}
