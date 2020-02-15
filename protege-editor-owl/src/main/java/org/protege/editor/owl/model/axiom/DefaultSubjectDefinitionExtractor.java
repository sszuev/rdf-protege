package org.protege.editor.owl.model.axiom;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 27/05/2014
 */
@SuppressWarnings("NullableProblems")
public class DefaultSubjectDefinitionExtractor implements SubjectDefinitionExtractor {

    @Override
    public Set<OWLAxiom> getDefiningAxioms(final OWLObject subject, final OWLOntology ontology) {
        return definingAxioms(subject, ontology).collect(Collectors.toSet());
    }

    private Stream<? extends OWLAxiom> definingAxioms(OWLObject subject, OWLOntology ontology) {
        return subject.accept(new OWLObjectVisitorEx<Stream<? extends OWLAxiom>>() {

            @Override
            public Stream<? extends OWLAxiom> visit(OWLClass cls) {
                return ontology.axioms(cls, Imports.EXCLUDED);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLObjectProperty property) {
                return ontology.axioms(property, Imports.EXCLUDED);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLDataProperty property) {
                return ontology.axioms(property, Imports.EXCLUDED);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLNamedIndividual individual) {
                return ontology.axioms(individual, Imports.EXCLUDED);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLDatatype datatype) {
                return ontology.axioms(datatype, Imports.EXCLUDED);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLAnnotationProperty property) {
                return ontology.axioms(property, Imports.EXCLUDED);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(IRI iri) {
                Stream<? extends OWLAxiom> s1 = ontology.annotationAssertionAxioms(iri);
                Stream<? extends OWLAxiom> s2 = ontology.entitiesInSignature(iri, Imports.INCLUDED)
                        .flatMap(e -> definingAxioms(e, ontology));
                return Stream.concat(s1, s2);
            }
        });
    }
}
