package org.github.owlcs.owlapi.reasoner.impl;

import com.github.owlcs.ontapi.owlapi.InternalizedEntities;
import org.semanticweb.owlapi.model.OWLDataProperty;

import java.util.Collection;
import java.util.Optional;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLDataPropertyNode.java'>org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNode</a>
 */
@SuppressWarnings("WeakerAccess")
public class OWLDataPropertyNode extends DefaultNode<OWLDataProperty> {

    private static final OWLDataPropertyNode TOP_DATA_NODE =
            new OWLDataPropertyNode(InternalizedEntities.OWL_TOP_DATA_PROPERTY);
    private static final OWLDataPropertyNode BOTTOM_DATA_NODE =
            new OWLDataPropertyNode(InternalizedEntities.OWL_BOTTOM_DATA_PROPERTY);

    /**
     * Default constructor.
     */
    public OWLDataPropertyNode() {
        super();
    }

    /**
     * @param entity the entity to be contained
     */
    public OWLDataPropertyNode(OWLDataProperty entity) {
        super(entity);
    }

    /**
     * @param entities the entities to be contained
     */
    public OWLDataPropertyNode(Collection<OWLDataProperty> entities) {
        super(entities);
    }

    /**
     * @return singleton top node
     */
    public static OWLDataPropertyNode getTopNode() {
        return TOP_DATA_NODE;
    }

    /**
     * @return singleton bottom node
     */
    public static OWLDataPropertyNode getBottomNode() {
        return BOTTOM_DATA_NODE;
    }

    @Override
    protected Optional<OWLDataProperty> getTopEntity() {
        return Optional.of(InternalizedEntities.OWL_TOP_DATA_PROPERTY);
    }

    @Override
    protected Optional<OWLDataProperty> getBottomEntity() {
        return Optional.of(InternalizedEntities.OWL_BOTTOM_DATA_PROPERTY);
    }
}
