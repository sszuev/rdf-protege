package org.protege.editor.owl.ui.usage;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 21-Feb-2007<br><br>
 */
public class UsageByEntityTreeModel extends DefaultTreeModel implements UsageTreeModel {

    private static final long serialVersionUID = -2530774548488512609L;

    private final OWLModelManager owlModelManager;
    private final AxiomSorter axiomSorter;
    private final Map<OWLEntity, DefaultMutableTreeNode> nodeMap;
    private final Map<OWLEntity, Set<OWLAxiom>> axiomsByEntityMap;

    // axioms that cannot be indexed by entity
    private final Set<OWLAxiom> additionalAxioms = new HashSet<>();
    private final Set<UsageFilter> filters = new HashSet<>();

    private int usageCount;
    private OWLEntity entity;
    private DefaultMutableTreeNode rootNode;

    public UsageByEntityTreeModel(OWLEditorKit owlEditorKit) {
        super(new DefaultMutableTreeNode("No usage"));
        owlModelManager = owlEditorKit.getModelManager();
        axiomSorter = new AxiomSorter();
        nodeMap = new HashMap<>();
        axiomsByEntityMap = new TreeMap<>(owlModelManager.getOWLObjectComparator());
    }

    private String getRootContent(OWLModelManager mngr, OWLEntity entity) {
        return entity != null ? "Found " + usageCount + " uses of " + mngr.getRendering(entity) : "";
    }

    @Override
    public void setOWLEntity(OWLEntity owlEntity) {
        if (owlEntity == null) {
            return;
        }
        this.entity = owlEntity;
        axiomsByEntityMap.clear();
        usageCount = 0;

        for (OWLOntology ont : owlModelManager.getActiveOntologies()) {
            ont.referencingAxioms(owlEntity.getIRI()).forEach(this::addUsage);
            // This is terribly inefficient but there are no indexes in the OWL API to do this.
            ont.axioms(AxiomType.ANNOTATION_ASSERTION).forEach(ax -> {
                Optional<IRI> valueIRI = ax.getValue().asIRI();
                if (valueIRI.filter(x -> x.equals(owlEntity.getIRI())).isPresent()) {
                    addUsage(ax);
                }
            });
        }

        rootNode = new DefaultMutableTreeNode(getRootContent(owlModelManager, entity));
        setRoot(rootNode);

        for (OWLEntity ent : axiomsByEntityMap.keySet()) {
            for (OWLAxiom ax : axiomsByEntityMap.get(ent)) {
                getNode(ent).add(new UsageTreeNode(null, ax));
            }
        }
        if (!additionalAxioms.isEmpty()) {
            DefaultMutableTreeNode otherNode = new DefaultMutableTreeNode("Other");
            rootNode.add(otherNode);
            for (OWLAxiom ax : additionalAxioms) {
                otherNode.add(new DefaultMutableTreeNode(ax));
            }
        }
    }

    private void addUsage(OWLAxiom ax) {
        axiomSorter.setAxiom(ax);
        ax.accept(axiomSorter);
    }

    @Override
    public void addFilter(UsageFilter filter) {
        filters.add(filter);
    }

    @Override
    public void addFilters(Set<UsageFilter> filters) {
        this.filters.addAll(filters);
    }

    @Override
    public void removeFilter(UsageFilter filter) {
        filters.remove(filter);
    }

    private boolean isFilterSet(UsageFilter filter) {
        return filters.contains(filter);
    }

    private DefaultMutableTreeNode getNode(OWLEntity entity) {
        DefaultMutableTreeNode node = nodeMap.get(entity);
        if (node == null) {
            node = new DefaultMutableTreeNode(entity);
            nodeMap.put(entity, node);
            rootNode.add(node);
        }
        return node;
    }

    @Override
    public void refresh() {
        setOWLEntity(entity);
    }

    @SuppressWarnings("NullableProblems")
    private class AxiomSorter implements OWLAxiomVisitor, OWLEntityVisitor, OWLPropertyExpressionVisitor {
        private OWLAxiom currentAxiom;

        public void setAxiom(OWLAxiom axiom) {
            currentAxiom = axiom;
        }

        private void add(OWLEntity ent) {
            if (isFilterSet(UsageFilter.filterSelf) && entity.equals(ent)) {
                return;
            }
            usageCount++;
            axiomsByEntityMap.computeIfAbsent(ent, k -> new HashSet<>()).add(currentAxiom);
        }

        @Override
        public void visit(OWLClass cls) {
            add(cls);
        }

        @Override
        public void visit(OWLDatatype dataType) {
            add(dataType);
        }

        @Override
        public void visit(OWLNamedIndividual individual) {
            add(individual);
        }

        @Override
        public void visit(OWLDataProperty property) {
            add(property);
        }

        @Override
        public void visit(OWLObjectProperty property) {
            add(property);
        }

        @Override
        public void visit(OWLAnnotationProperty property) {
            add(property);
        }

        @Override
        public void visit(OWLObjectInverseOf property) {
            property.getInverse().accept(this);
        }

