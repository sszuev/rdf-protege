package org.protege.editor.owl.model.inference;

import org.github.owlcs.ontapi.OWLManager;
import org.github.owlcs.owlapi.reasoner.impl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.Version;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Apr-2007<br><br>
 */
@ParametersAreNonnullByDefault
public class NoOpReasoner implements OWLReasoner {

    private final OWLOntology rootOntology;
    private final OWLClass OWL_THING;
    private final OWLClass OWL_NOTHING;
    private final OWLObjectProperty OWL_TOP_OBJECT_PROPERTY;
    private final OWLObjectProperty OWL_BOTTOM_OBJECT_PROPERTY;
    private final OWLDataProperty OWL_TOP_DATA_PROPERTY;
    private final OWLDataProperty OWL_BOTTOM_DATA_PROPERTY;

    public NoOpReasoner(OWLOntology rootOntology) {
        this(rootOntology, OWLManager.getOWLDataFactory());
    }

    protected NoOpReasoner(OWLOntology rootOntology, OWLDataFactory df) {
        this.rootOntology = rootOntology;
        OWL_THING = df.getOWLThing();
        OWL_NOTHING = df.getOWLNothing();
        OWL_TOP_OBJECT_PROPERTY = df.getOWLTopObjectProperty();
        OWL_BOTTOM_OBJECT_PROPERTY = df.getOWLBottomObjectProperty();
        OWL_TOP_DATA_PROPERTY = df.getOWLTopDataProperty();
        OWL_BOTTOM_DATA_PROPERTY = df.getOWLBottomDataProperty();
    }

    @Override
    public OWLOntology getRootOntology() {
        return rootOntology;
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        return Collections.emptySet();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        return Collections.emptySet();
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        return Collections.emptyList();
    }

    @Override
    public BufferingMode getBufferingMode() {
        return BufferingMode.NON_BUFFERING;
    }

    @Override
    public long getTimeOut() {
        return 0;
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        return true;
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes) {
    }

    @Override
    public void interrupt() {

    }

    @Override
    public void dispose() {
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
        return true;
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {
        return new OWLClassNodeSet();
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {
        return Collections.emptySet();
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) {
        return ce.isAnonymous() ? NodeFactory.getOWLClassNode() : NodeFactory.getOWLClassNode(ce.asOWLClass());
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {
        return pe.isAnonymous() ? NodeFactory.getOWLDataPropertyNode() : NodeFactory.getOWLDataPropertyNode(pe.asOWLDataProperty());
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) {
        return pe.isAnonymous() ? NodeFactory.getOWLObjectPropertyNode() : NodeFactory.getOWLObjectPropertyNode(pe.asOWLObjectProperty());
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
        return new OWLNamedIndividualNodeSet();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {
        return NodeFactory.getOWLObjectPropertyNode();
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
        return new OWLClassNodeSet();
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {
        return new OWLClassNodeSet();
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind,
                                                               OWLObjectPropertyExpression pe) {
        return new OWLNamedIndividualNodeSet();
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {
        return NodeFactory.getOWLNamedIndividualNode(ind);
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
        return new OWLClassNodeSet();
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {
        return new OWLDataPropertyNodeSet();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe,
                                                                       boolean direct) {
        return new OWLObjectPropertyNodeSet();
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) {
        return new OWLClassNodeSet();
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {
        return new OWLDataPropertyNodeSet();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe,
                                                                         boolean direct) {
        return new OWLObjectPropertyNodeSet();
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {
        return new OWLClassNodeSet();
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return NodeFactory.getOWLClassNode();
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        return false;
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        return false;
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return false;
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) {
        return true;
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        return NodeFactory.getOWLClassNode(OWL_NOTHING);
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return NodeFactory.getOWLDataPropertyNode(OWL_BOTTOM_DATA_PROPERTY);
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return NodeFactory.getOWLObjectPropertyNode(OWL_BOTTOM_OBJECT_PROPERTY);
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {
        return new OWLNamedIndividualNodeSet();
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        return new OWLClassNodeSet();
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {
        return new OWLDataPropertyNodeSet();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) {
        return new OWLObjectPropertyNodeSet();
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return IndividualNodeSetPolicy.BY_SAME_AS;
    }

    @Override
    public String getReasonerName() {
        return "Prot\u00E9g\u00E9 Null Reasoner";
    }

    @Override
    public Version getReasonerVersion() {
        return new Version(1, 0, 0, 0);
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        return NodeFactory.getOWLClassNode(OWL_THING);
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return NodeFactory.getOWLDataPropertyNode(OWL_TOP_DATA_PROPERTY);
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return NodeFactory.getOWLObjectPropertyNode(OWL_TOP_OBJECT_PROPERTY);
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return FreshEntityPolicy.ALLOW;
    }
}
