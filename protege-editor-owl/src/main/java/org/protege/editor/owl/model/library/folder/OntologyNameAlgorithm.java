package org.protege.editor.owl.model.library.folder;

import com.github.owlcs.ontapi.config.OntConfig;
import com.github.owlcs.ontapi.jena.impl.conf.OntModelConfig;
import org.github.owlcs.ontapi.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class OntologyNameAlgorithm implements Algorithm {

    @Override
    public Set<URI> getSuggestions(File f) {
        try {
            OWLOntologyDocumentSource src = new FileDocumentSource(f);
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(src, createSafeConfiguration());

            Set<URI> suggestions = new TreeSet<>();
            OWLOntologyID id = ontology.getOntologyID();
            if (id.getOntologyIRI().isPresent()) {
                suggestions.add(id.getOntologyIRI().get().toURI());
                if (id.getVersionIRI().isPresent()) {
                    suggestions.add(id.getVersionIRI().get().toURI());
                }
            }
            return suggestions;
        } catch (Throwable t) {
            return Collections.emptySet();
        }
    }

    public static OWLOntologyLoaderConfiguration createSafeConfiguration() {
        return new OntConfig()
                .buildLoaderConfiguration()
                .setPerformTransformation(false)
                .setPersonality(OntModelConfig.ONT_PERSONALITY_LAX)
                .setIgnoreAxiomsReadErrors(true)
                .setProcessImports(false)
                .setLoadAnnotationAxioms(false)
                .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
    }
}
