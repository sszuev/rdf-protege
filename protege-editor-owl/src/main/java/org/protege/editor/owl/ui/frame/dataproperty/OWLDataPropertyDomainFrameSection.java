package org.protege.editor.owl.ui.frame.dataproperty;

import org.github.owlcs.owlapi.reasoner.impl.OWLClassNodeSet;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.property.AbstractPropertyDomainFrameSection;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLDataPropertyDomainFrameSection
        extends AbstractPropertyDomainFrameSection<OWLDataProperty, OWLDataPropertyDomainAxiom> {

    public OWLDataPropertyDomainFrameSection(OWLEditorKit kit, OWLFrame<OWLDataProperty> frame) {
        super(kit, frame);
    }

    @Override
    protected OWLDataPropertyDomainAxiom createAxiom(OWLClassExpression object) {
        return getOWLDataFactory().getOWLDataPropertyDomainAxiom(getRootObject(), object);
    }

    @Override
    protected OWLDataPropertyDomainFrameSectionRow createFrameSectionRow(OWLDataPropertyDomainAxiom axiom,
                                                                         OWLOntology ontology) {
        return new OWLDataPropertyDomainFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), axiom);
    }

    @Override
    protected Stream<OWLDataPropertyDomainAxiom> axioms(OWLOntology ontology) {
        return ontology.dataPropertyDomainAxioms(getRootObject());
    }

    @Override
    protected NodeSet<OWLClass> getInferredDomains() {
        OWLReasoner reasoner = getOWLModelManager().getReasoner();
        OWLDataProperty p = getRootObject();
        OWLDataFactory factory = getOWLModelManager().getOWLOntologyManager().getOWLDataFactory();
        if (p.equals(factory.getOWLTopDataProperty())) {
            return new OWLClassNodeSet(reasoner.getTopClassNode());
        }
        OWLClassExpression domain = factory.getOWLDataSomeValuesFrom(p, factory.getTopDatatype());
        Node<OWLClass> node = reasoner.getEquivalentClasses(domain);
        if (node != null && node.getSize() != 0) {
            return new OWLClassNodeSet(node);
        }
        return reasoner.getDataPropertyDomains(getRootObject(), true);
    }

    @Override
    protected void infer() {
        if (!isConsistent()) {
            return;
        }
        super.infer();
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_DATATYPE_PROPERTY_DOMAINS;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLDataPropertyDomainAxiom &&
                ((OWLDataPropertyDomainAxiom) change.getAxiom()).getProperty().equals(getRootObject());
    }

}
