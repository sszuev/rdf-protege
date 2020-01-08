package org.protege.editor.owl.model.repository.extractors;

import org.protege.editor.owl.model.io.IOUtils;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

public class RdfXmlExtractor implements OntologyIdExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdfXmlExtractor.class);

    @Override
    public Optional<OWLOntologyID> getOntologyId(URI location) {
        RdfExtractorConsumer consumer = new RdfExtractorConsumer();
        RDFParser parser = new RDFParser();
        try (InputStream iStream = IOUtils.getInputStream(location, true, 30000)) {
            InputSource is = new InputSource(iStream);
            is.setSystemId(location.toURL().toString());
            parser.parse(is, consumer);
            return consumer.getOntologyID();
        } catch (Throwable t) {
            LOGGER.debug("Exception caught trying to extract ontology from rdf file at  {}", location, t);
            return Optional.empty();
        }
    }
}
