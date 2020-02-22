package org.protege.editor.owl.model.refactor.ontology;

import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 22-Jun-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OntologyMerger {
    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyMerger.class);

    private final OWLOntologyManager manager;
    private final Set<OWLOntology> ontologies;
    private final OWLOntology targetOntology;

    public OntologyMerger(OWLOntologyManager manager, Set<OWLOntology> ontologies, OWLOntology targetOntology) {
        this.ontologies = new HashSet<>(ontologies);
        this.manager = manager;
        this.targetOntology = targetOntology;
    }

    public void mergeOntologies() {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLOntology ont : ontologies) {
            if (ont.equals(targetOntology)) {
                continue;
            }

            // move the axioms
            ont.axioms().map(a -> new AddAxiom(targetOntology, a)).forEach(changes::add);

            // move ontology annotations
            ont.annotations().map(a -> new AddOntologyAnnotation(targetOntology, a)).forEach(changes::add);

            if (targetOntology.getOntologyID().isAnonymous()) {
                continue;
            }
            // move ontology imports
            ont.importsDeclarations()
                    .filter(d -> !ontologies.contains(ont.getOWLOntologyManager().getImportedOntology(d)))
                    .forEach(d -> {
                        Optional<IRI> iri = targetOntology.getOntologyID().getDefaultDocumentIRI();
                        if (iri.isPresent() && !d.getIRI().equals(iri.get())) {
                            changes.add(new AddImport(targetOntology, d));
                            return;
                        }
                        LOGGER.warn("Merge: ignoring import declaration for ontology {}" +
                                        " (would result in target ontology importing itself).",
                                targetOntology.getOntologyID());
                    });
        }
        try {
            manager.applyChanges(changes);
        } catch (OWLOntologyChangeException e) {
            ErrorLogPanel.showErrorDialog(e);
        }
    }
}
