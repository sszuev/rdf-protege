package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Dec-2007<br><br>
 */
public class AxiomListFrameSection extends AbstractOWLFrameSection<Set<OWLAxiom>, OWLAxiom, OWLAxiom> {

    private final RowComparator rowComparator = new RowComparator();

    private final Set<OWLAxiom> added = new HashSet<>();

    public AxiomListFrameSection(OWLEditorKit editorKit, OWLFrame<Set<OWLAxiom>> owlFrame) {
        super(editorKit, "Axioms", "Axiom", owlFrame);
    }

    @Override
    protected OWLAxiom createAxiom(OWLAxiom object) {
        return null;
    }

    @Override
    public OWLObjectEditor<OWLAxiom> getObjectEditor() {
        return null;
    }

    @Override
    protected void refill(OWLOntology ontology) {
        Set<OWLAxiom> axioms = getRootObject();
        axioms.stream().filter(ontology::containsAxiom).forEach(ax -> {
            addRow(new AxiomListFrameSectionRow(getOWLEditorKit(), this, ontology, axioms, ax));
            added.add(ax);
        });
    }

    @Override
    protected void refillInferred() {
        Set<OWLAxiom> axioms = getRootObject();
        axioms.stream()
                .filter(ax -> !added.contains(ax))
                .forEach(ax -> addRow(new AxiomListFrameSectionRow(getOWLEditorKit(), this, null, axioms, ax)));
    }

    @Override
    protected void clear() {
        added.clear();
    }

    @Override
    public Comparator<OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom>> getRowComparator() {
        return rowComparator;
    }

    @Override
    public boolean canAdd() {
        return false;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange();
    }

    private class RowComparator implements Comparator<OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom>> {
        private final Comparator<OWLObject> objComparator;

        public RowComparator() {
            this.objComparator = getOWLModelManager().getOWLObjectComparator();
        }

        public int compare(OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom> o1,
                           OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom> o2) {
            return objComparator.compare(o1.getAxiom(), o2.getAxiom());
        }
    }
}
