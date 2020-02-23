package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLGeneralAxiomEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Apr-2007<br><br>
 */
public class OWLGeneralClassAxiomsFrameSection extends AbstractOWLFrameSection<OWLOntology, OWLClassAxiom, OWLClassAxiom> {

    public OWLGeneralClassAxiomsFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLOntology> frame) {
        super(editorKit, "General class axioms", "General class axiom", frame);
    }

    @Override
    protected OWLClassAxiom createAxiom(OWLClassAxiom object) {
        return object;
    }

    @Override
    public OWLObjectEditor<OWLClassAxiom> getObjectEditor() {
        return new OWLGeneralAxiomEditor(getOWLEditorKit());
    }

    @Override
    public void handleEditingFinished(Set<OWLClassAxiom> editedObjects) {
        super.handleEditingFinished(editedObjects);
        checkEditedAxiom(getOWLEditorKit(), editedObjects);
    }

    static void checkEditedAxiom(OWLEditorKit editorKit, Set<OWLClassAxiom> editedObjects) {
        OWLClassAxiom axiom = editedObjects.iterator().next();
        OWLOntology ontology = editorKit.getOWLModelManager().getActiveOntology();
        if (ontology.containsAxiom(axiom) && ontology.generalClassAxioms().noneMatch(axiom::equals)) {
            JOptionPane.showMessageDialog(editorKit.getOWLWorkspace(), "Edited axiom is not a general class axiom. " +
                    "It has been added to\nthe ontology but will not show in the General Class Axiom Window.");
        }
    }

    @Override
    protected void refill(OWLOntology ontology) {
        ontology.generalClassAxioms()
                .map(ax -> new OWLGeneralClassAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), ax))
                .forEach(this::addRow);
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLSubClassOfAxiom) {
            return ((OWLSubClassOfAxiom) axiom).getSubClass().isAnonymous();
        }
        if (axiom instanceof OWLDisjointClassesAxiom) {
            return ((OWLDisjointClassesAxiom) axiom).classExpressions().allMatch(IsAnonymous::isAnonymous);
        }
        if (axiom instanceof OWLEquivalentClassesAxiom) {
            return ((OWLEquivalentClassesAxiom) axiom).classExpressions().allMatch(IsAnonymous::isAnonymous);
        }
        return false;
    }

}
