package org.protege.editor.owl.ui.frame.objectproperty;

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
 * Date: 29-Jan-2007<br><br>
 */
public class OWLObjectPropertyDomainFrameSection
        extends AbstractPropertyDomainFrameSection<OWLObjectProperty, OWLObjectPropertyDomainAxiom> {

    public OWLObjectPropertyDomainFrameSection(OWLEditorKit editorKit, OWLFrame<OWLObjectProperty> owlObjectPropertyOWLFrame) {
        super(editorKit, owlObjectPropertyOWLFrame);
    }

    @Override
    protected OWLObjectPropertyDomainAxiom createAxiom(OWLClassExpression object) {
        return getOWLDataFactory().getOWLObjectPropertyDomainAxiom(getRootObject(), object);
    }

    @Override
    protected OWLObjectPropertyDomainFrameSectionRow createFrameSectionRow(OWLObjectPropertyDomainAxiom axiom,
                                                                           OWLOntology ontology) {
        return new OWLObjectPropertyDomainFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), axiom);
    }

    @Override
    protected Stream<OWLObjectPropertyDomainAxiom> axioms(OWLOntology ontology) {
        return ontology.objectPropertyDomainAxioms(getRootObject());
    }

    @Override
    protected NodeSet<OWLClass> getInferredDomains() {
        OWLReasoner reasoner = getOWLModelManager().getReasoner();
        OWLObjectProperty p = getRootObject();
        OWLDataFactory factory = getOWLModelManager().getOWLOntologyManager().getOWLDataFactory();
        if (p.equals(factory.getOWLTopObjectProperty())) {
            return new OWLClassNodeSet(reasoner.getTopClassNode());
        }
        OWLClassExpression domain = factory.getOWLObjectSomeValuesFrom(p, factory.getOWLThing());
        Node<OWLClass> domainNode = reasoner.getEquivalentClasses(domain);
        if (domainNode != null && domainNode.getSize() != 0) {
            return new OWLClassNodeSet(domainNode);
        }
        return reasoner.getObjectPropertyDomains(getRootObject(), true);
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_DOMAINS;
    }

    @Override
    protected void infer() {
        if (!getOWLModelManager().getReasoner().isConsistent()) {
            return;
        }
        super.infer();
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLObjectPropertyDomainAxiom) {
            return ((OWLObjectPropertyDomainAxiom) axiom).getProperty().equals(getRootObject());
        }
        return false;
    }
}
