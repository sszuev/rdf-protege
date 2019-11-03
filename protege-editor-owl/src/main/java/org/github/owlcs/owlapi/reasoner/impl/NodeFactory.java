package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.Set;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/NodeFactory.java'>NodeFactory</a>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class NodeFactory {

    private NodeFactory() {
    }

    /**
     * @return a class node
     */
    public static DefaultNode<OWLClass> getOWLClassNode() {
        return new OWLClassNode();
    }

    /**
     * @param cls a class to be included in the node
     * @return a class node with one element
     */
    public static DefaultNode<OWLClass> getOWLClassNode(OWLClass cls) {
        return new OWLClassNode(checkNotNull(cls, "cls cannot be null"));
    }

    /**
     * @param classes set of classes to be included
     * @return a class node with some elements
     */
    public static DefaultNode<OWLClass> getOWLClassNode(Set<OWLClass> classes) {
        return new OWLClassNode(checkNotNull(classes, "clses cannot be null"));
    }

    /**
     * @return the top class node
     */
    public static DefaultNode<OWLClass> getOWLClassTopNode() {
        return OWLClassNode.getTopNode();
    }

    /**
     * @return the bottom class node
     */
    public static DefaultNode<OWLClass> getOWLClassBottomNode() {
        return OWLClassNode.getBottomNode();
    }

    /**
     * @return an object property node
     */
    public static DefaultNode<OWLObjectPropertyExpression> getOWLObjectPropertyNode() {
        return new OWLObjectPropertyNode();
    }

    /**
     * @param prop a property to be added
     * @return an object property node with one element
     */
    public static DefaultNode<OWLObjectPropertyExpression> getOWLObjectPropertyNode(OWLObjectPropertyExpression prop) {
        return new OWLObjectPropertyNode(prop);
    }

    /**
     * @param properties some properties to be added
     * @return an object property node with some elements
     */
    public static DefaultNode<OWLObjectPropertyExpression> getOWLObjectPropertyNode(Set<OWLObjectPropertyExpression> properties) {
        return new OWLObjectPropertyNode(properties);
    }

    /**
     * @return the top object property node
     */
    public static DefaultNode<OWLObjectPropertyExpression> getOWLObjectPropertyTopNode() {
        return OWLObjectPropertyNode.getTopNode();
    }

    /**
     * @return the bottom object property node
     */
    public static DefaultNode<OWLObjectPropertyExpression> getOWLObjectPropertyBottomNode() {
        return OWLObjectPropertyNode.getBottomNode();
    }

    /**
     * @return a data property node
     */
    public static DefaultNode<OWLDataProperty> getOWLDataPropertyNode() {
        return new OWLDataPropertyNode();
    }

    /**
     * @param prop a property to be added
     * @return a data property node with one element
     */
    public static DefaultNode<OWLDataProperty> getOWLDataPropertyNode(OWLDataProperty prop) {
        return new OWLDataPropertyNode(prop);
    }

    /**
     * @param properties some properties to be added
     * @return a data property node with some elements
     */
    public static DefaultNode<OWLDataProperty> getOWLDataPropertyNode(Set<OWLDataProperty> properties) {
        return new OWLDataPropertyNode(properties);
    }

    /**
     * @return the top data property node
     */
    public static DefaultNode<OWLDataProperty> getOWLDataPropertyTopNode() {
        return OWLDataPropertyNode.getTopNode();
    }

    /**
     * @return the bottom data property node
     */
    public static DefaultNode<OWLDataProperty> getOWLDataPropertyBottomNode() {
        return OWLDataPropertyNode.getBottomNode();
    }

    /**
     * @return an individual node
     */
    public static DefaultNode<OWLNamedIndividual> getOWLNamedIndividualNode() {
        return new OWLNamedIndividualNode();
    }

    /**
     * @param ind an individual to be added
     * @return an individual node with one element
     */
    public static DefaultNode<OWLNamedIndividual> getOWLNamedIndividualNode(OWLNamedIndividual ind) {
        return new OWLNamedIndividualNode(ind);
    }

    /**
     * @param inds some individuals to be added
     * @return an individual node containing some individuals
     */
    public static DefaultNode<OWLNamedIndividual> getOWLNamedIndividualNode(Set<OWLNamedIndividual> inds) {
        return new OWLNamedIndividualNode(inds);
    }
}

