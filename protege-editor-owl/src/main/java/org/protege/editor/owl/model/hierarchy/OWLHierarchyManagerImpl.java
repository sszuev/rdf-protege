package org.protege.editor.owl.model.hierarchy;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.hierarchy.cls.InferredOWLClassHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.property.InferredObjectPropertyHierarchyProvider;
import org.protege.editor.owl.ui.view.rdf.RDFHierarchyProvider;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Objects;
import java.util.Set;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 27, 2008<br><br>
 */
public class OWLHierarchyManagerImpl implements OWLHierarchyManager {

    private OWLHierarchyProvider<OWLClass> assertedClassHierarchyProvider;
    private InferredOWLClassHierarchyProvider inferredClassHierarchyProvider;
    private OWLHierarchyProvider<OWLObjectProperty> assertedObjectPropertyHierarchyProvider;
    private OWLHierarchyProvider<OWLObjectProperty> inferredObjectPropertyHierarchyProvider;
    private OWLHierarchyProvider<OWLDataProperty> assertedDataPropertyHierarchyProvider;
    private OWLAnnotationPropertyHierarchyProvider assertedAnnotationPropertyHierarchyProvider;
    private IndividualsByTypeHierarchyProvider individualsByTypeHierarchyProvider;
    private RDFHierarchyProvider tripleHierarchyProvider;

    private final OWLModelManager manager;

    private final OWLModelManagerListener listener = event -> {
        if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || event.isType(EventType.ONTOLOGY_RELOADED)) {
            rebuildAsNecessary();
        }
    };

    public OWLHierarchyManagerImpl(OWLModelManager manager) {
        this.manager = Objects.requireNonNull(manager);
        manager.addListener(listener);
    }

    @Override
    public OWLHierarchyProvider<OWLClass> getOWLClassHierarchyProvider() {
        if (assertedClassHierarchyProvider != null) {
            return assertedClassHierarchyProvider;
        }
        AssertedClassHierarchyProvider res = new AssertedClassHierarchyProvider(manager.getOWLOntologyManager());
        res.setOntologies(manager.getActiveOntologies());
        return this.assertedClassHierarchyProvider = res;
    }

    @Override
    public OWLHierarchyProvider<OWLClass> getInferredOWLClassHierarchyProvider() {
        if (inferredClassHierarchyProvider != null) {
            return inferredClassHierarchyProvider;
        }
        return inferredClassHierarchyProvider = new InferredOWLClassHierarchyProvider(manager, manager.getOWLOntologyManager());
    }

    @Override
    public OWLHierarchyProvider<OWLObjectProperty> getOWLObjectPropertyHierarchyProvider() {
        if (assertedObjectPropertyHierarchyProvider != null) {
            return assertedObjectPropertyHierarchyProvider;
        }
        OWLObjectPropertyHierarchyProvider res = new OWLObjectPropertyHierarchyProvider(manager.getOWLOntologyManager());
        res.setOntologies(manager.getActiveOntologies());
        return this.assertedObjectPropertyHierarchyProvider = res;
    }

    @Override
    public OWLHierarchyProvider<OWLDataProperty> getOWLDataPropertyHierarchyProvider() {
        if (assertedDataPropertyHierarchyProvider != null) {
            return assertedDataPropertyHierarchyProvider;
        }
        OWLDataPropertyHierarchyProvider res = new OWLDataPropertyHierarchyProvider(manager.getOWLOntologyManager());
        res.setOntologies(manager.getActiveOntologies());
        return this.assertedDataPropertyHierarchyProvider = res;
    }

    @Override
    public OWLAnnotationPropertyHierarchyProvider getOWLAnnotationPropertyHierarchyProvider() {
        if (assertedAnnotationPropertyHierarchyProvider != null) {
            return assertedAnnotationPropertyHierarchyProvider;
        }
        OWLAnnotationPropertyHierarchyProvider res = new OWLAnnotationPropertyHierarchyProvider(manager.getOWLOntologyManager());
        res.setOntologies(manager.getOntologies());
        return this.assertedAnnotationPropertyHierarchyProvider = res;
    }

    @Override
    public IndividualsByTypeHierarchyProvider getOWLIndividualsByTypeHierarchyProvider() {
        if (individualsByTypeHierarchyProvider != null) {
            return individualsByTypeHierarchyProvider;
        }
        IndividualsByTypeHierarchyProvider res = new IndividualsByTypeHierarchyProvider(manager.getOWLOntologyManager());
        res.setOntologies(manager.getActiveOntologies());
        return this.individualsByTypeHierarchyProvider = res;
    }

    @Override
    public OWLHierarchyProvider<OWLObjectProperty> getInferredOWLObjectPropertyHierarchyProvider() {
        if (inferredObjectPropertyHierarchyProvider != null) {
            return inferredObjectPropertyHierarchyProvider;
        }
        InferredObjectPropertyHierarchyProvider res = new InferredObjectPropertyHierarchyProvider(manager);
        res.setOntologies(manager.getActiveOntologies());
        return this.inferredObjectPropertyHierarchyProvider = res;
    }

    @Override
    public void dispose() throws Exception {
        manager.removeListener(listener);

        if (assertedClassHierarchyProvider != null) {
            assertedClassHierarchyProvider.dispose();
        }
        if (inferredClassHierarchyProvider != null) {
            inferredClassHierarchyProvider.dispose();
        }
        if (assertedObjectPropertyHierarchyProvider != null) {
            assertedObjectPropertyHierarchyProvider.dispose();
        }
        if (inferredObjectPropertyHierarchyProvider != null) {
            inferredObjectPropertyHierarchyProvider.dispose();
        }
        if (assertedDataPropertyHierarchyProvider != null) {
            assertedDataPropertyHierarchyProvider.dispose();
        }
        if (individualsByTypeHierarchyProvider != null) {
            individualsByTypeHierarchyProvider.dispose();
        }
        if (tripleHierarchyProvider != null) {
            tripleHierarchyProvider.dispose();
        }
    }

    private void rebuildAsNecessary() {
        Set<OWLOntology> ontologies = manager.getActiveOntologies();
        // Rebuild the various hierarchies
        if (assertedClassHierarchyProvider != null) {
            getOWLClassHierarchyProvider().setOntologies(ontologies);
        }
        if (assertedObjectPropertyHierarchyProvider != null) {
            getOWLObjectPropertyHierarchyProvider().setOntologies(ontologies);
        }
        if (assertedDataPropertyHierarchyProvider != null) {
            getOWLDataPropertyHierarchyProvider().setOntologies(ontologies);
        }
        if (individualsByTypeHierarchyProvider != null) {
            getOWLIndividualsByTypeHierarchyProvider().setOntologies(ontologies);
        }
        if (assertedAnnotationPropertyHierarchyProvider != null) {
            getOWLAnnotationPropertyHierarchyProvider().setOntologies(ontologies);
        }
        if (tripleHierarchyProvider != null) {
            tripleHierarchyProvider.setOntology(manager.getActiveOntology());
        }
    }

    @Override
    public RDFHierarchyProvider getRDFTripleHierarchyProvider() {
        if (tripleHierarchyProvider != null) return tripleHierarchyProvider;
        RDFHierarchyProvider res = new RDFHierarchyProvider();
        res.setOntology(manager.getActiveOntology());
        return tripleHierarchyProvider = res;
    }
}
