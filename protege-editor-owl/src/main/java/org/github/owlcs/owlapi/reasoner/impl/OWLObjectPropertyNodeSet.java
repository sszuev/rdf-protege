package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.Node;

import java.util.Set;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLObjectPropertyNodeSet.java'>org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet</a>
 */
public class OWLObjectPropertyNodeSet extends DefaultNodeSet<OWLObjectPropertyExpression> {

    /**
     * Default constructor.
     */
    public OWLObjectPropertyNodeSet() {
        super();
    }

    /**
     * @param entity property to include
     */
    public OWLObjectPropertyNodeSet(OWLObjectPropertyExpression entity) {
        super(entity);
    }

    /**
     * @param owlObjectPropertyNode property node to include
     */
    public OWLObjectPropertyNodeSet(Node<OWLObjectPropertyExpression> owlObjectPropertyNode) {
        super(owlObjectPropertyNode);
    }

    /**
     * @param nodes nodes to include
     */
    public OWLObjectPropertyNodeSet(Set<Node<OWLObjectPropertyExpression>> nodes) {
        super(nodes);
    }

    @Override
    protected DefaultNode<OWLObjectPropertyExpression> getNode(OWLObjectPropertyExpression entity) {
        return NodeFactory.getOWLObjectPropertyNode(entity);
    }

    @Override
    protected DefaultNode<OWLObjectPropertyExpression> getNode(Set<OWLObjectPropertyExpression> entities) {
        return NodeFactory.getOWLObjectPropertyNode(entities);
    }
}
