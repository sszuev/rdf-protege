package org.protege.editor.owl.model.cache;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.DublinCoreVocabulary;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 21-Sep-2006<br><br>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLEntityRenderingCacheImpl implements OWLEntityRenderingCache {

    private final Map<String, OWLClass> owlClassMap = new HashMap<>();
    private final Map<String, OWLObjectProperty> owlObjectPropertyMap = new HashMap<>();
    private final Map<String, OWLDataProperty> owlDataPropertyMap = new HashMap<>();
    private final Map<String, OWLAnnotationProperty> owlAnnotationPropertyMap = new HashMap<>();
    private final Map<String, OWLNamedIndividual> owlIndividualMap = new HashMap<>();
    private final Map<String, OWLDatatype> owlDatatypeMap = new HashMap<>();
    private final Map<OWLEntity, String> entityRenderingMap = new HashMap<>();
    private final OWLOntologyChangeListener listener = this::processChanges;

    private OWLModelManager manager;

    @Override
    public void setOWLModelManager(OWLModelManager owlModelManager) {
        this.manager = owlModelManager;
        owlModelManager.addOntologyChangeListener(listener);
    }

    private void processChanges(List<? extends OWLOntologyChange> changes) {
        changes.stream()
                .filter(x -> x instanceof OWLAxiomChange)
                .map(x -> (OWLAxiomChange) x)
                .flatMap(OWLAxiomChange::signature)
                .forEach(this::updateRendering);
    }

    @Override
    public void rebuild() {
        clear();
        manager.getOWLEntityRenderer();
        OWLDataFactory factory = manager.getOWLDataFactory();

        addRendering(factory.getOWLThing(), owlClassMap);
        addRendering(factory.getOWLNothing(), owlClassMap);
        addRendering(factory.getOWLTopObjectProperty(), owlObjectPropertyMap);
        addRendering(factory.getOWLBottomObjectProperty(), owlObjectPropertyMap);
        addRendering(factory.getOWLTopDataProperty(), owlDataPropertyMap);
        addRendering(factory.getOWLBottomDataProperty(), owlDataPropertyMap);

        for (OWLOntology ont : manager.getOntologies()) {
            ont.classesInSignature().forEach(c -> addRendering(c, owlClassMap));
            ont.objectPropertiesInSignature().forEach(p -> addRendering(p, owlObjectPropertyMap));
            ont.dataPropertiesInSignature().forEach(p -> addRendering(p, owlDataPropertyMap));
            ont.individualsInSignature().forEach(i -> addRendering(i.asOWLNamedIndividual(), owlIndividualMap));
            ont.annotationPropertiesInSignature().forEach(p -> addRendering(p, owlAnnotationPropertyMap));
        }

        // standard annotation properties        
        for (IRI uri : OWLRDFVocabulary.BUILT_IN_AP_IRIS) {
            addRendering(factory.getOWLAnnotationProperty(uri), owlAnnotationPropertyMap);
        }

        // Dublin Core
        for (DublinCoreVocabulary vocabulary : DublinCoreVocabulary.values()) {
            addRendering(factory.getOWLAnnotationProperty(vocabulary.getIRI()), owlAnnotationPropertyMap);
        }

        // datatypes
        final OWLDataTypeUtils datatypeUtils = new OWLDataTypeUtils(manager.getOWLOntologyManager());
        datatypeUtils.knownDatatypes(manager.getActiveOntologies()).forEach(dt -> addRendering(dt, owlDatatypeMap));
    }

    @Override
    public void dispose() {
        clear();
        manager.removeOntologyChangeListener(listener);
    }

    private void clear() {
        owlClassMap.clear();
        owlObjectPropertyMap.clear();
        owlDataPropertyMap.clear();
        owlAnnotationPropertyMap.clear();
        owlIndividualMap.clear();
        owlDatatypeMap.clear();
        entityRenderingMap.clear();
    }

    @Override
    public OWLClass getOWLClass(String rendering) {
        return owlClassMap.get(rendering);
    }

    @Override
    public OWLObjectProperty getOWLObjectProperty(String rendering) {
        return owlObjectPropertyMap.get(rendering);
    }

    @Override
    public OWLDataProperty getOWLDataProperty(String rendering) {
        return owlDataPropertyMap.get(rendering);
    }

    @Override
    public OWLAnnotationProperty getOWLAnnotationProperty(String rendering) {
        return owlAnnotationPropertyMap.get(rendering);
    }

    @Override
    public OWLNamedIndividual getOWLIndividual(String rendering) {
        return owlIndividualMap.get(rendering);
    }

    @Override
    public OWLDatatype getOWLDatatype(String rendering) {
        return owlDatatypeMap.get(rendering);
    }

    @Override
    public String getRendering(OWLEntity owlEntity) {
        return entityRenderingMap.get(owlEntity);
    }

    @Override
    public OWLEntity getOWLEntity(String rendering) {
        // Examine in the order of class, property, individual
        OWLEntity entity = getOWLClass(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLObjectProperty(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLDataProperty(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLIndividual(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLDatatype(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLAnnotationProperty(rendering);
        return entity;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void addRendering(OWLEntity owlEntity) {
        owlEntity.accept(new OWLEntityVisitor() {
            @Override
            public void visit(OWLDataProperty entity) {
                addRendering(entity, owlDataPropertyMap);
            }

            @Override
            public void visit(OWLObjectProperty entity) {
                addRendering(entity, owlObjectPropertyMap);
            }

            @Override
            public void visit(OWLAnnotationProperty owlAnnotationProperty) {
                addRendering(owlAnnotationProperty, owlAnnotationPropertyMap);
            }

            @Override
            public void visit(OWLNamedIndividual entity) {
                addRendering(entity, owlIndividualMap);
            }

            @Override
            public void visit(OWLClass entity) {
                addRendering(entity, owlClassMap);
            }

            @Override
            public void visit(OWLDatatype entity) {
                addRendering(entity, owlDatatypeMap);
            }
        });
    }

    private <T extends OWLEntity> void addRendering(T entity, Map<String, T> map) {
        if (!entityRenderingMap.containsKey(entity)) {
            String rendering = manager.getRendering(entity);
            map.put(rendering, entity);
            entityRenderingMap.put(entity, rendering);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void removeRendering(OWLEntity owlEntity) {
        final String oldRendering = entityRenderingMap.get(owlEntity);
        entityRenderingMap.remove(owlEntity);
        owlEntity.accept(new OWLEntityVisitor() {
            @Override
            public void visit(OWLClass entity) {
                owlClassMap.remove(oldRendering);
            }

            @Override
            public void visit(OWLDataProperty entity) {
                owlDataPropertyMap.remove(oldRendering);
            }

            @Override
            public void visit(OWLObjectProperty entity) {
                owlObjectPropertyMap.remove(oldRendering);
            }

            @Override
            public void visit(OWLAnnotationProperty owlAnnotationProperty) {
                owlAnnotationPropertyMap.remove(oldRendering);
            }

            @Override
            public void visit(OWLNamedIndividual entity) {
                owlIndividualMap.remove(oldRendering);
            }

            @Override
            public void visit(OWLDatatype entity) {
                owlDatatypeMap.remove(oldRendering);
            }
        });
    }

    @Override
    public void updateRendering(final OWLEntity ent) {
        boolean updateRendering = false;
        for (OWLOntology ont : manager.getActiveOntologies()) {
            if (ont.containsEntityInSignature(ent)) {
                updateRendering = true;
                break;
            }
        }
        removeRendering(ent); // always remove the old rendering
        if (updateRendering) {
            addRendering(ent);
        }
    }

    @Override
    public Set<String> getOWLClassRenderings() {
        return owlClassMap.keySet();
    }

    @Override
    public Set<String> getOWLObjectPropertyRenderings() {
        return owlObjectPropertyMap.keySet();
    }

    @Override
    public Set<String> getOWLDataPropertyRenderings() {
        return owlDataPropertyMap.keySet();
    }

    @Override
    public Set<String> getOWLAnnotationPropertyRenderings() {
        return owlAnnotationPropertyMap.keySet();
    }

    @Override
    public Set<String> getOWLIndividualRenderings() {
        return owlIndividualMap.keySet();
    }

    @Override
    public Set<String> getOWLDatatypeRenderings() {
        return owlDatatypeMap.keySet();
    }

    @Override
    public Set<String> getOWLEntityRenderings() {
        Set<String> renderings = new HashSet<>(owlClassMap.size() +
                                                     owlObjectPropertyMap.size() +
                                                     owlDataPropertyMap.size() +
                                                     owlAnnotationPropertyMap.size() +
                                                     owlIndividualMap.size() +
                                                     owlDatatypeMap.size());
        renderings.addAll(owlClassMap.keySet());
        renderings.addAll(owlObjectPropertyMap.keySet());
        renderings.addAll(owlDataPropertyMap.keySet());
        renderings.addAll(owlAnnotationPropertyMap.keySet());
        renderings.addAll(owlIndividualMap.keySet());
        renderings.addAll(owlDatatypeMap.keySet());
        return renderings;
    }
}
