package org.protege.editor.owl.ui.prefix;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.prefix.ActiveOntologyComparator;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.PrefixDocumentFormatImpl;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PrefixUtilities {
	private static final Logger LOGGER = LoggerFactory.getLogger(PrefixUtilities.class);

	public static final Set<String> STANDARD_PREFIXES =
			Collections.unmodifiableSet(createFreshPrefixManager().prefixNames().collect(Collectors.toSet()));

	public static PrefixManager createFreshPrefixManager() {
		return new DefaultPrefixManager();
	}

	public static PrefixManager getPrefixOWLOntologyFormat(OWLModelManager modelManager) {
		OWLOntologyManager owlManager = modelManager.getOWLOntologyManager();
		PrefixManager res = createFreshPrefixManager();
		List<OWLOntology> ontologies = new ArrayList<>(modelManager.getOntologies());
		ontologies.sort(new ActiveOntologyComparator());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Sorted ontologies = {}", ontologies);
		}
		Set<String> prefixValues = new HashSet<>();
		for (OWLOntology ontology : ontologies) {
			OWLDocumentFormat format = owlManager.getOntologyFormat(ontology);
			if (!(format instanceof PrefixDocumentFormat)) {
				continue;
			}
			PrefixDocumentFormat newPrefixes = (PrefixDocumentFormat) format;
			newPrefixes.getPrefixName2PrefixMap().forEach((name, prefix) -> {
				if (!res.containsPrefixMapping(name) && !prefixValues.contains(prefix)) {
					res.setPrefix(name, prefix);
					prefixValues.add(prefix);
				}
			});
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Merged prefix to prefix value map = {}", res.getPrefixName2PrefixMap());
		}
		return res;
	}

	public static PrefixDocumentFormat getPrefixOWLOntologyFormat(OWLOntology ontology) {
		PrefixDocumentFormat prefixManager = null;
		if (ontology != null) {
			OWLOntologyManager manager = ontology.getOWLOntologyManager();
			OWLDocumentFormat format = manager.getOntologyFormat(ontology);
			if (format != null && format.isPrefixOWLDocumentFormat()) {
				prefixManager = format.asPrefixOWLDocumentFormat();
			}
		}
		if (prefixManager == null) {
			prefixManager = new PrefixDocumentFormatImpl();
		}
		return prefixManager;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isStandardPrefix(String prefix) {
		return STANDARD_PREFIXES.contains(prefix + ":");
	}
}
