package org.protege.editor.owl.model.util;

import com.google.common.base.Stopwatch;
import org.protege.editor.core.log.LogBanner;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 08/03/15
 */
public class OWLEntityDeleter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OWLEntityDeleter.class);

    public static void deleteEntities(Collection<? extends OWLEntity> entities, OWLModelManager manager) {
        LOGGER.info(LogBanner.start("Deleting entities"));
        LOGGER.info("Generating changes to remove {} entities", entities.size());
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<OWLOntologyChange> allChanges = getChangesToDeleteEntities(entities, manager);
        LOGGER.info("Generated {} changes to remove {} entities in {} ms",
                allChanges.size(), entities.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        manager.applyChanges(allChanges);
        LOGGER.info("Applied {} changes in {}", allChanges.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        LOGGER.info(LogBanner.end());
    }

    private static List<OWLOntologyChange> getChangesToDeleteEntities(Collection<? extends OWLEntity> entities,
                                                                      OWLModelManager manager) {
        List<OWLOntologyChange> allChanges = new ArrayList<>();
        for (OWLOntology ontology : manager.getOntologies()) {
            List<OWLOntologyChange> changeList = getChangesForOntology(entities, ontology);
            allChanges.addAll(changeList);
        }
        return allChanges;
    }

    private static List<OWLOntologyChange> getChangesForOntology(Collection<? extends OWLEntity> entities,
                                                                 OWLOntology ontology) {
        ReferenceFinder referenceFinder = new ReferenceFinder();
        ReferenceFinder.ReferenceSet referenceSet = referenceFinder.getReferenceSet(entities, ontology);
        List<OWLOntologyChange> changeList = new ArrayList<>(
                referenceSet.getReferencingAxioms().size() + referenceSet.getReferencingOntologyAnnotations().size()
        );
        for (OWLAxiom ax : referenceSet.getReferencingAxioms()) {
            changeList.add(new RemoveAxiom(referenceSet.getOntology(), ax));
        }
        for (OWLAnnotation annotation : referenceSet.getReferencingOntologyAnnotations()) {
            changeList.add(new RemoveOntologyAnnotation(referenceSet.getOntology(), annotation));
        }
        return changeList;
    }
}
