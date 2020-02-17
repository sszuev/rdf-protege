package org.protege.editor.owl.model.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 20 May 16
 * todo: using mockito here was brilliant idea.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReferenceFinder_TestCase {

    private ReferenceFinder referenceFinder;
    @Mock
    private OWLOntology ontology;
    @Mock
    private OWLAnnotationProperty entity;
    @Mock
    private OWLAxiom axiom;
    @Mock
    private OWLAnnotationAssertionAxiom annotationAssertionAxiom;
    @Mock
    private IRI iri;
    @Mock
    private OWLAnnotation ontologyAnnotation;

    @Before
    public void setUp() {
        referenceFinder = new ReferenceFinder();
        when(entity.getIRI()).thenReturn(iri);
    }

    @Test
    public void shouldRetrieveNonAnnotationAssertionAxiom() {
        when(ontology.referencingAxioms(entity)).thenReturn(Stream.of(axiom));
        when(ontology.annotations()).thenReturn(Stream.empty());
        when(ontology.axioms(AxiomType.ANNOTATION_ASSERTION)).thenReturn(Stream.empty());


        ReferenceFinder.ReferenceSet referenceSet = getReferenceSet();
        assertThat(referenceSet.getReferencingAxioms(), hasItem(axiom));
    }

    @Test
    public void shouldRetrieveAnnotationAssertionAxiomBySubjectReference() {
        when(ontology.axioms(AxiomType.ANNOTATION_ASSERTION)).thenReturn(Stream.of(annotationAssertionAxiom));
        when(ontology.referencingAxioms(entity)).thenReturn(Stream.empty());
        when(ontology.annotations()).thenReturn(Stream.empty());

        when(annotationAssertionAxiom.getSubject()).thenReturn(iri);
        when(annotationAssertionAxiom.getValue()).thenReturn(mock(IRI.class));

        ReferenceFinder.ReferenceSet referenceSet = getReferenceSet();
        assertThat(referenceSet.getReferencingAxioms(), hasItem(annotationAssertionAxiom));
    }

    @Test
    public void shouldRetrieveAnnotationAssertionAxiomByObjectReference() {
        when(ontology.axioms(AxiomType.ANNOTATION_ASSERTION)).thenReturn(Stream.of(annotationAssertionAxiom));
        when(ontology.referencingAxioms(entity)).thenReturn(Stream.empty());
        when(ontology.annotations()).thenReturn(Stream.empty());

        when(annotationAssertionAxiom.getValue()).thenReturn(iri);
        when(annotationAssertionAxiom.getSubject()).thenReturn(mock(IRI.class));

        ReferenceFinder.ReferenceSet referenceSet = getReferenceSet();
        assertThat(referenceSet.getReferencingAxioms(), hasItem(annotationAssertionAxiom));
    }

    @Test
    public void shouldRetrieveOntologyAnnotationsByValue() {
        when(ontology.annotations()).thenReturn(Stream.of(ontologyAnnotation));
        when(ontology.referencingAxioms(entity)).thenReturn(Stream.empty());
        when(ontology.axioms(AxiomType.ANNOTATION_ASSERTION)).thenReturn(Stream.empty());

        when(ontologyAnnotation.getProperty()).thenReturn(mock(OWLAnnotationProperty.class));
        when(ontologyAnnotation.getValue()).thenReturn(iri);

        ReferenceFinder.ReferenceSet referenceSet = getReferenceSet();
        assertThat(referenceSet.getReferencingOntologyAnnotations(), hasItem(ontologyAnnotation));
    }

    @Test
    public void shouldRetrieveOntologyAnnotationsByProperty() {
        when(ontology.annotations()).thenReturn(Stream.of(ontologyAnnotation));
        when(ontology.referencingAxioms(entity)).thenReturn(Stream.empty());
        when(ontology.axioms(AxiomType.ANNOTATION_ASSERTION)).thenReturn(Stream.empty());

        when(ontologyAnnotation.getProperty()).thenReturn(entity);
        when(ontologyAnnotation.getValue()).thenReturn(mock(IRI.class));

        ReferenceFinder.ReferenceSet referenceSet = getReferenceSet();
        assertThat(referenceSet.getReferencingOntologyAnnotations(), hasItem(ontologyAnnotation));
    }

    /**
     * Convenience method to get the ReferenceSet for the entity an ontology.
     * @return  The ReferenceSet.
     */
    private ReferenceFinder.ReferenceSet getReferenceSet() {
        return referenceFinder.getReferenceSet(Collections.singleton(entity), ontology);
    }
}
