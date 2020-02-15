package org.protege.editor.owl.model.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19 Sep 16
 */
@RunWith(MockitoJUnitRunner.class)
public class DefinedClassPredicate_TestCase {

    @Mock
    private OWLClass cls;

    @Mock
    private OWLOntology ontology;

    @Mock
    private OWLEquivalentClassesAxiom equivalentClassesAxiom;

    @Mock
    private OWLDisjointUnionAxiom disjointUnionAxiom;

    private DefinedClassPredicate predicate;


    @Before
    public void setUp() {
        predicate = new DefinedClassPredicate(singleton(ontology));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfOntologiesIsNull() {
        new DefinedClassPredicate(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionIfClassIsNull() {
        predicate.test(null);
    }

    @Test
    public void shouldNotBeDefinedClass() {
        mock(null, null);
        assertThat(predicate.test(cls), is(false));
    }

    @Test
    public void shouldBeDefinedByEquivalentClassesAxiom() {
        mock(null, equivalentClassesAxiom);
        assertThat(predicate.test(cls), is(true));
    }

    @Test
    public void shouldBeDefinedByDisjointUnionAxiom() {
        mock(disjointUnionAxiom, null);
        assertThat(predicate.test(cls), is(true));
    }

    private void mock(OWLDisjointUnionAxiom d, OWLEquivalentClassesAxiom a) {
        when(ontology.disjointUnionAxioms(cls)).thenReturn(Stream.of(d).filter(Objects::nonNull));
        when(ontology.equivalentClassesAxioms(cls)).thenReturn(Stream.of(a).filter(Objects::nonNull));
    }

}
