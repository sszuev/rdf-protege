package org.protege.editor.owl.model.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 20 May 16
 */
public class ReferenceFinder {

    /**
     * Gets the references set for the specified entities in the specified ontology.
     *
     * @param entities The entities whose references are to be retrieved. Not {@code null}.
     * @param ontology The ontology.  Not {@code null}.
     * @return The ReferenceSet that contains axioms that reference the specified entities and ontology annotations
     * that reference the specified entities.  Note that, since annotation assertions have subjects that may be IRIs and
     * values that may be IRIs, and ontology annotation have values that may be IRIs, the reference set includes these
     * axioms where the IRI is the IRI of one or more of the specified entities.
     */
    public ReferenceSet getReferenceSet(Collection<? extends OWLEntity> entities, OWLOntology ontology) {
        ImmutableSet.Builder<OWLAxiom> axiomSetBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<OWLAnnotation> ontologyAnnotationSetBuilder = ImmutableSet.builder();

        Collection<IRI> entityIRIs = collectIRIs(entities, ontology, axiomSetBuilder);
        ontology.axioms(AxiomType.ANNOTATION_ASSERTION).forEach(axiom -> {
            OWLAnnotationSubject subject = axiom.getSubject();
            if (subject instanceof IRI && entityIRIs.contains(subject)) {
                axiomSetBuilder.add(axiom);
                return;
            }
            OWLAnnotationValue value = axiom.getValue();
            if (value instanceof IRI && entityIRIs.contains(value)) {
                axiomSetBuilder.add(axiom);
            }
        });

        ontology.annotations().forEach(annotation -> {
            OWLAnnotationValue value = annotation.getValue();
            if (value instanceof IRI && entityIRIs.contains(value)) {
                ontologyAnnotationSetBuilder.add(annotation);
                return;
            }
            if (entities.contains(annotation.getProperty())) {
                ontologyAnnotationSetBuilder.add(annotation);
            }
        });

        return new ReferenceSet(ontology, axiomSetBuilder.build(), ontologyAnnotationSetBuilder.build());
    }

    private Collection<IRI> collectIRIs(Collection<? extends OWLEntity> entities,
                                        OWLOntology ontology,
                                        ImmutableSet.Builder<OWLAxiom> axiomSetBuilder) {
        // @ssz: original logic is preserved
        Set<IRI> res = new HashSet<>(entities.size());
        for (OWLEntity entity : entities) {
            ontology.referencingAxioms(entity).forEach(axiomSetBuilder::add);
            res.add(entity.getIRI());
        }
        // Optimisation for the case where there is just one entity.
        if (res.size() == 1) {
            res = Collections.singleton(res.iterator().next());
        }
        return res;
    }

    public static final class ReferenceSet {
        private final OWLOntology ontology;
        private final ImmutableCollection<OWLAxiom> referencingAxioms;
        private final ImmutableCollection<OWLAnnotation> referencingOntologyAnnotations;

        public ReferenceSet(OWLOntology ontology,
                            ImmutableCollection<OWLAxiom> referencingAxioms,
                            ImmutableCollection<OWLAnnotation> referencingOntologyAnnotations) {
            this.ontology = checkNotNull(ontology);
            this.referencingAxioms = checkNotNull(referencingAxioms);
            this.referencingOntologyAnnotations = checkNotNull(referencingOntologyAnnotations);
        }

        public OWLOntology getOntology() {
            return ontology;
        }

        public ImmutableCollection<OWLAxiom> getReferencingAxioms() {
            return referencingAxioms;
        }

        public ImmutableCollection<OWLAnnotation> getReferencingOntologyAnnotations() {
            return referencingOntologyAnnotations;
        }
    }
}
