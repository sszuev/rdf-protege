package org.protege.editor.owl.model.repository;

import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.semanticweb.owlapi.util.PriorityCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 18-Oct-2008<br><br>
 */
public class ManchesterRepository implements OntologyRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManchesterRepository.class);

    private final String repositoryName;
    private final URI repositoryLocation;
    private final List<OntologyRepositoryEntry> entries;
    private final OWLOntologyIRIMapper iriMapper;

    public ManchesterRepository(String repositoryName, URI repositoryLocation) {
        this.repositoryName = repositoryName;
        this.repositoryLocation = repositoryLocation;
        entries = new ArrayList<>();
        iriMapper = new RepositoryIRIMapper();
    }

    @Override
    public String getName() {
        return repositoryName;
    }

    @Override
    public String getLocation() {
        return repositoryLocation.toString();
    }

    @Override
    public void refresh() {
        fillRepository();
    }

    @Override
    public Collection<OntologyRepositoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public List<Object> getMetaDataKeys() {
        return Collections.emptyList();
    }

    @Override
    public void dispose() throws Exception {
    }

    private void fillRepository() {
        entries.clear();
        URI listURI = URI.create(repositoryLocation + "/list");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(listURI.toURL().openStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    entries.add(new RepositoryEntry(new URI(line)));
                } catch (URISyntaxException e) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("Can't process line '{}'", line, e);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error while fill repository", e);
        }
    }

    private class RepositoryEntry implements OntologyRepositoryEntry {
        private final String shortName;
        private final URI ontologyURI;
        private final URI physicalURI;

        public RepositoryEntry(URI ontologyIRI) {
            this.ontologyURI = ontologyIRI;
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(IRI.create(ontologyIRI));
            physicalURI = URI.create(repositoryLocation + "/download?ontology=" + ontologyIRI);
        }

        @Override
        public String getOntologyShortName() {
            return shortName;
        }

        @Override
        public URI getOntologyURI() {
            return ontologyURI;
        }

        @Override
        public URI getPhysicalURI() {
            return physicalURI;
        }

        @Override
        public String getEditorKitId() {
            return "org.protege.editor.owl.OWLEditorKitFactory";
        }

        @Override
        public String getMetaData(Object key) {
            return null;
        }

        @Override
        public void configureEditorKit(EditorKit editorKit) {
            getIRIMapper(editorKit).add(iriMapper);
        }

        @Override
        public void restoreEditorKit(EditorKit editorKit) {
            getIRIMapper(editorKit).remove(iriMapper);
        }

        private PriorityCollection<OWLOntologyIRIMapper> getIRIMapper(EditorKit kit) {
            return getManager(kit).getIRIMappers();
        }

        private OWLOntologyManager getManager(EditorKit kit) {
            return ((OWLEditorKit) kit).getOWLModelManager().getOWLOntologyManager();
        }
    }


    private class RepositoryIRIMapper implements OWLOntologyIRIMapper {

        @Override
        public IRI getDocumentIRI(@Nonnull IRI iri) {
            for (OntologyRepositoryEntry entry : entries) {
                if (entry.getOntologyURI().equals(iri.toURI())) {
                    return IRI.create(entry.getPhysicalURI());
                }
            }
            return null;
        }
    }
}
