package org.ru.avicomp.ontapi;

import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OntologyConfigurator;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import ru.avicomp.ontapi.OntManagers;

/**
 * Created by @szuev on 13.02.2018.
 */
public class OWLManager {
    public static ManchesterOWLSyntaxParser createManchesterParser() {
        return createManchesterParser(new OntologyConfigurator(), getOWLDataFactory());
    }

    public static ManchesterOWLSyntaxParser createManchesterParser(OntologyConfigurator conf, OWLDataFactory factory) {
        // todo: from owlapi-parsers, must be removed
        return new ManchesterOWLSyntaxParserImpl(conf, factory);
    }

    public static OWLOntologyManager createOWLOntologyManager() {
        return OntManagers.createONT();
    }

    public static OWLOntologyManager createConcurrentOWLOntologyManager() {
        return OntManagers.createConcurrentONT();
    }

    public static OWLDataFactory getOWLDataFactory() {
        return OntManagers.getDataFactory();
    }
}
