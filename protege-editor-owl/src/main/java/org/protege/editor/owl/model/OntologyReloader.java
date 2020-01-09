package org.protege.editor.owl.model;

import com.github.owlcs.ontapi.OWLAdapter;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.github.owlcs.ontapi.OWLManager;
import org.protege.editor.core.log.LogBanner;
import org.protege.editor.owl.ui.util.ProgressDialog;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.PriorityCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 21/01/16
 */
public class OntologyReloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyReloader.class);

    private final OWLOntology ontologyToReload;

    private final ProgressDialog dlg = new ProgressDialog();

    private final OWLModelManager modelManager;

    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
            Executors.newSingleThreadExecutor()
    );

    public OntologyReloader(OWLOntology ontologyToReload, OWLModelManager modelManager) {
        this.ontologyToReload = ontologyToReload;
        this.modelManager = modelManager;
    }

    /**
     * Reloads the specified ontology.  Either the ontology is successfully reloaded or it is left intact.
     *
     * @throws OWLOntologyCreationException can't reload
     */
    public void reload() throws OWLOntologyCreationException {
        LOGGER.info(LogBanner.start("Reloading ontology"));
        LOGGER.info("Reloading ontology: {}", ontologyToReload.getOntologyID());
        try {
            // Load the ontology as a fresh ontology
            List<OWLOntologyChange> changes = reloadOntologyAndGetPatch();
            LOGGER.info("Applying {} change(s) to patch ontology to reloaded ontology", changes.size());
            if (changes.isEmpty()) {
                return;
            }
            ontologyToReload.getOWLOntologyManager().applyChanges(changes);
            if (modelManager instanceof IOListenerManager) {
                ((IOListenerManager) modelManager).fireAfterLoadEvent(ontologyToReload.getOntologyID(),
                        ontologyToReload.getOWLOntologyManager().getOntologyDocumentIRI(ontologyToReload).toURI());
            }
        } catch (Throwable t) {
            if (t instanceof OWLOntologyCreationException) {
                throw (OWLOntologyCreationException) t;
            }
            throw new OWLOntologyCreationException(t);
        }
    }

    private List<OWLOntologyChange> reloadOntologyAndGetPatch() throws OWLOntologyCreationException {
        ListenableFuture<List<OWLOntologyChange>> future = executorService
                .submit(() -> {
                    try {
                        return performReloadAndGetPatch();
                    } finally {
                        dlg.setVisible(false);
                    }
                });
        dlg.setVisible(true);
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new OWLOntologyCreationException(e);
        } catch (ExecutionException e) {
            LOGGER.debug("Error while reloading: '{}'", e.getMessage());
            if (e.getCause() instanceof OWLOntologyCreationException) {
                throw (OWLOntologyCreationException) e.getCause();
            }
            LOGGER.error("An error occurred whilst reloading the ontology: {}", e.getMessage(), e);
            throw new OWLOntologyCreationException(e);
        }
    }

    @SuppressWarnings("deprecation")
    private List<OWLOntologyChange> performReloadAndGetPatch() throws OWLOntologyCreationException {
        dlg.setMessage("Reloading ontology");
        OWLDocumentFormat format = ontologyToReload.getFormat();
        OWLOntologyManager reloadingManager = OWLManager.createOWLOntologyManager();
        PriorityCollection<OWLOntologyIRIMapper> iriMappers = reloadingManager.getIRIMappers();
        iriMappers.clear();
        // Should be able to share these
        iriMappers.add(ontologyToReload.getOWLOntologyManager().getIRIMappers());
        Stopwatch sw = Stopwatch.createStarted();
        long freeMemory0 = Runtime.getRuntime().freeMemory();
        // We might need declarations and imports for parsing the reloaded ontology.  Copy as much as we really need.
        OWLOntologyManager manager = ontologyToReload.getOWLOntologyManager();

        for (OWLOntology o : manager.getOntologies()) {
            // We don't need the ontology that we want to reload
            if (o.equals(ontologyToReload)) {
                continue;
            }
            reloadingManager.createOntology(o.getOntologyID());
            reloadingManager.setOntologyDocumentIRI(o, manager.getOntologyDocumentIRI(o));
            Set<OWLDeclarationAxiom> axioms = o.getAxioms(AxiomType.DECLARATION);
            LOGGER.info("Copying {} declaration axioms from {} for reloading", axioms.size(), o.getOntologyID());
            reloadingManager.addAxioms(o, axioms);
        }
        sw.stop();
        long freeMemory1 = Runtime.getRuntime().freeMemory();
        long usedMemMb = (long) ((freeMemory0 - freeMemory1) / (1024 * 1024.0));
        LOGGER.info("Copied ontologies in {} ms.  Used: {} MB", sw.elapsed(TimeUnit.MILLISECONDS), usedMemMb);

        IRI iri = manager.getOntologyDocumentIRI(ontologyToReload);
        OWLOntologyDocumentSource src = new IRIDocumentSource(iri, format, null);
        OWLOntologyLoaderConfiguration conf = OWLAdapter.get().asONT(manager.getOntologyLoaderConfiguration())
                .setPerformTransformation(false);
        LOGGER.debug("Start reloading {}", iri);
        OWLOntology reloadedOntology = reloadingManager.loadOntologyFromOntologyDocument(src, conf);
        // Compute a diff between the original and the reloaded ontology
        List<OWLOntologyChange> changes = new ArrayList<>();
        generateChangesToTransferContent(reloadedOntology, ontologyToReload, changes);
        return changes;
    }

    /**
     * Generates
     * @param from The ontology that should essentially be the final ontology
     * @param to The ontology to which changes should be applied to make it the same (in terms of id, annotations,
     *           imports and axioms) as the from ontology
     * @param changeList A list that will be filled with changes
     */
    @SuppressWarnings("deprecation")
    private static void generateChangesToTransferContent(OWLOntology from, OWLOntology to, List<OWLOntologyChange> changeList) {
        for (OWLImportsDeclaration decl : from.getImportsDeclarations()) {
            if (!to.getImportsDeclarations().contains(decl)) {
                changeList.add(new AddImport(to, decl));
            }
        }
        for (OWLImportsDeclaration decl : to.getImportsDeclarations()) {
            if (!from.getImportsDeclarations().contains(decl)) {
                changeList.add(new RemoveImport(to, decl));
            }
        }
        for (OWLAnnotation annotation : from.getAnnotations()) {
            if (!to.getAnnotations().contains(annotation)) {
                changeList.add(new AddOntologyAnnotation(to, annotation));
            }
        }
        for (OWLAnnotation annotation : to.getAnnotations()) {
            if (!from.getAnnotations().contains(annotation)) {
                changeList.add(new RemoveOntologyAnnotation(to, annotation));
            }
        }
        for (OWLAxiom ax : from.getAxioms()) {
            if (!to.containsAxiom(ax)) {
                changeList.add(new AddAxiom(to, ax));
            }
        }
        for (OWLAxiom ax : to.getAxioms()) {
            if (!from.containsAxiom(ax)) {
                changeList.add(new RemoveAxiom(to, ax));
            }
        }
        if (!from.getOntologyID().equals(to.getOntologyID())) {
            changeList.add(new SetOntologyID(to, from.getOntologyID()));
        }
    }
}