        @Override
        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLAnnotationAssertionAxiom axiom) {
            if (!(axiom.getSubject() instanceof IRI)) {
                return;
            }
            IRI subjectIRI = (IRI) axiom.getSubject();
            for (OWLOntology ont : owlModelManager.getActiveOntologies()) {
                if (ont.containsClassInSignature(subjectIRI)) {
                    add(owlModelManager.getOWLDataFactory().getOWLClass(subjectIRI));
                }
                if (ont.containsObjectPropertyInSignature(subjectIRI)) {
                    add(owlModelManager.getOWLDataFactory().getOWLObjectProperty(subjectIRI));
                }
                if (ont.containsDataPropertyInSignature(subjectIRI)) {
                    add(owlModelManager.getOWLDataFactory().getOWLDataProperty(subjectIRI));
                }
                if (ont.containsIndividualInSignature(subjectIRI)) {
                    add(owlModelManager.getOWLDataFactory().getOWLNamedIndividual(subjectIRI));
                }
                if (ont.containsAnnotationPropertyInSignature(subjectIRI)) {
                    add(owlModelManager.getOWLDataFactory().getOWLAnnotationProperty(subjectIRI));
                }
                if (ont.containsDatatypeInSignature(subjectIRI)) {
                    add(owlModelManager.getOWLDataFactory().getOWLDatatype(subjectIRI));
                }
            }
        }

        @Override
        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
            ((OWLEntity) axiom.getSubProperty()).accept(this);
        }

        @Override
        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
            ((OWLEntity) axiom.getProperty()).accept(this);
        }

        @Override
        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
            ((OWLEntity) axiom.getProperty()).accept(this);
        }

        @Override
        public void visit(OWLClassAssertionAxiom axiom) {
            if (!axiom.getIndividual().isAnonymous()) {
                axiom.getIndividual().asOWLNamedIndividual().accept(this);
            }
        }

        @Override
        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()) {
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }

        @Override
        public void visit(OWLDataPropertyDomainAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLDataPropertyRangeAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
        }

        @Override
        public void visit(OWLDeclarationAxiom axiom) {
            axiom.getEntity().accept(this);
        }

        @Override
        public void visit(OWLDifferentIndividualsAxiom axiom) {
            axiom.individuals().filter(i -> !i.isAnonymous()).forEach(i -> i.asOWLNamedIndividual().accept(this));
        }

        @Override
        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            if (!isFilterSet(UsageFilter.filterDisjoints)) {
                axiom.properties().forEach(prop -> prop.accept(this));
            }
        }

        @Override
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            if (!isFilterSet(UsageFilter.filterDisjoints)) {
                axiom.properties().forEach(prop -> prop.accept(this));
            }
        }

        @Override
        public void visit(OWLDisjointUnionAxiom axiom) {
            if (!isFilterSet(UsageFilter.filterDisjoints)) {
                axiom.getOWLClass().accept(this);
            }
        }

        @Override
        public void visit(OWLDisjointClassesAxiom axiom) {
            if (!isFilterSet(UsageFilter.filterDisjoints) && axiom.classExpressions()
                    .filter(x -> !x.isAnonymous())
                    .map(AsOWLClass::asOWLClass)
                    .peek(x -> x.accept(this)).count() > 0) {
                return;
            }
            additionalAxioms.add(axiom);
            usageCount++;
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            if (axiom.classExpressions()
                    .filter(x -> !x.isAnonymous())
                    .map(AsOWLClass::asOWLClass)
                    .peek(x -> x.accept(this)).count() > 0) {
                return;
            }
            additionalAxioms.add(axiom);
            usageCount++;
        }

        @Override
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            axiom.properties().forEach(prop -> prop.accept(this));
        }

        @Override
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            axiom.properties().forEach(prop -> prop.accept(this));
        }

        @Override
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            axiom.properties().forEach(prop -> prop.accept(this));
        }

        @Override
        public void visit(OWLHasKeyAxiom axiom) {
            //@@TODO implement
        }

        @Override
        public void visit(OWLDatatypeDefinitionAxiom axiom) {
            axiom.getDatatype().accept(this);
        }

        @Override
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()) {
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }

        @Override
        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()) {
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }

        @Override
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()) {
                axiom.getSubject().asOWLNamedIndividual().accept(this);
            }
        }

        @Override
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            axiom.getSuperProperty().accept(this);
        }

        @Override
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
        }

        @Override
        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLSameIndividualAxiom axiom) {
            axiom.individuals().filter(i -> !i.isAnonymous()).forEach(ind -> ind.asOWLNamedIndividual().accept(this));
        }

        @Override
        public void visit(OWLSubClassOfAxiom axiom) {
            if (!axiom.getSubClass().isAnonymous()) {
                if (!isFilterSet(UsageFilter.filterNamedSubsSupers) ||
                        (!axiom.getSubClass().equals(entity) && !axiom.getSuperClass().equals(entity))) {
                    axiom.getSubClass().asOWLClass().accept(this);
                }
            } else {
                additionalAxioms.add(axiom);
                usageCount++;
            }
        }

        @Override
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(SWRLRule rule) {
        }
    }


    protected static class UsageTreeNode extends DefaultMutableTreeNode {

        private final OWLOntology ont;
        private final OWLAxiom axiom;

        public UsageTreeNode(OWLOntology ont, OWLAxiom axiom) {
            super(axiom);
            this.ont = ont;
            this.axiom = axiom;
        }

        public OWLAxiom getAxiom() {
            return axiom;
        }

        public OWLOntology getOntology() {
            return ont;
        }
    }
}
