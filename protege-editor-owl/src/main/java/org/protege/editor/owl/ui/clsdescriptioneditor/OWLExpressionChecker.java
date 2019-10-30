package org.protege.editor.owl.ui.clsdescriptioneditor;

import org.github.owlcs.ontapi.OWLManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OntologyConfigurator;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 11-Oct-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public interface OWLExpressionChecker<O> {

    void check(String text) throws OWLExpressionParserException;

    O createObject(String text) throws OWLExpressionParserException;

    default ManchesterOWLSyntaxParser getParser(OntologyConfigurator conf, OWLDataFactory factory) {
        return OWLManager.createManchesterParser(conf, factory);
    }

    default ManchesterOWLSyntaxParser getParser(OWLDataFactory factory) {
        return getParser(new OntologyConfigurator(), factory);
    }
}
