package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.reasoner.Node;

import java.util.Set;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLDataPropertyNodeSet.java'>org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet</a>
 */
@SuppressWarnings("unused")
public class OWLDataPropertyNodeSet extends DefaultNodeSet<OWLDataProperty> {

    /**
     * Default constructor.
     */
    public OWLDataPropertyNodeSet() {
        super();
    }

    /**
     * @param entity the entity to be contained
     */
    public OWLDataPropertyNodeSet(OWLDataProperty entity) {
        super(entity);
    }

    /**
     * @param owlDataPropertyNode the node to be contained
     */
    public OWLDataPropertyNodeSet(Node<OWLDataProperty> owlDataPropertyNode) {
        super(owlDataPropertyNode);
    }

    /**
     * @param nodes the nodes to be contained
     */
    public OWLDataPropertyNodeSet(Set<Node<OWLDataProperty>> nodes) {
        super(nodes);
    }

    @Override
    protected DefaultNode<OWLDataProperty> getNode(OWLDataProperty entity) {
        return NodeFactory.getOWLDataPropertyNode(entity);
    }

    @Override
    protected DefaultNode<OWLDataProperty> getNode(Set<OWLDataProperty> entities) {
        return NodeFactory.getOWLDataPropertyNode(entities);
    }
}

