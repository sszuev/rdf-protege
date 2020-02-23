package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Dec-2007<br><br>
 */
public class AxiomListFrameSectionRow extends AbstractOWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom> {


    public AxiomListFrameSectionRow(OWLEditorKit kit,
                                    OWLFrameSection<Set<OWLAxiom>, OWLAxiom, OWLAxiom> section,
                                    OWLOntology ontology,
                                    Set<OWLAxiom> root,
                                    OWLAxiom axiom) {
        super(kit, section, ontology, root, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLAxiom> getObjectEditor() {
        return null;
    }

    @Override
    protected OWLAxiom createAxiom(OWLAxiom editedObject) {
        return null;
    }

    @Override
    public Stream<OWLAxiom> manipulatableObjects() {
        return Stream.of(getAxiom());
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isDeleteable() {
        return true;
    }
}
