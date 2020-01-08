package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10/03/15
 */
public class OWLPropertyHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLEntity> {

    private HierarchyProvider<OWLObjectProperty> objectPropertyHierarchyProvider;

    private HierarchyProvider<OWLDataProperty> dataPropertyHierarchyProvider;

    private HierarchyProvider<OWLAnnotationProperty> annotationPropertyHierarchyProvider;

    public OWLPropertyHierarchyProvider(
            OWLOntologyManager owlOntologyManager,
            HierarchyProvider<OWLObjectProperty> objectPropertyHierarchyProvider,
            HierarchyProvider<OWLDataProperty> dataPropertyHierarchyProvider,
            HierarchyProvider<OWLAnnotationProperty> annotationPropertyHierarchyProvider
    ) {
        super(owlOntologyManager);
        this.annotationPropertyHierarchyProvider = annotationPropertyHierarchyProvider;
        this.dataPropertyHierarchyProvider = dataPropertyHierarchyProvider;
        this.objectPropertyHierarchyProvider = objectPropertyHierarchyProvider;

        annotationPropertyHierarchyProvider.addListener(new HierarchyProviderListener<OWLAnnotationProperty>() {
            @Override
            public void nodeChanged(OWLAnnotationProperty node) {
                OWLPropertyHierarchyProvider.this.fireNodeChanged(node);
            }

            @Override
            public void hierarchyChanged() {
                OWLPropertyHierarchyProvider.this.fireHierarchyChanged();
            }
        });
        dataPropertyHierarchyProvider.addListener(new HierarchyProviderListener<OWLDataProperty>() {
            @Override
            public void nodeChanged(OWLDataProperty node) {
                OWLPropertyHierarchyProvider.this.fireNodeChanged(node);
            }

            @Override
            public void hierarchyChanged() {
                OWLPropertyHierarchyProvider.this.fireHierarchyChanged();
            }
        });
        objectPropertyHierarchyProvider.addListener(new HierarchyProviderListener<OWLObjectProperty>() {
            @Override
            public void nodeChanged(OWLObjectProperty node) {
                OWLPropertyHierarchyProvider.this.fireNodeChanged(node);
            }

            @Override
            public void hierarchyChanged() {
                OWLPropertyHierarchyProvider.this.fireHierarchyChanged();
            }
        });
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean containsReference(OWLEntity object) {
        return object.accept(new OWLObjectVisitorEx<Boolean>() {
            @Override
            public Boolean visit(OWLAnnotationProperty property) {
                return annotationPropertyHierarchyProvider.containsReference(property);
            }

            @Override
            public Boolean visit(OWLDataProperty property) {
                return dataPropertyHierarchyProvider.containsReference(property);
            }

            @Override
            public Boolean visit(OWLObjectProperty property) {
                return objectPropertyHierarchyProvider.containsReference(property);
            }
        });
    }

    /**
     * Sets the ontologies that this hierarchy provider should use
     * in order to determine the hierarchy.
     *
     * @param ontologies a {@code Set} of {@link OWLOntology ontologies}
     */
    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        annotationPropertyHierarchyProvider.setOntologies(ontologies);
        dataPropertyHierarchyProvider.setOntologies(ontologies);
        objectPropertyHierarchyProvider.setOntologies(ontologies);
    }

    /**
     * Gets the objects that represent the roots of the hierarchy.
     */
    @Override
    public Set<OWLEntity> getRoots() {
        LinkedHashSet<OWLEntity> roots = new LinkedHashSet<>();
        roots.addAll(objectPropertyHierarchyProvider.getRoots());
        roots.addAll(dataPropertyHierarchyProvider.getRoots());
        roots.addAll(annotationPropertyHierarchyProvider.getRoots());
        return roots;
    }

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public Set<OWLEntity> getUnfilteredChildren(OWLEntity object) {
        Set<? extends OWLObject> result = object.accept(new OWLObjectVisitorEx<Set<? extends OWLEntity>>() {
            @Override
            public Set<? extends OWLEntity> visit(OWLAnnotationProperty property) {
                return annotationPropertyHierarchyProvider.getChildren(property);
            }

            @Override
            public Set<? extends OWLEntity> visit(OWLDataProperty property) {
                return dataPropertyHierarchyProvider.getChildren(property);
            }

            @Override
            public Set<? extends OWLEntity> visit(OWLObjectProperty property) {
                return objectPropertyHierarchyProvider.getChildren(property);
            }
        });
        return (Set<OWLEntity>)result;
    }

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public Set<OWLEntity> getParents(OWLEntity object) {
        Set<? extends OWLEntity> result = object.accept(new OWLObjectVisitorEx<Set<? extends OWLEntity>>() {
            @Override
            public Set<? extends OWLEntity> visit(OWLAnnotationProperty property) {
                return annotationPropertyHierarchyProvider.getParents(property);
            }

            @Override
            public Set<? extends OWLEntity> visit(OWLDataProperty property) {
                return dataPropertyHierarchyProvider.getParents(property);
            }

            @Override
            public Set<? extends OWLEntity> visit(OWLObjectProperty property) {
                return objectPropertyHierarchyProvider.getParents(property);
            }
        });
        return (Set<OWLEntity>)result;
    }

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public Set<OWLEntity> getEquivalents(OWLEntity object) {
        Set<? extends OWLEntity> result = object.accept(new OWLObjectVisitorEx<Set<? extends OWLEntity>>() {
            @Override
            public Set<? extends OWLEntity> visit(OWLAnnotationProperty property) {
                return annotationPropertyHierarchyProvider.getEquivalents(property);
            }

            @Override
            public Set<? extends OWLEntity> visit(OWLDataProperty property) {
                return dataPropertyHierarchyProvider.getEquivalents(property);
            }

            @Override
            public Set<? extends OWLEntity> visit(OWLObjectProperty property) {
                return objectPropertyHierarchyProvider.getEquivalents(property);
            }
        });
        return (Set<OWLEntity>)result;
    }
}
