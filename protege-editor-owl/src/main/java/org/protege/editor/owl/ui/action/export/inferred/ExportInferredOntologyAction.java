package org.protege.editor.owl.ui.action.export.inferred;

import org.github.owlcs.ontapi.OWLManager;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.protege.editor.owl.model.inference.ReasonerUtilities;
import org.protege.editor.owl.model.inference.VacuousAxiomVisitor;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 09-Aug-2007<br><br>
 */
public class ExportInferredOntologyAction extends ProtegeOWLAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportInferredOntologyAction.class);
    private static final long serialVersionUID = 5000834279491773432L;

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();
            ReasonerStatus status = reasonerManager.getReasonerStatus();
            if (status != ReasonerStatus.INITIALIZED && status != ReasonerStatus.OUT_OF_SYNC) {
                ReasonerUtilities.warnUserIfReasonerIsNotConfigured(getOWLWorkspace(), reasonerManager);
                return;
            }
            final ExportInferredOntologyWizard wizard = new ExportInferredOntologyWizard(getOWLEditorKit());
            int ret = wizard.showModalDialog();
            if (ret != Wizard.FINISH_RETURN_CODE) {
                return;
            }
            new Thread(new ExportTask(wizard), "Export Inferred Axioms").start();
        } catch (OWLOntologyCreationException e1) {
            JOptionPane.showMessageDialog(getWorkspace(), "Could not create ontology:\n" + e1.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void initialise() throws Exception {
    }

    @Override
    public void dispose() throws Exception {
    }

    private class ExportTask implements Runnable {
        private final ExportInferredOntologyWizard wizard;
        private final OWLOntologyManager outputManager;
        private final List<InferredAxiomGenerator<? extends OWLAxiom>> inferredAxiomGenerators;
        private final InferredOntologyGenerator inferredOntologyGenerator;
        private final OWLOntology exportedOntology;

        private ProgressMonitor monitor;
        private Set<InferenceType> precompute;
        private int taskCount;

        public ExportTask(ExportInferredOntologyWizard wizard) throws OWLOntologyCreationException {
            this.wizard = wizard;
            inferredAxiomGenerators = wizard.getInferredAxiomGenerators();
            outputManager = OWLManager.createOWLOntologyManager();
            inferredOntologyGenerator = new InferredOntologyGenerator(getOWLModelManager().getReasoner(), inferredAxiomGenerators);
            exportedOntology = outputManager.createOntology(wizard.getOntologyID());

            taskCount = inferredAxiomGenerators.size() + 1;
            if (wizard.isIncludeAnnotations()) {
                taskCount += 1;
            }
            if (wizard.isIncludeAssertedLogicalAxioms()) {
                taskCount += 1;
            }
            taskCount += 3; // classify, apply changes and save the ontology...
        }

        @Override
        public void run() {
            try {
                setupMonitor();

                adjustProgress("Initializing Reasoner", 0);
                precompute();

                inferredOntologyGenerator.fillOntology(outputManager.getOWLDataFactory(), exportedOntology);

                int currentTask = inferredAxiomGenerators.size();
                List<OWLOntologyChange> changes = new ArrayList<>();

                adjustProgress("Deleting trivial inferences", ++currentTask);
                deleteTrivialAxioms(changes);

                if (wizard.isIncludeAnnotations()) {
                    adjustProgress("Adding annotations", ++currentTask);
                    addAnnotations(changes);
                }
                if (wizard.isIncludeAssertedLogicalAxioms()) {
                    adjustProgress("Adding asserted axioms", ++currentTask);
                    addAsserted(changes);
                }

                adjustProgress("Applying extra changes", ++currentTask);
                outputManager.applyChanges(changes);

                adjustProgress("Saving...", ++currentTask);
                outputManager.saveOntology(exportedOntology, wizard.getFormat(), IRI.create(wizard.getPhysicalURL()));


                monitor.close();

                JOptionPane.showMessageDialog(getWorkspace(),
                        "The inferred axioms have been exported as an ontology to \n" + wizard.getPhysicalURL(),
                        "Export finished",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (ExportCancelledException cancelled) {
                JOptionPane.showMessageDialog(getWorkspace(),
                        "The export operation has been cancelled at the users request", "Export aborted",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Throwable t) {
                LOGGER.warn("An error occurred whilst exporting the inferred ontology: '{}'", t.getMessage(), t);
            }
        }

        private void setupMonitor() {
            monitor = new ProgressMonitor(getOWLWorkspace(), "Exporting Inferred Ontology", "Initializing", 0, taskCount);
            precompute = EnumSet.noneOf(InferenceType.class);
            int task = 1;
            for (InferredAxiomGenerator<? extends OWLAxiom> generator : inferredAxiomGenerators) {
                ((MonitoredInferredAxiomGenerator<? extends OWLAxiom>) generator).setProgressMonitor(monitor, task++);
                precompute.addAll(ExportInferredOntologyPanel.getInferenceType(generator));
            }
        }

        private void adjustProgress(String taskDescription, int taskCount) {
            if (monitor.isCanceled()) {
                throw new ExportCancelledException();
            }
            monitor.setNote(taskDescription);
            monitor.setProgress(taskCount);
        }

        private void precompute() {
            Set<InferenceType> precomputeNow = EnumSet.copyOf(precompute);
            OWLReasoner reasoner = getOWLModelManager().getReasoner();
            if (!reasoner.getPendingChanges().isEmpty()) {
                reasoner.flush();
            }
            precomputeNow.retainAll(reasoner.getPrecomputableInferenceTypes());
            for (InferenceType inference : precompute) {
                if (reasoner.isPrecomputed(inference)) {
                    precomputeNow.remove(inference);
                }
            }
            if (!precomputeNow.isEmpty()) {
                reasoner.precomputeInferences(precomputeNow.toArray(new InferenceType[0]));
            }
        }

        private void deleteTrivialAxioms(List<OWLOntologyChange> changes) {
            exportedOntology.axioms()
                    .filter(x -> VacuousAxiomVisitor.isVacuousAxiom(x) || VacuousAxiomVisitor.involvesInverseSquared(x))
                    .map(axiom -> new RemoveAxiom(exportedOntology, axiom))
                    .forEach(changes::add);
        }

        private void addAnnotations(List<OWLOntologyChange> changes) {
            getOWLModelManager().getReasoner().getRootOntology().importsClosure().forEach(o -> {
                o.annotations().map(a -> new AddOntologyAnnotation(exportedOntology, a)).forEach(changes::add);
                o.axioms(AxiomType.ANNOTATION_ASSERTION).map(a -> new AddAxiom(exportedOntology, a)).forEach(changes::add);
            });
        }

        private void addAsserted(List<OWLOntologyChange> changes) {
            getOWLModelManager().getReasoner().getRootOntology()
                    .importsClosure()
                    .flatMap(HasLogicalAxioms::logicalAxioms)
                    .forEach(ax -> {
                        if (ax.isAnnotated() && exportedOntology.containsAxiom(ax.getAxiomWithoutAnnotations())) {
                            changes.add(new RemoveAxiom(exportedOntology, ax.getAxiomWithoutAnnotations()));
                        }
                        changes.add(new AddAxiom(exportedOntology, ax));
                    });
        }
    }
}
