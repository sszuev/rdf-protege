package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLGeneralAxiomEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 09/06/2014
 */
public class OWLClassGeneralClassAxiomFrameSectionRow extends AbstractOWLFrameSectionRow<OWLClass, OWLClassAxiom, OWLClassAxiom> {

    public OWLClassGeneralClassAxiomFrameSectionRow(OWLEditorKit kit,
                                                    OWLFrameSection<OWLClass, OWLClassAxiom, OWLClassAxiom> section,
                                                    OWLOntology ontology,
                                                    OWLClass rootObject,
                                                    OWLClassAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLClassAxiom> getObjectEditor() {
        OWLGeneralAxiomEditor editor = new OWLGeneralAxiomEditor(getOWLEditorKit());
        editor.setEditedObject(getAxiom());
        return editor;
    }

    @Override
    public void handleEditingFinished(Set<OWLClassAxiom> editedObjects) {
        OWLClassGeneralClassAxiomFrameSection.checkEditedAxiom(getOWLEditorKit(), editedObjects, getRoot());
        super.handleEditingFinished(editedObjects);
    }

    @Override
    protected OWLClassAxiom createAxiom(OWLClassAxiom editedObject) {
        return editedObject;
    }

    @Override
    public Stream<? extends OWLObject> manipulatableObjects() {
        return Stream.of(getAxiom());
    }
}
