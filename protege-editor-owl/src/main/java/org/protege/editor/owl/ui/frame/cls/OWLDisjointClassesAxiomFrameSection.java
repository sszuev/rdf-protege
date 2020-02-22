package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLClassExpressionSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public class OWLDisjointClassesAxiomFrameSection
        extends AbstractOWLClassAxiomFrameSection<OWLDisjointClassesAxiom, Set<OWLClassExpression>> {

    public static final String LABEL = "Disjoint With";

    public final Set<OWLClassExpression> added = new HashSet<>();

    public OWLDisjointClassesAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<OWLClass> frame) {
        super(editorKit, LABEL, LABEL, frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void addAxiom(OWLDisjointClassesAxiom ax, OWLOntology ont) {
        addRow(new OWLDisjointClassesAxiomFrameSectionRow(getOWLEditorKit(), this, ont, getRootObject(), ax));
        ax.classExpressions().forEach(added::add);
    }

    @Override
    protected Stream<OWLDisjointClassesAxiom> classAxioms(OWLClassExpression clazz, OWLOntology ont) {
        if (!clazz.isAnonymous()) {
            return ont.disjointClassesAxioms(clazz.asOWLClass());
        }
        return ont.axioms(AxiomType.DISJOINT_CLASSES).filter(ax -> ax.contains(clazz));
    }

    @Override
    protected OWLDisjointClassesAxiom createAxiom(Set<OWLClassExpression> object) {
        object.add(getRootObject());
        return getOWLDataFactory().getOWLDisjointClassesAxiom(object);
    }

    @Override
    public OWLObjectEditor<Set<OWLClassExpression>> getObjectEditor() {
        return new OWLClassExpressionSetEditor(getOWLEditorKit());
    }

    @Override
    protected void infer() {
        OWLModelManager m = getOWLModelManager();
        OWLReasoner reasoner = m.getReasoner();
        if (!reasoner.isConsistent()) {
            return;
        }
        OWLDataFactory df = m.getOWLDataFactory();
        OWLClassExpression root = getRootObject();
        OWLEditorKit kit = getOWLEditorKit();
        NodeSet<OWLClass> disjointFromRoot = reasoner.getSubClasses(df.getOWLObjectComplementOf(root), true);
        disjointFromRoot.entities()
                .filter(c -> !added.contains(c) && !c.equals(root))
                .forEach(c -> {
                    addInferredRowIfNontrivial(new OWLDisjointClassesAxiomFrameSectionRow(kit, this, null, root,
                            df.getOWLDisjointClassesAxiom(getRootObject(), c)));
                    added.add(c);
                });

    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_DISJOINT_CLASSES;
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLClassExpression>> editor) {
        Set<OWLClassExpression> disjoints = Objects.requireNonNull(editor.getEditedObject());
        return disjoints.size() != 1 || !disjoints.contains(getRootObject());
    }

    @Override
    public boolean canAcceptDrop(List<OWLObject> objects) {
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean dropObjects(List<OWLObject> objects) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        Set<OWLClassExpression> descriptions = new HashSet<>();
        descriptions.add(getRootObject());
        for (OWLObject obj : objects) {
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
            descriptions.add((OWLClassExpression) obj);
        }
        if (descriptions.size() > 1) {
            OWLAxiom ax = getOWLDataFactory().getOWLDisjointClassesAxiom(descriptions);
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            getOWLModelManager().applyChanges(changes);
        }
        return true;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLDisjointClassesAxiom
                && ((OWLDisjointClassesAxiom) change.getAxiom()).contains(getRootObject());
    }

    @Override
    public Comparator<OWLFrameSectionRow<OWLClassExpression, OWLDisjointClassesAxiom, Set<OWLClassExpression>>> getRowComparator() {
        return null;
    }
}
