package org.protege.editor.owl.model.inference;

import com.google.common.base.Stopwatch;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.log.LogBanner;
import org.protege.editor.core.ui.util.Resettable;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.ui.explanation.io.InconsistentOntologyManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 19-Jun-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLReasonerManagerImpl implements OWLReasonerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(OWLReasonerManager.class);

    private final OWLModelManager manager;
    private final ReasonerPreferences preferences;

    private Set<ProtegeOWLReasonerInfo> reasonerFactories;

    private ProtegeOWLReasonerInfo currentReasonerFactory;

    private final Map<OWLOntology, OWLReasoner> reasonerMap = new HashMap<>();

    private OWLReasoner runningReasoner;

    private boolean classificationInProgress = false;

    private ReasonerProgressMonitor reasonerProgressMonitor;

    private OWLReasonerExceptionHandler exceptionHandler;

    private List<ReasonerFilter> reasonerFilters = new ArrayList<>();

    @SuppressWarnings("NullableProblems")
    private final OWLOntologyChangeListener nonBufferingOntologyChangeListener = new OWLOntologyChangeListener() {
        @Override
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            OWLReasoner reasoner = getCurrentReasoner();
            if (reasoner instanceof NoOpReasoner || reasoner.getBufferingMode() != BufferingMode.NON_BUFFERING) {
                return;
            }
            OWLOntology activeOntology = manager.getActiveOntology();
            Set<OWLOntology> importClosure = null;
            boolean needsRefresh = false;

            for (OWLOntologyChange change : changes) {
                if (change instanceof AnnotationChange) {
                    continue;
                }
                if (change instanceof SetOntologyID) {
                    continue;
                }
                if (change instanceof OWLAxiomChange && !change.getAxiom().isLogicalAxiom()) {
                    continue;
                }
                OWLOntology changedOntology = change.getOntology();
                if (!changedOntology.equals(activeOntology)) {
                    if (importClosure == null) {
                        importClosure = activeOntology.importsClosure().collect(Collectors.toSet());
                    }
                    if (!importClosure.contains(changedOntology)) {
                        continue;
                    }
                }
                // otherwise
                needsRefresh = true;
                break;
            }
            if (!needsRefresh) {
                return;
            }
            // too tricky... too tricky... wait until after the reasoner has reacted to the changes.
            SwingUtilities.invokeLater(() -> {
                if (manager.getOWLReasonerManager().getReasonerStatus() == ReasonerStatus.INITIALIZED) {
                    fireReclassified();
                }
            });
        }
    };

    public OWLReasonerManagerImpl(OWLModelManager owlModelManager) {
        this.manager = Objects.requireNonNull(owlModelManager);
        preferences = new ReasonerPreferences();
        preferences.load();
        reasonerFactories = new HashSet<>();
        reasonerProgressMonitor = new NullReasonerProgressMonitor();
        installFactories();
        exceptionHandler = new DefaultOWLReasonerExceptionHandler();
        owlModelManager.addOntologyChangeListener(nonBufferingOntologyChangeListener);
    }

    @Override
    public void setReasonerExceptionHandler(OWLReasonerExceptionHandler handler) {
        exceptionHandler = handler != null ? handler : new DefaultOWLReasonerExceptionHandler();
    }

    @Override
    public void dispose() throws Exception {
        if (preferences != null) {
            preferences.save();
        }
        clearAndDisposeReasoners();
        if (reasonerProgressMonitor instanceof Disposable) {
            ((Disposable) reasonerProgressMonitor).dispose();
        }
        manager.removeOntologyChangeListener(nonBufferingOntologyChangeListener);
    }

    private void clearAndDisposeReasoners() {
        for (OWLReasoner reasoner : reasonerMap.values()) {
            if (reasoner == null) {
                continue;
            }
            try {
                reasoner.dispose();
            } catch (Throwable t) {
                LOGGER.warn("An error occurred whilst disposing of the '{}' reasoner.  Error: {}",
                        reasoner.getReasonerName(), t);
            }
        }
        reasonerMap.clear();
    }

    @Override
    public String getCurrentReasonerName() {
        return getCurrentReasoner().getReasonerName();
    }

    @Override
    public ProtegeOWLReasonerInfo getCurrentReasonerFactory() {
        if (currentReasonerFactory == null) {
            currentReasonerFactory = new NoOpReasonerInfo();
        }
        return currentReasonerFactory;
    }

    @Override
    public void setReasonerProgressMonitor(ReasonerProgressMonitor progressMonitor) {
        this.reasonerProgressMonitor = progressMonitor;
    }

    @Override
    public Set<ProtegeOWLReasonerInfo> getInstalledReasonerFactories() {
        return reasonerFactories;
    }

    private void installFactories() {
        ProtegeOWLReasonerPluginLoader loader = new ProtegeOWLReasonerPluginLoader(manager);
        addReasonerFactories(loader.getPlugins());
        setCurrentReasonerFactoryId(preferences.getDefaultReasonerId());
    }

    public void addReasonerFactories(Set<ProtegeOWLReasonerPlugin> plugins) {
        for (ProtegeOWLReasonerPlugin plugin : plugins) {
            try {
                ProtegeOWLReasonerInfo factory = plugin.newInstance();
                factory.initialise();
                reasonerFactories.add(factory);
            } catch (Throwable t) {
                LOGGER.warn("An error occurred whilst instantiating the '{}' reasoner.  Error: {}", plugin.getName(), t);
            }
        }
    }

    @Override
    public String getCurrentReasonerFactoryId() {
        return getCurrentReasonerFactory().getReasonerId();
    }

    @Override
    public void setCurrentReasonerFactoryId(String id) {
        if (getCurrentReasonerFactory().getReasonerId().equals(id)) {
            return;
        }
        for (ProtegeOWLReasonerInfo reasonerFactory : reasonerFactories) {
            if (!reasonerFactory.getReasonerId().equals(id)) {
                continue;
            }
            preferences.setDefaultReasonerId(id);
            preferences.save();
            clearAndDisposeReasoners();
            currentReasonerFactory = reasonerFactory;
            manager.fireEvent(EventType.REASONER_CHANGED);
            return;
        }
        LOGGER.warn("Reasoner with id " + id + " not found");
        preferences.setDefaultReasonerId(NoOpReasonerInfo.NULL_REASONER_ID);
        preferences.save();
    }

    @Override
    public OWLReasoner getCurrentReasoner() {
        OWLReasoner reasoner;
        OWLOntology activeOntology = manager.getActiveOntology();
        synchronized (reasonerMap) {
            reasoner = reasonerMap.get(activeOntology);
        }
        if (reasoner != null) {
            return reasoner;
        }
        reasoner = new NoOpReasoner(activeOntology);
        synchronized (reasonerMap) {
            reasonerMap.put(activeOntology, reasoner);
        }
        return reasoner;
    }

    @Override
    public void killCurrentReasoner() {
        OWLReasoner reasoner = getCurrentReasoner();
        if (reasoner instanceof NoOpReasoner) {
            return;
        }
        try {
            reasoner.dispose();
        } catch (Exception ex) {
            LOGGER.warn("An error occurred whilst disposing of the current reasoner ({}).  Error: {}",
                    reasoner.getReasonerName(), ex);
        }
        synchronized (reasonerMap) {
            reasonerMap.put(manager.getActiveOntology(), null);
        }
    }

    @Override
    public boolean isClassificationInProgress() {
        synchronized (reasonerMap) {
            return classificationInProgress;
        }
    }

    @Override
    public boolean isClassified() {
        synchronized (reasonerMap) {
            OWLReasoner reasoner = getCurrentReasoner();
            return !(reasoner instanceof NoOpReasoner) &&
                    (reasoner.getPendingChanges() == null || reasoner.getPendingChanges().isEmpty());
        }
    }

    @Override
    public ReasonerStatus getReasonerStatus() {
        synchronized (reasonerMap) {
            if (classificationInProgress) {
                return ReasonerStatus.INITIALIZATION_IN_PROGRESS;
            }
            if (currentReasonerFactory.getReasonerFactory() instanceof NoOpReasonerFactory) {
                return ReasonerStatus.NO_REASONER_FACTORY_CHOSEN;
            }
            OWLReasoner reasoner = getCurrentReasoner();
            try {
                if (reasoner instanceof NoOpReasoner) {
                    return ReasonerStatus.REASONER_NOT_INITIALIZED;
                }
                if (!reasoner.isConsistent()) {
                    return ReasonerStatus.INCONSISTENT;
                }
                if (reasoner.getPendingChanges().isEmpty()) {
                    return ReasonerStatus.INITIALIZED;
                }
                return ReasonerStatus.OUT_OF_SYNC;
            } catch (Throwable t) {
                killCurrentReasoner();
                LOGGER.warn("Protege terminated reasoner.");
                throw new ReasonerDiedException(t);
            }
        }
    }

    /**
     * Classifies the current active ontologies.
     *
     * @param precompute a {@code Set}
     * @return boolean
     */
    @Override
    public boolean classifyAsynchronously(Set<InferenceType> precompute) {
        if (getCurrentReasonerFactory() instanceof NoOpReasonerInfo) {
            return true;
        }
        OWLOntology ont = manager.getActiveOntology();
        synchronized (reasonerMap) {
            if (classificationInProgress) {
                return false;
            }
            runningReasoner = reasonerMap.get(ont);
            reasonerMap.put(ont, new NoOpReasoner(ont));
            classificationInProgress = true;
        }
        manager.fireEvent(EventType.ABOUT_TO_CLASSIFY);
        Thread currentReasonerThread = new Thread(new ClassificationRunner(ont, precompute), "Classification Thread");
        currentReasonerThread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("An error occurred during reasoning: {}.", throwable.getMessage(), throwable);
            try {
                if (getReasonerStatus() != ReasonerStatus.REASONER_NOT_INITIALIZED) {
                    exceptionHandler.handle(throwable);
                }
            } catch (ReasonerDiedException died) {
                ReasonerUtilities.warnThatReasonerDied(null, died);
            }
        });
        currentReasonerThread.start();
        return true;
    }

    @Override
    public void killCurrentClassification() {
        synchronized (reasonerMap) {
            if (runningReasoner != null) {
                runningReasoner.interrupt();
            }
        }
    }

    @Override
    public ReasonerPreferences getReasonerPreferences() {
        return preferences;
    }

    /**
     * Fires a reclassify event, ensuring that the event is fired in the event dispatch thread.
     */
    private void fireReclassified() {
        Runnable r = () -> manager.fireEvent(EventType.ONTOLOGY_CLASSIFIED);
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    @Override
    public void addReasonerFilter(ReasonerFilter filter) {
        synchronized (reasonerMap) {
            reasonerFilters.add(filter);
        }
    }

    private OWLOntology applyReasonerFilters(OWLOntology ontology) {
        synchronized (reasonerMap) {
            for (ReasonerFilter filter : reasonerFilters) {
                ontology = filter.getFilteredOntology(ontology);
            }
        }
        return ontology;
    }

    private class ClassificationRunner implements Runnable {

        private final OWLOntology ontology;
        private final Set<InferenceType> precompute;
        private final ProtegeOWLReasonerInfo currentReasonerFactory;

        public ClassificationRunner(OWLOntology ontology, Set<InferenceType> precompute) {
            this.ontology = ontology;
            this.precompute = EnumSet.noneOf(InferenceType.class);
            this.precompute.addAll(precompute);
            currentReasonerFactory = getCurrentReasonerFactory();
        }

        @Override
        public void run() {
            LOGGER.info(LogBanner.start("Running Reasoner"));
            boolean inconsistencyFound = false;
            boolean reasonerChanged = false;
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                reasonerChanged = ensureRunningReasonerInitialized();
                if (runningReasoner != null) {
                    precompute();
                    LOGGER.info("Ontologies processed in {} ms by {}", stopwatch.elapsed(TimeUnit.MILLISECONDS),
                            runningReasoner.getReasonerName());
                }
            } catch (ReasonerInterruptedException rie) {
                reasonerChanged = true;
                OWLReasoner reasonerInBadState = runningReasoner;
                runningReasoner = null;
                reasonerInBadState.dispose();
            } catch (InconsistentOntologyException ioe) {
                inconsistencyFound = true;
            } finally {
                if (runningReasoner != null) {
                    synchronized (reasonerMap) {
                        reasonerFilters.clear();
                    }
                }
                installRunningReasoner(inconsistencyFound, reasonerChanged);
                if (reasonerProgressMonitor instanceof Resettable) {
                    ((Resettable) reasonerProgressMonitor).reset();
                }
                LOGGER.info(LogBanner.end());
            }
        }

        public boolean ensureRunningReasonerInitialized() {
            boolean reasonerChanged = false;
            if (runningReasoner instanceof NoOpReasoner) {
                runningReasoner = null;
            }
            if (runningReasoner != null && !runningReasoner.getPendingChanges().isEmpty()) {
                if (runningReasoner.getBufferingMode() == null
                        || runningReasoner.getBufferingMode() == BufferingMode.NON_BUFFERING) {
                    runningReasoner.dispose();
                    runningReasoner = null;
                } else {
                    runningReasoner.flush();
                }
            }
            if (runningReasoner == null) {
                runningReasoner = ReasonerUtilities.createReasoner(applyReasonerFilters(ontology),
                        currentReasonerFactory, reasonerProgressMonitor);
                reasonerChanged = true;
            }
            if (runningReasoner == null) {
                classificationInProgress = false;
                LOGGER.warn("An error occurred during reasoner initialisation");
            }
            return reasonerChanged;
        }

        public void precompute() {
            Set<InferenceType> precomputeThisRun = EnumSet.noneOf(InferenceType.class);
            precomputeThisRun.addAll(precompute);
            precomputeThisRun.retainAll(runningReasoner.getPrecomputableInferenceTypes());
            if (!precomputeThisRun.isEmpty()) {
                LOGGER.info("Pre-computing inferences:");
                for (InferenceType type : precompute) {
                    LOGGER.info("    - {}", type);
                }
                runningReasoner.precomputeInferences(precomputeThisRun.toArray(new InferenceType[0]));
            }
        }

        public void installRunningReasoner(boolean inconsistencyFound, boolean reasonerChanged) {
            synchronized (reasonerMap) {
                reasonerMap.put(ontology, runningReasoner);
                runningReasoner = null;
                classificationInProgress = false;
            }
            if (reasonerChanged) {
                SwingUtilities.invokeLater(() -> manager.fireEvent(EventType.REASONER_CHANGED));
            }
            fireReclassified();
            if (inconsistencyFound) {
                InconsistentOntologyManager.get(manager).explain();
            }
        }
    }

}
