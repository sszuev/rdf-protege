package org.protege.editor.owl.model.io;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.github.owlcs.ontapi.OWLManager;
import org.protege.editor.owl.model.IOListenerManager;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.ui.util.ProgressDialog;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.PriorityCollection;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 May 16
 */
public class OntologyLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyLoader.class);

    private final OWLModelManager manager;
    private final UserResolvedIRIMapper userResolvedIRIMapper;

    private final ProgressDialog dlg = new ProgressDialog();

    private final ListeningExecutorService ontologyLoadingService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    public OntologyLoader(OWLModelManager modelManager, UserResolvedIRIMapper userResolvedIRIMapper) {
        this.manager = Objects.requireNonNull(modelManager);
        this.userResolvedIRIMapper = Objects.requireNonNull(userResolvedIRIMapper);
    }

    public Optional<OWLOntology> loadOntology(OWLOntologyDocumentSource src,
                                              OWLOntologyLoaderConfiguration conf) throws OWLOntologyCreationException {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("The ontology loader must be called from the Event Dispatch Thread");
        }
        return loadOntologyInOtherThread(src, conf);
    }

    private Optional<OWLOntology> loadOntologyInOtherThread(OWLOntologyDocumentSource src,
                                                            OWLOntologyLoaderConfiguration conf) throws OWLOntologyCreationException {
        ListenableFuture<Optional<OWLOntology>> res = ontologyLoadingService.submit(() -> {
            try {
                return loadOntologyInternal(src, conf);
            } finally {
                dlg.setVisible(false);
            }
        });
        dlg.setVisible(true);
        try {
            return res.get();
        } catch (InterruptedException e) {
            return Optional.empty();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof OWLOntologyCreationException) {
                throw (OWLOntologyCreationException) e.getCause();
            }
            LOGGER.error("An error occurred whilst loading the ontology at {}. Cause: '{}'",
                    src.getDocumentIRI(), e.getCause().getMessage(), e);
            return Optional.empty();
        }
    }

    private OWLOntologyManager getOntologyManager() {
        return manager.getOWLOntologyManager();
    }

    private Optional<OWLOntology> loadOntologyInternal(OWLOntologyDocumentSource src,
                                                       OWLOntologyLoaderConfiguration conf) throws OWLOntologyCreationException {

        // manager no need to be a concurrent
        OWLOntologyManager loadingManager = OWLManager.createOWLOntologyManager();

        PriorityCollection<OWLOntologyIRIMapper> iriMappers = loadingManager.getIRIMappers();
        iriMappers.clear();
        iriMappers.add(userResolvedIRIMapper);
        iriMappers.add(new WebConnectionIRIMapper());
        iriMappers.add(new AutoMappedRepositoryIRIMapper(manager.getOntologyCatalogManager()));

        loadingManager.addOntologyLoaderListener(new ProgressDialogOntologyLoaderListener(dlg, LOGGER));
        URI documentURI = src.getDocumentIRI().toURI();
        OWLOntologyID id = loadingManager.loadOntologyFromOntologyDocument(src, conf).getOntologyID();
        Set<OWLOntology> alreadyLoadedOntologies = new HashSet<>();
        loadingManager.ontologies().forEach(o -> {
            if (!manager.getOntologies().contains(o)) {
                OWLOntologyManager modelManager = getOntologyManager();
                fireBeforeLoad(o, documentURI);
                OWLManager.copy(o, modelManager);
                fireAfterLoad(o, documentURI);
            } else {
                alreadyLoadedOntologies.add(o);
            }
        });
        if (!alreadyLoadedOntologies.isEmpty()) {
            displayOntologiesAlreadyLoadedMessage(alreadyLoadedOntologies);
        }

        OWLOntology ontology = Objects.requireNonNull(getOntologyManager().getOntology(id));

        manager.setActiveOntology(ontology);
        manager.fireEvent(EventType.ONTOLOGY_LOADED);
        id.getDefaultDocumentIRI()
                .ifPresent(iri -> getOntologyManager().getIRIMappers()
                        .add(new SimpleIRIMapper(iri, IRI.create(documentURI))));
        return Optional.of(ontology);
    }

    private void displayOntologiesAlreadyLoadedMessage(Set<OWLOntology> alreadyLoadedOntologies) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("The following ontologies are already loaded in this workspace<br><br>");
        for (OWLOntology o : alreadyLoadedOntologies) {
            sb.append("<b>").append(manager.getRendering(o)).append("</b><br>");
        }
        sb.append("<br>");
        sb.append("They have not been replaced/overwritten");
        sb.append("</body></html>");

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, sb.toString(),
                "Workspace already contains loaded ontologies", JOptionPane.WARNING_MESSAGE));
    }

    private void fireBeforeLoad(OWLOntology loadedOntology, URI documentURI) {
        if (!(manager instanceof IOListenerManager)) {
            return;
        }
        ((IOListenerManager) manager).fireAfterLoadEvent(loadedOntology.getOntologyID(), documentURI);
    }

    private void fireAfterLoad(OWLOntology loadedOntology, URI documentURI) {
        if (!(manager instanceof IOListenerManager)) {
            return;
        }
        ((IOListenerManager) manager).fireAfterLoadEvent(loadedOntology.getOntologyID(), documentURI);
    }

}
