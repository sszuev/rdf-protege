package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLIndividualEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import javax.swing.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 27-Jan-2007<br>
 * <br>
 */
public class OWLClassAssertionAxiomMembersSection
        extends AbstractOWLClassAxiomFrameSection<OWLClassAssertionAxiom, OWLNamedIndividual> {

    public static final String LABEL = "Instances";
    public static final boolean SHOW_DIRECT_INSTANCES = true;

    private final Set<OWLNamedIndividual> added = new HashSet<>();

    public OWLClassAssertionAxiomMembersSection(OWLEditorKit editorKit, OWLFrame<? extends OWLClass> frame) {
        super(editorKit, LABEL, "Type assertion", frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void addAxiom(OWLClassAssertionAxiom ax, OWLOntology ont) {
        addRow(new OWLClassAssertionAxiomMembersSectionRow(getOWLEditorKit(), this, ont, getRootObject(), ax));
        if (!ax.getIndividual().isAnonymous()) {
            added.add(ax.getIndividual().asOWLNamedIndividual());
        }
    }

    @Override
    protected Stream<OWLClassAssertionAxiom> classAxioms(OWLClassExpression clazz, OWLOntology ont) {
        if (!clazz.isAnonymous()) {
            return ont.classAssertionAxioms(clazz.asOWLClass());
        }
        return ont.axioms(AxiomType.CLASS_ASSERTION).filter(ax -> ax.getClassExpression().equals(clazz));
    }

    @Override
    protected void infer() {
        OWLModelManager m = getOWLModelManager();
        if (!m.getReasoner().isConsistent()) {
            return;
        }
        OWLDataFactory df = m.getOWLDataFactory();
        OWLClassExpression root = getRootObject();
        NodeSet<OWLNamedIndividual> instances = m.getReasoner().getInstances(root, SHOW_DIRECT_INSTANCES);
        if (instances == null) {
            return;
        }
        instances.entities()
                .filter(i -> !i.isAnonymous() && !added.contains(i.asOWLNamedIndividual()))
                .forEach(i -> {
                    addRow(new OWLClassAssertionAxiomMembersSectionRow(getOWLEditorKit(), this, null, root,
                            df.getOWLClassAssertionAxiom(root, i)));
                    added.add(i.asOWLNamedIndividual());
                });
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERED_CLASS_MEMBERS;
    }

    @Override
    protected OWLClassAssertionAxiom createAxiom(OWLNamedIndividual individual) {
        return getOWLDataFactory().getOWLClassAssertionAxiom(getRootObject(), individual);
    }

    @Override
    public OWLObjectEditor<OWLNamedIndividual> getObjectEditor() {
        return new OWLIndividualEditor(getOWLEditorKit(), ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    @Override
    public boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLIndividual)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLObject obj : objects) {
            if (obj instanceof OWLIndividual) {
                OWLIndividual ind = (OWLIndividual) obj;
                OWLAxiom ax = getOWLDataFactory().getOWLClassAssertionAxiom(getRootObject(), ind);
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            }
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    @Override
    public Comparator<OWLFrameSectionRow<OWLClassExpression, OWLClassAssertionAxiom, OWLNamedIndividual>> getRowComparator() {
        return (o1, o2) -> {
            OWLModelManager m = getOWLModelManager();
            String s1 = m.getRendering(o1.getAxiom().getIndividual());
            String s2 = m.getRendering(o2.getAxiom().getIndividual());
            return s1.compareToIgnoreCase(s2);
        };
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLClassAssertionAxiom
                && ((OWLClassAssertionAxiom) change.getAxiom()).getClassExpression().equals(getRootObject());
    }
}
