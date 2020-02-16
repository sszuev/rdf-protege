package org.protege.editor.owl.model.util;

import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;


/**
 * A visitor that may be used to "flatten" <code>OWLClassExpression</code>s.
 * The visitor collects <code>OWLClassExpression</code>s and operands of
 * <code>OWLAnd</code> classes.  For example the description:
 * <code>
 * A and (B and C) and (D or E) and F
 * </code>
 * would be flattened to the set <code>{A, B, C, (D or E), F}</code>.
 * <p>
 * The general pattern of usage is to visit several descriptions and
 * which accumulates the set of flattened descriptions.  These can
 * be obtained with the <code>getClassExpressions</code> method.
 */
@SuppressWarnings("NullableProblems")
public class NestedIntersectionFlattener implements OWLClassExpressionVisitor {

    private final Set<OWLClassExpression> descriptions;

    public NestedIntersectionFlattener() {
        descriptions = new HashSet<>();
    }

    public void reset() {
        descriptions.clear();
    }

    public Set<OWLClassExpression> getClassExpressions() {
        return descriptions;
    }

    @Override
    public void visit(OWLObjectIntersectionOf node) {
        node.operands().forEach(desc -> desc.accept(this));
    }

    @Override
    public void visit(OWLDataAllValuesFrom node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLDataSomeValuesFrom node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLDataHasValue node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLObjectAllValuesFrom node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLObjectHasValue node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLObjectComplementOf node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLObjectUnionOf node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLClass node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLObjectOneOf node) {
        descriptions.add(node);
    }

    @Override
    public void visit(OWLObjectMinCardinality desc) {
        descriptions.add(desc);
    }

    @Override
    public void visit(OWLObjectExactCardinality desc) {
        descriptions.add(desc);
    }

    @Override
    public void visit(OWLObjectMaxCardinality desc) {
        descriptions.add(desc);
    }

    @Override
    public void visit(OWLObjectHasSelf desc) {
        descriptions.add(desc);
    }

    @Override
    public void visit(OWLDataMinCardinality desc) {
        descriptions.add(desc);
    }

    @Override
    public void visit(OWLDataExactCardinality desc) {
        descriptions.add(desc);
    }

    @Override
    public void visit(OWLDataMaxCardinality desc) {
        descriptions.add(desc);
    }
}
