package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public class OWLSubClassAxiomFrameSection extends AbstractOWLClassAxiomFrameSection<OWLSubClassOfAxiom, OWLClassExpression> {

    private static final String LABEL = "SubClass Of";

    private Set<OWLClassExpression> added = new HashSet<>();
    private OWLObjectProperty prop; // WTF?

    public OWLSubClassAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<OWLClass> frame) {
        super(editorKit, LABEL, "Superclass", frame);
    }

    @Override
    protected void clear() {
        added.clear();
        prop = null;
    }

    @Override
    protected void addAxiom(OWLSubClassOfAxiom ax, OWLOntology ont) {
        addRow(new OWLSubClassAxiomFrameSectionRow(getOWLEditorKit(), this, ont, getRootObject(), ax));
        added.add(ax.getSuperClass());
    }

    @Override
    protected Stream<OWLSubClassOfAxiom> classAxioms(OWLClassExpression clazz, OWLOntology ont) {
        if (!clazz.isAnonymous()) {
            return ont.subClassAxiomsForSubClass(clazz.asOWLClass());
        }
        return ont.generalClassAxioms()
                .filter(ax -> ax instanceof OWLSubClassOfAxiom)
                .map(ax -> (OWLSubClassOfAxiom) ax)
                .filter(ax -> ax.getSubClass().equals(clazz));
    }

    @Override
    protected void refillInferred() {
        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return;
        }
        if (!reasoner.isSatisfiable(getRootObject())) {
            return;
        }
        super.refillInferred();
    }

    @Override
    protected void infer() {
        OWLModelManager m = getOWLModelManager();
        OWLReasoner reasoner = m.getReasoner();
        OWLClassExpression root = getRootObject();
        OWLEditorKit kit = getOWLEditorKit();
        for (Node<OWLClass> inferredSuperClasses : reasoner.getSuperClasses(root, true)) {
            for (OWLClassExpression inferredSuperClass : inferredSuperClasses) {
                if (added.contains(inferredSuperClass)) {
                    continue;
                }
                addInferredRowIfNontrivial(new OWLSubClassAxiomFrameSectionRow(kit, this, null, root,
                        m.getOWLDataFactory().getOWLSubClassOfAxiom(root, inferredSuperClass)));
                added.add(inferredSuperClass);
            }
        }
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_SUPER_CLASSES;
    }

    @Override
    protected OWLSubClassOfAxiom createAxiom(OWLClassExpression object) {
        return getOWLDataFactory().getOWLSubClassOfAxiom(getRootObject(), object);
    }

    @Override
    public OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLEditorKit().getWorkspace()
                .getOWLComponentFactory().getOWLClassDescriptionEditor(null, AxiomType.SUBCLASS_OF);
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
        for (OWLObject obj : objects) {
            if (obj instanceof OWLClassExpression) {
                OWLClassExpression desc;
                if (prop != null) {
                    desc = getOWLDataFactory().getOWLObjectSomeValuesFrom(prop, (OWLClassExpression) obj);
                } else {
                    desc = (OWLClassExpression) obj;
                }
                OWLAxiom ax = getOWLDataFactory().getOWLSubClassOfAxiom(getRootObject(), desc);
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            } else if (obj instanceof OWLObjectProperty) {
                // Prime
                prop = (OWLObjectProperty) obj;
            } else {
                return false;
            }
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLSubClassOfAxiom) {
            return ((OWLSubClassOfAxiom) axiom).getSubClass().equals(getRootObject());
        }
        return false;
    }

    @Override
    public Comparator<OWLFrameSectionRow<OWLClassExpression, OWLSubClassOfAxiom, OWLClassExpression>> getRowComparator() {
        return (o1, o2) -> {
            if (o1.isInferred()) {
                if (!o2.isInferred()) {
                    return 1;
                }
            } else {
                if (o2.isInferred()) {
                    return -1;
                }
            }
            int val = getOWLModelManager().getOWLObjectComparator().compare(o1.getAxiom(), o2.getAxiom());
            return val == 0 ? o1.getOntology().getOntologyID().compareTo(o2.getOntology().getOntologyID()) : val;
        };
    }
}
