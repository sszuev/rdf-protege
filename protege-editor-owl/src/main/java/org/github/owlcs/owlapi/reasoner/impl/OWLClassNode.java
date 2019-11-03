package org.github.owlcs.owlapi.reasoner.impl;

import com.github.owlcs.ontapi.owlapi.InternalizedEntities;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Collection;
import java.util.Optional;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLClassNode.java'>org.semanticweb.owlapi.reasoner.impl.OWLClassNode</a>
 */
@SuppressWarnings("WeakerAccess")
public class OWLClassNode extends DefaultNode<OWLClass> {

    private static final OWLClassNode TOP_NODE = new OWLClassNode(InternalizedEntities.OWL_THING);
    private static final OWLClassNode BOTTOM_NODE = new OWLClassNode(InternalizedEntities.OWL_NOTHING);

    /**
     * @param entity the class to be contained
     */
    public OWLClassNode(OWLClass entity) {
        super(entity);
    }

    /**
     * @param entities the classes to be contained
     */
    public OWLClassNode(Collection<OWLClass> entities) {
        super(entities);
    }

    /**
     * Default constructor.
     */
    public OWLClassNode() {
        super();
    }

    /**
     * @return singleton top node
     */
    public static OWLClassNode getTopNode() {
        return TOP_NODE;
    }

    /**
     * @return singleton bottom node
     */
    public static OWLClassNode getBottomNode() {
        return BOTTOM_NODE;
    }

    @Override
    protected Optional<OWLClass> getTopEntity() {
        return Optional.of(InternalizedEntities.OWL_THING);
    }

    @Override
    protected Optional<OWLClass> getBottomEntity() {
        return Optional.of(InternalizedEntities.OWL_NOTHING);
    }
}
