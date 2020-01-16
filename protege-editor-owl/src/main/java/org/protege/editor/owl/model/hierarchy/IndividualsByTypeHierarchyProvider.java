package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 24-May-2007<br><br>
 */
public class IndividualsByTypeHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLObject> {

    private final Set<OWLNamedIndividual> untypedIndividuals = new HashSet<>();
    private final Set<OWLClass> classes = new HashSet<>();
    private final Set<OWLOntology> ontologies = new HashSet<>();
    private final OWLOntologyChangeListener ontChangeListener = this::handleOntologyChanges;

    public IndividualsByTypeHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
        owlOntologyManager.addOntologyChangeListener(ontChangeListener);
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        this.ontologies.clear();
        this.ontologies.addAll(ontologies);
        rebuild();
    }

    private void rebuild() {
        classes.clear();
        untypedIndividuals.clear();
        for (OWLOntology ont : ontologies) {
            ont.individualsInSignature().forEach(ind -> {
                if (ont.classAssertionAxioms(ind)
                        .map(OWLClassAssertionAxiom::getClassExpression)
                        .filter(x -> !x.isAnonymous())
                        .map(AsOWLClass::asOWLClass)
                        .peek(classes::add)
                        .count() == 0) {
                    untypedIndividuals.add(ind);
                }
            });
        }
        fireHierarchyChanged();
    }

    @Override
    public Set<OWLObject> getRoots() {
        Set<OWLObject> roots = new HashSet<>(classes);
        roots.addAll(untypedIndividuals);
        return roots;
    }

    @Override
    public Set<OWLObject> getUnfilteredChildren(OWLObject object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<OWLObject> unfilteredChildren(OWLObject object) {
        if (!(object instanceof OWLClass) || !classes.contains(object)) {
            return Stream.empty();
        }
        OWLClass cls = (OWLClass) object;
        return ontologies.stream()
                .flatMap(ont -> ont.classAssertionAxioms(cls))
                .filter(x -> !x.getIndividual().isAnonymous())
                .map(x -> x);
    }

    @Override
    public Set<OWLObject> getParents(OWLObject object) {
        if (!(object instanceof OWLNamedIndividual)) {
            return Collections.emptySet();
        }
        OWLIndividual ind = (OWLNamedIndividual) object;
        return ontologies.stream()
                .flatMap(ont -> ont.classAssertionAxioms(ind))
                .map(OWLClassAssertionAxiom::getClassExpression)
                .filter(x -> !x.isAnonymous())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<OWLObject> getEquivalents(OWLObject object) {
        return Collections.emptySet();
    }

    @Override
    public boolean containsReference(OWLObject object) {
        return object instanceof OWLNamedIndividual || (object instanceof OWLClass && classes.contains(object));
    }

    @Override
    public void dispose() {
        getManager().removeOntologyChangeListener(ontChangeListener);
        super.dispose();
    }

    private void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {
        TypesChangeVisitor changeVisitor = new TypesChangeVisitor();
        for (OWLOntologyChange chg : changes){
            chg.accept(changeVisitor);
        }
        for (OWLObject node : changeVisitor.getNodes()){
            fireNodeChanged(node);
        }
    }

    /**
     * Scans changes for nodes that have changed in the tree
     */
    @SuppressWarnings("NullableProblems")
    class TypesChangeVisitor implements OWLOntologyChangeVisitor {

        private Set<OWLObject> changedNodes = new HashSet<>();

        Set<OWLNamedIndividual> checkIndividuals = new HashSet<>();

        private final OWLAxiomVisitor addAxiomVisitor = new OWLAxiomVisitor() {
            @Override
            public void visit(OWLClassAssertionAxiom ax) {
                handleAddClassAssertionAxiom(ax);
            }
        };
        private final OWLAxiomVisitor removeAxiomVisitor = new OWLAxiomVisitor() {
            @Override
            public void visit(OWLClassAssertionAxiom ax) {
                handleRemoveClassAssertionAxiom(ax);
            }
        };

        public Set<OWLObject> getNodes() {
            for (OWLNamedIndividual ind : checkIndividuals) {
                if (untypedIndividuals.contains(ind)) {
                    if (isTyped(ind) || !isReferenced(ind)) {
                        untypedIndividuals.remove(ind);
                        changedNodes.add(ind);
                    }
                } else if (isReferenced(ind) && !isTyped(ind)) {
                    untypedIndividuals.add(ind);
                    changedNodes.add(ind);
                }
            }
            checkIndividuals.clear(); // only do this once
            return changedNodes;
        }

        @Override
        public void visit(AddAxiom addAxiom) {
            if (!ontologies.contains(addAxiom.getOntology())) {
                return;
            }
            handleAxiomChange(addAxiom);
            addAxiom.getAxiom().accept(addAxiomVisitor);
        }

        @Override
        public void visit(RemoveAxiom removeAxiom) {
            if (!ontologies.contains(removeAxiom.getOntology())) {
                return;
            }
            handleAxiomChange(removeAxiom);
            removeAxiom.getAxiom().accept(removeAxiomVisitor);
        }

        private void handleAxiomChange(OWLAxiomChange chg) {
            chg.getAxiom().signature()
                    .filter(AsOWLNamedIndividual::isOWLNamedIndividual)
                    .forEach(x -> checkIndividuals.add(x.asOWLNamedIndividual()));
        }

        private void handleAddClassAssertionAxiom(OWLClassAssertionAxiom ax) {
            if (ax.getClassExpression().isAnonymous()) {
                return;
            }
            OWLClass type = ax.getClassExpression().asOWLClass();
            classes.add(type);
            changedNodes.add(type);
        }

        private void handleRemoveClassAssertionAxiom(OWLClassAssertionAxiom ax) {
            if (ax.getClassExpression().isAnonymous()) {
                return;
            }
            OWLClass type = ax.getClassExpression().asOWLClass();
            if (!classes.contains(type)) {
                return;
            }
            if (!hasChildren(type)) {
                classes.remove(type);
            }
            changedNodes.add(type);
        }

        private boolean isTyped(OWLNamedIndividual ind) {
            for (OWLOntology ont : ontologies) {
                if (ont.classAssertionAxioms(ind).findFirst().isPresent()) {
                    return true;
                }
            }
            return false;
        }

        private boolean isReferenced(OWLNamedIndividual ind) {
            for (OWLOntology ont : ontologies){
                if (ont.containsIndividualInSignature(ind.getIRI())){
                    return true;
                }
            }
            return false;
        }
    }
}
