package org.github.owlcs.owlapi.reasoner.impl;

import com.github.owlcs.ontapi.owlapi.InternalizedEntities;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.Collection;
import java.util.Optional;

/**
 * Copy-pasted from OWL-API-impl.
 *
 * @author Matthew Horridge, The University of Manchester, Information Management Group
 * @see <a href='https://github.com/owlcs/owlapi/blob/version5/impl/src/main/java/org/semanticweb/owlapi/reasoner/impl/OWLObjectPropertyNode.java'>org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode</a>
 */
@SuppressWarnings("WeakerAccess")
public class OWLObjectPropertyNode extends DefaultNode<OWLObjectPropertyExpression> {

    private static final OWLObjectPropertyNode TOP_OBJECT_NODE =
            new OWLObjectPropertyNode(InternalizedEntities.OWL_TOP_OBJECT_PROPERTY);
    private static final OWLObjectPropertyNode BOTTOM_OBJECT_NODE =
            new OWLObjectPropertyNode(InternalizedEntities.OWL_BOTTOM_OBJECT_PROPERTY);

    /**
     * Default constructor.
     */
    public OWLObjectPropertyNode() {
        super();
    }

    /**
     * @param entity property to include
     */
    public OWLObjectPropertyNode(OWLObjectPropertyExpression entity) {
        super(entity);
    }

    /**
     * @param entities properties to include
     */
    public OWLObjectPropertyNode(Collection<OWLObjectPropertyExpression> entities) {
        super(entities);
    }

    /**
     * @return top node
     */
    public static OWLObjectPropertyNode getTopNode() {
        return TOP_OBJECT_NODE;
    }

    /**
     * @return bottom node
     */
    public static OWLObjectPropertyNode getBottomNode() {
        return BOTTOM_OBJECT_NODE;
    }

    @Override
    protected Optional<OWLObjectPropertyExpression> getTopEntity() {
        return Optional.of(InternalizedEntities.OWL_TOP_OBJECT_PROPERTY);
    }

    @Override
    protected Optional<OWLObjectPropertyExpression> getBottomEntity() {
        return Optional.of(InternalizedEntities.OWL_BOTTOM_OBJECT_PROPERTY);
    }
}

