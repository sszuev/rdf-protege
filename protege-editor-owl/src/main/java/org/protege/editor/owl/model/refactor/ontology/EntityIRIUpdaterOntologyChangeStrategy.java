package org.protege.editor.owl.model.refactor.ontology;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.util.*;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 26/02/2012
 */
public class EntityIRIUpdaterOntologyChangeStrategy implements OntologyIDChangeStrategy {

    public List<OWLOntologyChange> getChangesForRename(OWLOntology ontology, OWLOntologyID from, OWLOntologyID to) {
        if (isNotEntityRenamingChange(from, to)) {
            return Collections.emptyList();
        }
        Map<OWLEntity, IRI> renameMap = new HashMap<>();
        getRenameMap(ontology, from, to, renameMap, Long.MAX_VALUE);
        OWLEntityRenamer renamer = new OWLEntityRenamer(ontology.getOWLOntologyManager(), Collections.singleton(ontology));
        return renamer.changeIRI(renameMap);
    }

    private boolean isNotEntityRenamingChange(OWLOntologyID from, OWLOntologyID to) {
        return from.isAnonymous() || to.isAnonymous() || from.equals(to);
    }

    @SuppressWarnings("SameParameterValue")
    private void getRenameMap(OWLOntology ontology,
                              OWLOntologyID fromId,
                              OWLOntologyID toId,
                              Map<OWLEntity, IRI> renameMap,
                              long limit) {
        if (isNotEntityRenamingChange(fromId, toId)) {
            return;
        }
        String fromBase = fromId.getOntologyIRI().map(IRI::getIRIString).orElseThrow(IllegalArgumentException::new);
        String toBase = toId.getOntologyIRI().map(IRI::getIRIString).orElseThrow(IllegalArgumentException::new);
        getEntitiesRenamings(ontology.objectPropertiesInSignature(), fromBase, toBase, renameMap, limit);
        if (renameMap.size() >= limit) {
            return;
        }
        getEntitiesRenamings(ontology.dataPropertiesInSignature(), fromBase, toBase, renameMap, limit);
        if (renameMap.size() >= limit) {
            return;
        }
        getEntitiesRenamings(ontology.annotationPropertiesInSignature(), fromBase, toBase, renameMap, limit);
        if (renameMap.size() >= limit) {
            return;
        }
        getEntitiesRenamings(ontology.classesInSignature(), fromBase, toBase, renameMap, limit);
        if (renameMap.size() >= limit) {
            return;
        }
        getEntitiesRenamings(ontology.individualsInSignature(), fromBase, toBase, renameMap, limit);
        if (renameMap.size() >= limit) {
            return;
        }
        getEntitiesRenamings(ontology.datatypesInSignature(), fromBase, toBase, renameMap, limit);
    }

    public Set<OWLEntity> getEntitiesToRename(OWLOntology ontology, OWLOntologyID from, OWLOntologyID to) {
        if (isNotEntityRenamingChange(from, to)) {
            return Collections.emptySet();
        }
        Map<OWLEntity, IRI> renameMap = new HashMap<>();
        getRenameMap(ontology, from, to, renameMap, Long.MAX_VALUE);
        return renameMap.keySet();
    }

    private void getEntitiesRenamings(Stream<? extends OWLEntity> entities,
                                      String base,
                                      String toBase,
                                      Map<OWLEntity, IRI> renameMap,
                                      long limit) {
        entities.filter(e -> e.getIRI().length() > base.length())
                .filter(e -> e.getIRI().subSequence(0, base.length()).equals(base))
                .limit(limit)
                .forEach(entity -> {
                    IRI iri = entity.getIRI();
                    renameMap.put(entity, IRI.create(toBase + iri.subSequence(base.length(), iri.length())));
                });
    }

}
