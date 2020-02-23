package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.editor.OWLGeneralAxiomEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 09/06/2014
 */
public class OWLClassGeneralClassAxiomFrameSection
        extends AbstractOWLFrameSection<OWLClass, OWLClassAxiom, OWLClassAxiom> {

    public OWLClassGeneralClassAxiomFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLClass> frame) {
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
        checkEditedAxiom(getOWLEditorKit(), editedObjects, getRootObject());
        super.handleEditingFinished(editedObjects);
    }

    static void checkEditedAxiom(OWLEditorKit kit, Set<OWLClassAxiom> editedObjects, OWLClass root) {
        if (editedObjects.isEmpty()) {
            return;
        }
        OWLClassAxiom axiom = editedObjects.iterator().next();
        if (axiom.containsEntityInSignature(root)) {
            return;
        }
        OWLModelManager m = kit.getModelManager();
        String classesInSigRendering = axiom.classesInSignature().map(m::getRendering).collect(Collectors.joining(",\n"));
        JOptionPane.showMessageDialog(kit.getOWLWorkspace(),
                String.format("The axiom that you edited has been added to the ontology. " +
                                "However, it will not be visible in the view below as " +
                                "it does not mention the selected class (%s).\n" +
                                "To view the axiom, select any of the classes it mentions: \n%s",
                        kit.getOWLModelManager().getRendering(root), classesInSigRendering));
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLWorkspace workspace = getOWLEditorKit().getOWLWorkspace();
        OWLClass clazz = workspace.getOWLSelectionModel().getLastSelectedClass();
        if (clazz == null) {
            return;
        }
        ontology.generalClassAxioms()
                .filter(ax -> ax.containsEntityInSignature(clazz))
                .map(ax -> new OWLClassGeneralClassAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), ax))
                .forEach(this::addRow);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected boolean isResettingChange(OWLOntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        if (change.signature().filter(AsOWLClass::isOWLClass).noneMatch(x -> x.equals(getRootObject()))) {
            return false;
        }
        OWLAxiom axiom = change.getAxiom();
        return axiom.accept(new OWLAxiomVisitorEx<Boolean>() {
            @Override
            public Boolean visit(OWLSubClassOfAxiom axiom) {
                return axiom.isGCI();
            }

            @Override
            public Boolean visit(OWLEquivalentClassesAxiom axiom) {
                return !axiom.contains(getRootObject());
            }

            @Override
            public Boolean visit(OWLDisjointClassesAxiom axiom) {
                return !axiom.contains(getRootObject());
            }
        });
    }
}
