package org.protege.editor.owl.model.repository.extractors;

import org.github.owlcs.ontapi.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;

public class LastResortExtractor implements OntologyIdExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LastResortExtractor.class);

    @Override
    public Optional<OWLOntologyID> getOntologyId(URI location) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(location));
            return Optional.of(ontology.getOntologyID());
        } catch (Throwable t) {
            LOGGER.info("Exception caught trying to get ontology id for {}", location, t);
            return Optional.empty();
        }
    }
}
