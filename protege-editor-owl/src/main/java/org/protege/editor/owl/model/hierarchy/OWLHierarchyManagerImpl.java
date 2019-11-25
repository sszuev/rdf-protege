package org.protege.editor.owl.model.hierarchy;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.hierarchy.cls.InferredOWLClassHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.property.InferredObjectPropertyHierarchyProvider;
import org.protege.editor.owl.ui.view.rdf.TripleHierarchyProvider;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>

 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 27, 2008<br><br>
 */
public class OWLHierarchyManagerImpl implements OWLHierarchyManager {

    private OWLObjectHierarchyProvider<OWLClass> assertedClassHierarchyProvider;
    private InferredOWLClassHierarchyProvider inferredClassHierarchyProvider;
    private OWLObjectHierarchyProvider<OWLObjectProperty> assertedObjectPropertyHierarchyProvider;
    private OWLObjectHierarchyProvider<OWLObjectProperty> inferredObjectPropertyHierarchyProvider;
    private OWLObjectHierarchyProvider<OWLDataProperty> assertedDataPropertyHierarchyProvider;
    private OWLAnnotationPropertyHierarchyProvider assertedAnnotationPropertyHierarchyProvider;
    private IndividualsByTypeHierarchyProvider individualsByTypeHierarchyProvider;
    private TripleHierarchyProvider tripleHierarchyProvider;

    private OWLModelManager mngr;

    private OWLModelManagerListener listener = event -> {
        if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || event.isType(EventType.ONTOLOGY_RELOADED)) {
            rebuildAsNecessary();
        }
    };

    public OWLHierarchyManagerImpl(OWLModelManager mngr) {
        this.mngr = mngr;
        mngr.addListener(listener);
    }

    @Override
    public OWLObjectHierarchyProvider<OWLClass> getOWLClassHierarchyProvider() {
        if (assertedClassHierarchyProvider == null) {
            assertedClassHierarchyProvider = new AssertedClassHierarchyProvider(mngr.getOWLOntologyManager());
            assertedClassHierarchyProvider.setOntologies(mngr.getActiveOntologies());
        }
        return assertedClassHierarchyProvider;
    }

    @Override
    public OWLObjectHierarchyProvider<OWLClass> getInferredOWLClassHierarchyProvider() {
        if (inferredClassHierarchyProvider == null) {
            inferredClassHierarchyProvider = new InferredOWLClassHierarchyProvider(mngr, mngr.getOWLOntologyManager());
        }
        return inferredClassHierarchyProvider;
    }

    @Override
    public OWLObjectHierarchyProvider<OWLObjectProperty> getOWLObjectPropertyHierarchyProvider() {
        if (assertedObjectPropertyHierarchyProvider == null) {
            assertedObjectPropertyHierarchyProvider = new OWLObjectPropertyHierarchyProvider(mngr.getOWLOntologyManager());
            assertedObjectPropertyHierarchyProvider.setOntologies(mngr.getActiveOntologies());
        }
        return assertedObjectPropertyHierarchyProvider;
    }

    @Override
    public OWLObjectHierarchyProvider<OWLDataProperty> getOWLDataPropertyHierarchyProvider() {
        if (assertedDataPropertyHierarchyProvider == null) {
            assertedDataPropertyHierarchyProvider = new OWLDataPropertyHierarchyProvider(mngr.getOWLOntologyManager());
            assertedDataPropertyHierarchyProvider.setOntologies(mngr.getActiveOntologies());
        }
        return assertedDataPropertyHierarchyProvider;
    }

    @Override
    public OWLAnnotationPropertyHierarchyProvider getOWLAnnotationPropertyHierarchyProvider() {
        if (assertedAnnotationPropertyHierarchyProvider == null) {
            assertedAnnotationPropertyHierarchyProvider = new OWLAnnotationPropertyHierarchyProvider(mngr.getOWLOntologyManager());
            assertedAnnotationPropertyHierarchyProvider.setOntologies(mngr.getOntologies());
        }
        return assertedAnnotationPropertyHierarchyProvider;
    }

    @Override
    public IndividualsByTypeHierarchyProvider getOWLIndividualsByTypeHierarchyProvider() {
        if (individualsByTypeHierarchyProvider == null) {
            individualsByTypeHierarchyProvider = new IndividualsByTypeHierarchyProvider(mngr.getOWLOntologyManager());
            individualsByTypeHierarchyProvider.setOntologies(mngr.getActiveOntologies());
        }
        return individualsByTypeHierarchyProvider;
    }

    @Override
    public OWLObjectHierarchyProvider<OWLObjectProperty> getInferredOWLObjectPropertyHierarchyProvider() {
        if (inferredObjectPropertyHierarchyProvider == null) {
            inferredObjectPropertyHierarchyProvider = new InferredObjectPropertyHierarchyProvider(mngr);
            inferredObjectPropertyHierarchyProvider.setOntologies(mngr.getActiveOntologies());
        }
        return inferredObjectPropertyHierarchyProvider;
    }

    @Override
    public void dispose() throws Exception {
        mngr.removeListener(listener);

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
        Set<OWLOntology> ontologies = mngr.getActiveOntologies();
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
            tripleHierarchyProvider.setOntology(mngr.getActiveOntology());
        }
    }

    @Override
    public TripleHierarchyProvider getRDFTripleHierarchyProvider() {
        if (tripleHierarchyProvider != null) return tripleHierarchyProvider;
        TripleHierarchyProvider res = new TripleHierarchyProvider();
        res.setOntology(mngr.getActiveOntology());
        return tripleHierarchyProvider = res;
    }
}
