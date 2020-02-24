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
		return ontologies.stream()
				.filter(OWLUtilities::process) // for speedup - a temporary solution (todo: need to fix ONT=API)
				.flatMap(x -> x.annotationAssertionAxioms(o.getIRI()))
				.filter(a -> a.getProperty().isDeprecated())
				.map(OWLAnnotationAssertionAxiom::getValue)
				.filter(v -> v instanceof OWLLiteral)
				.map(v -> (OWLLiteral) v)
				.filter(OWLLiteral::isBoolean)
				.anyMatch(OWLLiteral::parseBoolean);
	}

	private static boolean process(OWLOntology o) {
		Boolean res = hasDeprecated(o);
		return res == null || res;
	}

	private static boolean hasDeprecated(com.github.owlcs.ontapi.Ontology ont) {
		return ont.asGraphModel().getBaseGraph()
				.contains(org.apache.jena.graph.Node.ANY,
						com.github.owlcs.ontapi.jena.vocabulary.OWL.deprecated.asNode(), org.apache.jena.graph.Node.ANY);
	}

	private static Boolean hasDeprecated(OWLOntology ont) {
		if (!(ont instanceof com.github.owlcs.ontapi.Ontology)) {
			return null;
		}
		return hasDeprecated((com.github.owlcs.ontapi.Ontology) ont);
	}
}
