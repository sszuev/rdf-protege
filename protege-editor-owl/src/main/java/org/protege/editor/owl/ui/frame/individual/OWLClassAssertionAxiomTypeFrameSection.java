package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractInferOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;

import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLClassAssertionAxiomTypeFrameSection
        extends AbstractInferOWLFrameSection<OWLIndividual, OWLClassAssertionAxiom, OWLClassExpression> {

    public static final String LABEL = "Types";

    private final Set<OWLClassExpression> added = new HashSet<>();

    public OWLClassAssertionAxiomTypeFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLIndividual> frame) {
        super(editorKit, LABEL, "Type assertion", frame);
    }

    @Override
    protected void clear() {
        added.clear();
    }

    /**
     * Refills the section with rows.
     * This method will be called by the system and should be directly called.
     *
     * @param ontology {@link OWLOntology}
     */
    @Override
    protected void refill(OWLOntology ontology) {
        ontology.classAssertionAxioms(getRootObject()).forEach(ax -> {
            addRow(new OWLClassAssertionAxiomTypeFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), ax));
            added.add(ax.getClassExpression());
        });
    }

    @Override
    protected OptionalInferenceTask getOptionalInferenceTask() {
        return OptionalInferenceTask.SHOW_INFERRED_TYPES;
    }

    @Override
    protected void infer() {
        if (!getOWLModelManager().getReasoner().isConsistent()) {
            return;
        }
        if (getRootObject().isAnonymous()) {
            return;
        }
        getReasoner().getTypes(getRootObject().asOWLNamedIndividual(), true)
                .entities()
                .filter(c -> !added.contains(c))
                .forEach(c -> {
                    OWLClassAssertionAxiom ax = getOWLDataFactory().getOWLClassAssertionAxiom(c, getRootObject());
                    addInferredRowIfNontrivial(new OWLClassAssertionAxiomTypeFrameSectionRow(getOWLEditorKit(), this, null,
                            getRootObject(), ax));
                    added.add(c);
                });
    }

    @Override
    protected OWLClassAssertionAxiom createAxiom(OWLClassExpression classExpression) {
        return getOWLDataFactory().getOWLClassAssertionAxiom(classExpression, getRootObject());
    }

    @Override
    public OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLEditorKit().getWorkspace()
                .getOWLComponentFactory().getOWLClassDescriptionEditor(null, AxiomType.CLASS_ASSERTION);
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
            OWLClassExpression classExpression = (OWLClassExpression) obj;
            OWLAxiom ax = getOWLDataFactory().getOWLClassAssertionAxiom(classExpression, getRootObject());
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        }
        getOWLModelManager().applyChanges(changes);
        return true;
    }

    /**
     * Obtains a comparator which can be used to sort the rows in this section.
     *
     * @return A comparator if to sort the rows in this section, or <code>null</code> if the rows shouldn't be sorted.
     */
    @Override
    public Comparator<OWLFrameSectionRow<OWLIndividual, OWLClassAssertionAxiom, OWLClassExpression>> getRowComparator() {
        Comparator<OWLObject> comparator = getOWLModelManager().getOWLObjectComparator();
        return (o1, o2) -> {
            if (o1.isInferred() && !o2.isInferred()) {
                return 1;
            } else if (o2.isInferred() && !o1.isInferred()) {
                return -1;
            }
            return comparator.compare(o1.getAxiom().getClassExpression(), o2.getAxiom().getClassExpression());
        };
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLClassAssertionAxiom
                && ((OWLClassAssertionAxiom) change.getAxiom()).getIndividual().equals(getRootObject());
    }

}
