package org.github.owlcs.owlapi.reasoner.impl;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.Collection;
import java.util.Optional;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLNamedIndividualNode.java'>org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNode</a>
 */
@SuppressWarnings("WeakerAccess")
public class OWLNamedIndividualNode extends DefaultNode<OWLNamedIndividual> {

    /**
     * Default constructor.
     */
    public OWLNamedIndividualNode() {
        super();
    }

    /**
     * @param entity individual to include
     */
    public OWLNamedIndividualNode(OWLNamedIndividual entity) {
        super(entity);
    }

    /**
     * @param entities individuals to include
     */
    public OWLNamedIndividualNode(Collection<OWLNamedIndividual> entities) {
        super(entities);
    }

    @Override
    protected Optional<OWLNamedIndividual> getTopEntity() {
        return Optional.empty();
    }

    @Override
    protected Optional<OWLNamedIndividual> getBottomEntity() {
        return Optional.empty();
    }
}
