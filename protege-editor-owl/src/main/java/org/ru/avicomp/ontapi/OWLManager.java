package org.ru.avicomp.ontapi;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * Created by @szuev on 13.02.2018.
 */
public class OWLManager {
    public static ManchesterOWLSyntaxParser createManchesterParser() {
        return org.semanticweb.owlapi.apibinding.OWLManager.createManchesterParser();
    }

    public static OWLOntologyManager createOWLOntologyManager() {
        return org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
    }

    public static OWLOntologyManager createConcurrentOWLOntologyManager() {
        return org.semanticweb.owlapi.apibinding.OWLManager.createConcurrentOWLOntologyManager();
    }

    public static OWLDataFactory getOWLDataFactory() {
        return org.semanticweb.owlapi.apibinding.OWLManager.getOWLDataFactory();
    }
}
