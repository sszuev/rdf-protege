package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public class OWLEquivalentClassesAxiomFrameSection
        extends AbstractOWLClassAxiomFrameSection<OWLEquivalentClassesAxiom, OWLClassExpression> {

    private static final String LABEL = "Equivalent To";

    private final Set<OWLClassExpression> added = new HashSet<>();

    public OWLEquivalentClassesAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<OWLClass> frame) {
        super(editorKit, LABEL, "Equivalent class", frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    protected void addAxiom(OWLEquivalentClassesAxiom ax, OWLOntology ontology) {
        addRow(new OWLEquivalentClassesAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), ax));
        ax.classExpressions().forEach(added::add);
    }

    @Override
    protected Stream<OWLEquivalentClassesAxiom> classAxioms(OWLClassExpression descr, OWLOntology ont) {
        if (!descr.isAnonymous()) {
            return ont.equivalentClassesAxioms(descr.asOWLClass());
        }
        return ont.generalClassAxioms()
                .filter(ax -> ax instanceof OWLEquivalentClassesAxiom)
                .map(ax -> (OWLEquivalentClassesAxiom) ax)
                .filter(ax -> ax.classExpressions().anyMatch(x -> x.equals(descr)));
    }

    @Override
    protected void infer() {
        OWLReasoner reasoner = getOWLModelManager().getReasoner();
        if (!reasoner.isConsistent()) {
            return;
        }
        OWLDataFactory df = getOWLDataFactory();
        OWLClassExpression root = getRootObject();
        OWLEditorKit kit = getOWLEditorKit();
        if (!reasoner.isSatisfiable(root)) {
            if (!root.isOWLNothing()) {
                addRow(new OWLEquivalentClassesAxiomFrameSectionRow(kit, this, null, root,
                        df.getOWLEquivalentClassesAxiom(root, df.getOWLNothing())));
            }
            return;
        }
        for (OWLClassExpression c : reasoner.getEquivalentClasses(root)) {
            if (added.contains(c) || c.equals(root)) {
                continue;
            }
            addRow(new OWLEquivalentClassesAxiomFrameSectionRow(kit, this, null, root,
                    df.getOWLEquivalentClassesAxiom(root, c)));
        }
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_EQUIVALENT_CLASSES;
    }

    @Override
    protected OWLEquivalentClassesAxiom createAxiom(OWLClassExpression object) {
        return getOWLDataFactory().getOWLEquivalentClassesAxiom(getRootObject(), object);
    }

    @Override
    public OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLEditorKit().getWorkspace()
                .getOWLComponentFactory().getOWLClassDescriptionEditor(null, AxiomType.EQUIVALENT_CLASSES);
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<OWLClassExpression> editor) {
        Set<OWLClassExpression> equivalents = editor.getEditedObjects();
        return equivalents.size() != 1 || !equivalents.contains(getRootObject());
    }

    @Override
    public void handleEditingFinished(Set<OWLClassExpression> editedObjects) {
        editedObjects = new HashSet<>(editedObjects);
        editedObjects.remove(getRootObject());
        super.handleEditingFinished(editedObjects);
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
            if (!(obj instanceof OWLClassExpression)) {
                return false;
            }
            OWLClassExpression desc = (OWLClassExpression) obj;
            OWLAxiom ax = getOWLDataFactory().getOWLEquivalentClassesAxiom(getRootObject(), desc);
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLEquivalentClassesAxiom &&
                ((OWLEquivalentClassesAxiom) change.getAxiom()).contains(getRootObject());
    }

    @Override
    public Comparator<OWLFrameSectionRow<OWLClassExpression, OWLEquivalentClassesAxiom, OWLClassExpression>> getRowComparator() {
        return null;
    }
}
