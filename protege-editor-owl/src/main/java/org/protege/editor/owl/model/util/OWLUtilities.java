package org.protege.editor.owl.model.util;

import org.github.owlcs.ontapi.OWLManager;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.Set;

public final class OWLUtilities {
	private static final OWLAnnotationProperty OWL_DEPRECATED = OWLManager.getOWLDataFactory().getOWLDeprecated();

	public static boolean isDeprecated(OWLModelManager manager, OWLObject o) {
		if (!(o instanceof OWLEntity)) {
			return false;
		}
		Set<OWLOntology> activeOntologies = manager.getActiveOntologies();
		return isDeprecated((OWLEntity) o, activeOntologies);
	}

	private static boolean isDeprecated(OWLEntity entity, Collection<OWLOntology> ontologies) {
		return ontologies.stream()
				.filter(OWLUtilities::hasDeprecated)
				.flatMap(o -> o.annotationAssertionAxioms(entity.getIRI()))
				.filter(a -> a.getProperty().isDeprecated())
				.map(OWLAnnotationAssertionAxiom::getValue)
				.filter(OWLAnnotationValue::isLiteral)
				.map(v -> (OWLLiteral) v)
				.filter(OWLLiteral::isBoolean)
				.anyMatch(OWLLiteral::parseBoolean);
	}

	public static boolean hasDeprecated(OWLOntology ont) {
		return ont.containsAnnotationPropertyInSignature(OWL_DEPRECATED.getIRI());
	}
}
