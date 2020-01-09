package org.github.owlcs.ontapi;

import com.github.owlcs.ontapi.DataFactory;
import com.github.owlcs.ontapi.OntApiException;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.OntologyManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by @szuev on 13.02.2018.
 */
public class OWLManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(OWLManager.class);

    public static ManchesterOWLSyntaxParser createManchesterParser() {
        return createManchesterParser(new OntologyConfigurator(), getOWLDataFactory());
    }

    public static ManchesterOWLSyntaxParser createManchesterParser(OntologyConfigurator conf, OWLDataFactory factory) {
        // todo: from owlapi-parsers, must be removed
        ManchesterOWLSyntaxParser res = new ManchesterOWLSyntaxParserImpl(conf, factory);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ManchesterOWLSyntaxParser: {}", res);
        }
        return res;
    }

    public static OntologyManager createOWLOntologyManager() {
        OntologyManager res = OntManagers.createONT();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("OWLOntologyManager: {}", res);
        }
        return res;
    }

    public static OntologyManager createConcurrentOWLOntologyManager() {
        OntologyManager res = OntManagers.createConcurrentONT();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ConcurrentOWLOntologyManager: {}", res);
        }
        return res;
    }

    public static DataFactory getOWLDataFactory() {
        DataFactory res = OntManagers.getDataFactory();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("OWLDataFactory: {}", res);
        }
        return res;
    }

    /**
     * Copies the given ontology into the desired manager.
     * <p>
     * Starting v1.4.1 ONT-API does not support {@link OntologyCopy#MOVE MOVE} anymore.
     * Here the mode {@link OntologyCopy#SHALLOW SHALLOW} is used.
     * In ONT-API it just copies a base graph reference, providing a <b>different</b> facade instance.
     * The actual RDF data copied are not copied.
     *
     * @param from {@link OWLOntology} to copy, not {@code null}
     * @param to   {@link OWLOntologyManager}, the destination, not {@code null}
     * @return a ready to use instance, that wraps the data shared between two managers:
     * the specified and the original; but <b>note</b>: the instance is not the same as specified!
     * @throws OntApiException in case of error
     * @see <a href='https://github.com/avicomp/ont-api/issues/74'>copyOntology, v1.4.1</a>
     */
    public static OWLOntology copy(OWLOntology from, OWLOntologyManager to) throws OntApiException {
        OWLOntologyManager src = from.getOWLOntologyManager();
        IRI doc = src.getOntologyDocumentIRI(from);
        OWLDocumentFormat format = src.getOntologyFormat(from);
        OWLOntology res;
        try {
            res = to.copyOntology(from, OntologyCopy.SHALLOW);
        } catch (OWLOntologyCreationException e) {
            throw new OntApiException("Can't copy ontology " + src + ": '" + e.getMessage() + "'", e);
        }
        to.setOntologyDocumentIRI(res, doc);
        if (format != null)
            to.setOntologyFormat(res, format);
        return res;
    }
}
