package org.protege.editor.owl.model.hierarchy.property;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.hierarchy.OWLObjectPropertyHierarchyProvider;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 15, 2008<br><br>
 */
public class InferredObjectPropertyHierarchyProvider extends OWLObjectPropertyHierarchyProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(InferredObjectPropertyHierarchyProvider.class);
    public static final String ID = "inferredObjectPropertyHierarchyProvider";

    private final OWLModelManagerListener listener = e -> {
        if (e.isOneOf(EventType.REASONER_CHANGED
                , EventType.ACTIVE_ONTOLOGY_CHANGED
                , EventType.ONTOLOGY_CLASSIFIED
                , EventType.ONTOLOGY_RELOADED)) {
            fireHierarchyChanged();
        }
    };
    private final OWLModelManager mngr;

    public InferredObjectPropertyHierarchyProvider(OWLModelManager mngr) {
        super(mngr.getOWLOntologyManager());
        this.mngr = mngr;
        mngr.addListener(listener);
    }

    protected OWLReasoner getReasoner() {
        return mngr.getOWLReasonerManager().getCurrentReasoner();
    }

    @Override
    public Set<OWLObjectProperty> getUnfilteredChildren(OWLObjectProperty objectProperty) {
        try {
            if (!getReasoner().isConsistent()) {
                return Collections.emptySet();
            }
            Set<OWLObjectPropertyExpression> subs = getReasoner().getSubObjectProperties(objectProperty, true)
                    .entities()
                    .collect(Collectors.toSet());
            subs.remove(objectProperty);
            subs.remove(mngr.getOWLDataFactory().getOWLBottomObjectProperty());
            Set<OWLObjectProperty> children = new HashSet<>();
            for (OWLObjectPropertyExpression p : subs) {
                if (p instanceof OWLObjectProperty) {
                    children.add((OWLObjectProperty) p);
                }
            }
            return children;
        } catch (Exception e) {
            LOGGER.error("An error occurred whilst asking the reasoner for the sub-properties of an object property: {}",
                    e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<OWLObjectProperty> getParents(OWLObjectProperty objectProperty) {
        try {
            if (!getReasoner().isConsistent()) {
                return Collections.emptySet();
            }
            Set<OWLObjectPropertyExpression> supers = getReasoner().getSuperObjectProperties(objectProperty, true)
                    .entities().collect(Collectors.toSet());
            supers.remove(objectProperty);
            Set<OWLObjectProperty> parents = new HashSet<>();
            for (OWLObjectPropertyExpression p : supers) {
                if (p instanceof OWLObjectProperty) {
                    parents.add((OWLObjectProperty) p);
                }
            }
            return parents;
        } catch (Exception e) {
            LOGGER.error("An error occurred whilst asking the reasoner for the super-properties of an object property: {}",
                    e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<OWLObjectProperty> getEquivalents(OWLObjectProperty objectProperty) {
        try {
            if (!getReasoner().isConsistent()) {
                return Collections.emptySet();
            }
            Set<OWLObjectPropertyExpression> equivs = getReasoner().getEquivalentObjectProperties(objectProperty)
                    .entities().collect(Collectors.toSet());
            equivs.remove(objectProperty);
            Set<OWLObjectProperty> ret = new HashSet<>();
            for (OWLObjectPropertyExpression p : equivs) {
                if (p instanceof OWLObjectProperty) {
                    ret.add((OWLObjectProperty) p);
                }
            }
            return ret;
        } catch (Exception e) {
            LOGGER.error("An error occurred whilst asking the reasoner for the equivalent-properties of an object property: {}",
                    e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public void dispose() {
        mngr.removeListener(listener);
        super.dispose();
    }
}
