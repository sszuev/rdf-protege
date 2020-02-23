package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.stream.Stream;
/*
 * Copyright (C) 2007, University of Manchester
 *
 *
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14-Oct-2007<br><br>
 */
public class InferredAxiomsFrameSectionRow extends AbstractOWLFrameSectionRow<OWLOntology, OWLAxiom, OWLAxiom> {

    private final OWLAxiom axiom;

    public InferredAxiomsFrameSectionRow(OWLEditorKit kit,
                                         OWLFrameSection<OWLOntology, OWLAxiom, OWLAxiom> section,
                                         OWLOntology ontology,
                                         OWLOntology root,
                                         OWLAxiom axiom) {
        super(kit, section, ontology, root, axiom);
        this.axiom = axiom;
    }

    @Override
    protected OWLAxiom createAxiom(OWLAxiom editedObject) {
        return editedObject;
    }

    @Override
    protected OWLObjectEditor<OWLAxiom> getObjectEditor() {
        return null;
    }

    @Override
    public Stream<OWLAxiom> manipulatableObjects() {
        return Stream.of(axiom);
    }
}
