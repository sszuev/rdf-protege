package org.protege.editor.owl.model.util;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.Set;

public class OWLUtilities {

	private OWLUtilities() {
	}
	
    public static boolean isDeprecated(OWLModelManager manager, OWLObject o) {
    	if (!(o instanceof OWLEntity)) {
    		return false;
    	}
		Set<OWLOntology> activeOntologies = manager.getActiveOntologies();
		return isDeprecated((OWLEntity) o, activeOntologies);
    }

	private static boolean isDeprecated(OWLEntity o, Collection<OWLOntology> ontologies) {
		return ontologies.stream().flatMap(x -> x.annotationAssertionAxioms(o.getIRI()))
				.filter(a -> a.getProperty().isDeprecated())
				.map(OWLAnnotationAssertionAxiom::getValue)
				.filter(v -> v instanceof OWLLiteral)
				.map(v -> (OWLLiteral) v)
				.filter(OWLLiteral::isBoolean)
				.anyMatch(OWLLiteral::parseBoolean);
	}
}
