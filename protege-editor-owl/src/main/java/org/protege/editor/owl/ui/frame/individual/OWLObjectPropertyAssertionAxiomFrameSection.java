package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyIndividualPairEditor2;
import org.protege.editor.owl.ui.frame.AbstractInferFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLObjectPropertyIndividualPair;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 30-Jan-2007<br><br>
 */
public class OWLObjectPropertyAssertionAxiomFrameSection
        extends AbstractInferFrameSection<OWLIndividual, OWLObjectPropertyAssertionAxiom, OWLObjectPropertyIndividualPair> {

    public static final String LABEL = "Object property assertions";

    private final Set<OWLObjectPropertyAssertionAxiom> added = new HashSet<>();

    private final OWLObjectPropertyIndividualPairEditor2 editor;

    public OWLObjectPropertyAssertionAxiomFrameSection(OWLEditorKit owlEditorKit, OWLFrame<? extends OWLIndividual> frame) {
        super(owlEditorKit, LABEL, "Object property assertion", frame);
        editor = new OWLObjectPropertyIndividualPairEditor2(getOWLEditorKit());
    }

    @Override
    protected void refill(OWLOntology ontology) {
        added.clear();
        OWLIndividual root = getRootObject();
        ontology.objectPropertyAssertionAxioms(root).forEach(ax -> {
            addRow(new OWLObjectPropertyAssertionAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax));
            added.add(ax);
        });
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        OWLDataFactory df = getOWLDataFactory();
        OWLIndividual root = getRootObject();
        if (root.isAnonymous()) {
            return;
        }
        OWLReasoner r = getReasoner();
        r.getRootOntology().objectPropertiesInSignature(Imports.INCLUDED)
                .filter(p -> !p.equals(df.getOWLTopObjectProperty()))
                .forEach(p -> {
                    NodeSet<OWLNamedIndividual> values = r.getObjectPropertyValues(root.asOWLNamedIndividual(), p);
                    values.entities().map(ind -> df.getOWLObjectPropertyAssertionAxiom(p, root, ind))
                            .filter(ax -> !added.contains(ax))
                            .map(ax -> new OWLObjectPropertyAssertionAxiomFrameSectionRow(getOWLEditorKit(), this, null, root, ax))
                            .forEach(this::addRow);
                });
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_ASSERTIONS;
    }

    @Override
    protected OWLObjectPropertyAssertionAxiom createAxiom(OWLObjectPropertyIndividualPair object) {
        return getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(object.getProperty(),
                getRootObject(), object.getIndividual());
    }

    @Override
    public OWLObjectEditor<OWLObjectPropertyIndividualPair> getObjectEditor() {
        return editor;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
            return ((OWLObjectPropertyAssertionAxiom) axiom).getSubject().equals(getRootObject());
        }
        return false;
    }

}
