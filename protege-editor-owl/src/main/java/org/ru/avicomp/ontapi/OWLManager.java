package org.ru.avicomp.ontapi;

import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OntologyConfigurator;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;

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
            LOGGER.debug("OWLOntologyManager: {}", res);
        }
        return res;
    }

    public static OWLDataFactory getOWLDataFactory() {
        OWLDataFactory res = OntManagers.getDataFactory();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("OWLDataFactory: {}", res);
        }
        return res;
    }
}
