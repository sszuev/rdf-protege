package org.protege.editor.owl.model.repository;

import org.protege.editor.owl.model.repository.extractors.LastResortExtractor;
import org.protege.editor.owl.model.repository.extractors.OntologyIdExtractor;
import org.protege.editor.owl.model.repository.extractors.RdfXmlExtractor;
import org.semanticweb.owlapi.model.OWLOntologyID;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MasterOntologyIDExtractor implements OntologyIdExtractor {

    private final List<OntologyIdExtractor> extractors = new ArrayList<>();

    // TODO - all the callers of this method have problems which need fixing.
    public MasterOntologyIDExtractor() {
        extractors.add(new RdfXmlExtractor());
        extractors.add(new LastResortExtractor());
    }

    @Override
    public Optional<OWLOntologyID> getOntologyId(URI location) {
        Optional<OWLOntologyID> id = Optional.empty();
        for (OntologyIdExtractor extractor : extractors) {
            id = extractor.getOntologyId(location);
            if (id.isPresent()) {
                break;
            }
        }
        return id;
    }

}
