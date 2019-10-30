package org.protege.editor.owl.model.hierarchy;

import com.github.owlcs.ontapi.DataFactory;
import com.github.owlcs.ontapi.OntApiException;
import com.github.owlcs.ontapi.OntManagers;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;

/**
 * Created by @ssz on 17.09.2018.
 */
class OWLFunctionalSyntaxFactory {

    private static final DataFactory DF = OntManagers.getDataFactory();

    static OWLSubObjectPropertyOfAxiom createSubObjectPropertyOf(
            OWLObjectPropertyExpression subProperty,
            OWLObjectPropertyExpression superProperty) {
        return DF.getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
    }

    static OWLSubDataPropertyOfAxiom createSubDataPropertyOf(OWLDataPropertyExpression subProperty,
                                                             OWLDataPropertyExpression superProperty) {
        return DF.getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
    }

    static OWLDataProperty createDataProperty(String abbreviatedIRI, PrefixManager pm) {
        return DF.getOWLDataProperty(abbreviatedIRI, pm);
    }

    static OWLObjectProperty createObjectProperty(String abbreviatedIRI, PrefixManager pm) {
        return DF.getOWLObjectProperty(abbreviatedIRI, pm);
    }

    static OWLOntology createOntology(OWLOntologyManager man, OWLAxiom... axioms) {
        try {
            return man.createOntology(CollectionFactory.createSet(axioms));
        } catch (OWLOntologyCreationException e) {
            throw new OntApiException(e);
        }
    }
}
