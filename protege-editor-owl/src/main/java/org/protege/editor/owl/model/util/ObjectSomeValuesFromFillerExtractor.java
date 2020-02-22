package org.protege.editor.owl.model.util;

import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A utility class that can be used to extract the fillers
 * of existential (OWLObjectSomeValuesFroms) that restrict
 * a specified property.  This class is a visitor and should
 * be used to visit <code>OWLClassExpression</code>s - only existential
 * (and hasValue) restrictions are processed.
 * Visiting the following descriptions,
 * <code>
 * A
 * p some B
 * C
 * p some (C and D)
 * p only E
 * </code>
 * would return the set <code>{B, (C and D)}</code>.
 *
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 14-Jun-2006<br><br>
 */
public class ObjectSomeValuesFromFillerExtractor implements OWLClassExpressionVisitor {

    private final OWLDataFactory dataFactory;
    private final OWLObjectProperty objectProperty;
    protected final Set<OWLClassExpression> fillers;

    /**
     * Creates a filler extractor
     * that will extract the fillers of existential restrictions that restrict the specified object property.
     *
     * @param objectProperty The object property.
     */
    public ObjectSomeValuesFromFillerExtractor(OWLDataFactory dataFactory, OWLObjectProperty objectProperty) {
        this.dataFactory = dataFactory;
        this.objectProperty = objectProperty;
        fillers = new HashSet<>();
    }

    public OWLObjectProperty getObjectProperty() {
        return objectProperty;
    }

    /**
     * Resets the extractor and clears the set of fillers that have been
     * accumulated over the course of a series of visits.
     */
    public void reset() {
        fillers.clear();
    }

    /**
     * Gets a set of descriptions
     * that correspond to the fillers of existential restrictions that this visitor has visited.
     * Note that the set returned also includes nominals (i.e. singleton enumerations)
     * containing the the individual used in an <code>OWLObjectHasValue</code>,
     * since these are really syntactic sugar for existential restrictions with a nominal filler.
     *
     * @return A <code>Set</code> of <code>OWLClassExpression</code>s that corresponds to the
     * fillers of the existential restrictions that were processed
     */
    public Set<OWLClassExpression> getFillers() {
        return new HashSet<>(fillers);
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom node) {
        if (!node.getProperty().equals(objectProperty)) {
            return;
        }
        NestedIntersectionFlattener flattener = new NestedIntersectionFlattener();
        node.getFiller().accept(flattener);
        fillers.addAll(flattener.getClassExpressions());
    }

    @Override
    public void visit(OWLObjectHasValue node) {
        if (node.getProperty().equals(objectProperty)) {
            fillers.add(dataFactory.getOWLObjectOneOf(Collections.singleton(node.getFiller())));
        }
    }
}
