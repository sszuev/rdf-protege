package org.protege.editor.owl.ui.frame.property;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractInferOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.*;
import java.util.stream.Stream;

/*
* Copyright (C) 2007, University of Manchester
*
*
*/

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 16, 2008<br><br>
 */
public abstract class AbstractPropertyDomainFrameSection<P extends OWLProperty, A extends OWLPropertyDomainAxiom<?>>
        extends AbstractInferOWLFrameSection<P, A, OWLClassExpression> {

    public static final String LABEL = "Domains (intersection)";

    private final Set<OWLClassExpression> added = new HashSet<>();

    public AbstractPropertyDomainFrameSection(OWLEditorKit kit, OWLFrame<P> frame) {
        super(kit, LABEL, "Domain", frame);
    }

    @Override
    public OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLEditorKit().getWorkspace().getOWLComponentFactory().getOWLClassDescriptionEditor(null, getAxiomType());
    }

    protected AxiomType<?> getAxiomType() {
        return getRootObject() instanceof OWLObjectProperty ?
                AxiomType.OBJECT_PROPERTY_DOMAIN : AxiomType.DATA_PROPERTY_DOMAIN;
    }

    @Override
    public final boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
            OWLClassExpression desc = (OWLClassExpression) obj;
            OWLAxiom ax = createAxiom(desc);
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    protected abstract AbstractPropertyDomainFrameSectionRow<P, A> createFrameSectionRow(A axiom, OWLOntology ontology);

    protected abstract Stream<A> axioms(OWLOntology ontology);

    protected abstract NodeSet<OWLClass> getInferredDomains();

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected final void refill(OWLOntology ontology) {
        axioms(ontology).forEach(ax -> {
            addRow(createFrameSectionRow(ax, ontology));
            added.add(ax.getDomain());
        });
    }

    @Override
    protected void infer() {
        for (Node<OWLClass> domains : getInferredDomains()) {
            for (OWLClassExpression domain : domains) {
                if (added.contains(domain)) {
                    continue;
                }
                addInferredRowIfNontrivial(createFrameSectionRow(createAxiom(domain), null));
                added.add(domain);
            }
        }
    }

    @Override
    public Comparator<OWLFrameSectionRow<P, A, OWLClassExpression>> getRowComparator() {
        return null;
    }
}
