package org.protege.editor.owl.ui.frame;

import org.github.owlcs.ontapi.OWLManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14-Oct-2007<br><br>
 */
public class InferredAxiomsFrameSection extends AbstractOWLFrameSection<OWLOntology, OWLAxiom, OWLAxiom> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InferredAxiomsFrameSection.class);

    public InferredAxiomsFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLOntology> frame) {
        super(editorKit, "Inferred axioms", "Inferred axiom", frame);
    }

    @Override
    protected OWLAxiom createAxiom(OWLAxiom object) {
        return object;
    }

    @Override
    public OWLObjectEditor<OWLAxiom> getObjectEditor() {
        return null;
    }

    @Override
    protected void refill(OWLOntology ontology) {
    }

    @Override
    protected void refillInferred() {
        OWLModelManager m = getOWLModelManager();
        OWLReasoner r = m.getReasoner();
        try {
            for (OWLClass cls : r.getUnsatisfiableClasses()) {
                if (!cls.isOWLNothing()) {
                    OWLAxiom unsatAx = getOWLDataFactory().getOWLSubClassOfAxiom(cls,
                            getOWLDataFactory().getOWLNothing());
                    addRow(new InferredAxiomsFrameSectionRow(getOWLEditorKit(), this, null, getRootObject(), unsatAx));
                }
            }
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLOntology inferredOnt = man.createOntology(IRI.create("http://another.com/ontology" + System.currentTimeMillis()));
            InferredOntologyGenerator ontGen = new InferredOntologyGenerator(r, new ArrayList<>());
            ontGen.addGenerator(new InferredSubClassAxiomGenerator());
            ontGen.addGenerator(new InferredClassAssertionAxiomGenerator());
            ontGen.addGenerator(new InferredSubObjectPropertyAxiomGenerator());
            ontGen.addGenerator(new InferredSubDataPropertyAxiomGenerator());
            ontGen.fillOntology(man.getOWLDataFactory(), inferredOnt);

            inferredOnt.axioms().sorted().forEach(ax -> {
                if (m.getActiveOntologies().stream().noneMatch(o -> o.containsAxiom(ax))) {
                    addInferredRowIfNontrivial(new InferredAxiomsFrameSectionRow(getOWLEditorKit(),
                            InferredAxiomsFrameSection.this, null, getRootObject(), ax));
                }
            });
        } catch (Exception e) {
            LOGGER.error("Unknown error", e);
        }
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return false;
    }

    @Override
    public Comparator<OWLFrameSectionRow<OWLOntology, OWLAxiom, OWLAxiom>> getRowComparator() {
        return (o1, o2) -> {
            int diff = o1.getAxiom().compareTo(o2.getAxiom());
            if (diff != 0) {
                return diff;
            }
            if (o1.getOntology() == null && o2.getOntology() == null) {
                return 0;
            }
            if (o1.getOntology() == null) {
                return -1;
            }
            if (o2.getOntology() == null) {
                return +1;
            }
            return o1.getOntology().compareTo(o2.getOntology());
        };
    }
}
