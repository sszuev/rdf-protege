package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.Node;

import java.util.Set;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLNamedIndividualNodeSet.java'>org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet</a>
 */
public class OWLNamedIndividualNodeSet extends DefaultNodeSet<OWLNamedIndividual> {

    /**
     * Default constructor.
     */
    public OWLNamedIndividualNodeSet() {
        super();
    }

    /**
     * @param entity individual to include
     */
    public OWLNamedIndividualNodeSet(OWLNamedIndividual entity) {
        super(entity);
    }

    /**
     * @param owlNamedIndividualNode node to include
     */
    public OWLNamedIndividualNodeSet(Node<OWLNamedIndividual> owlNamedIndividualNode) {
        super(owlNamedIndividualNode);
    }

    /**
     * @param nodes nodes to include
     */
    public OWLNamedIndividualNodeSet(Set<Node<OWLNamedIndividual>> nodes) {
        super(nodes);
    }

    @Override
    protected DefaultNode<OWLNamedIndividual> getNode(OWLNamedIndividual entity) {
        return NodeFactory.getOWLNamedIndividualNode(entity);
    }

    @Override
    protected DefaultNode<OWLNamedIndividual> getNode(Set<OWLNamedIndividual> entities) {
        return NodeFactory.getOWLNamedIndividualNode(entities);
    }
}

