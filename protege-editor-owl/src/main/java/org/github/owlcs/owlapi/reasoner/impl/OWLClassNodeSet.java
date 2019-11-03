package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.Node;

import java.util.Set;

/**
 * A node set of OWL classes.
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLClassNodeSet.java'>org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet</a>
 */
@SuppressWarnings("unused")
public class OWLClassNodeSet extends DefaultNodeSet<OWLClass> {

    /**
     * Default constructor.
     */
    public OWLClassNodeSet() {
        super();
    }

    /**
     * @param entity the entity to be contained
     */
    public OWLClassNodeSet(OWLClass entity) {
        super(entity);
    }

    /**
     * @param owlClassNode the node to be contained
     */
    public OWLClassNodeSet(Node<OWLClass> owlClassNode) {
        super(owlClassNode);
    }

    @Override
    protected DefaultNode<OWLClass> getNode(OWLClass entity) {
        return NodeFactory.getOWLClassNode(entity);
    }

    @Override
    protected DefaultNode<OWLClass> getNode(Set<OWLClass> entities) {
        return NodeFactory.getOWLClassNode(entities);
    }
}

