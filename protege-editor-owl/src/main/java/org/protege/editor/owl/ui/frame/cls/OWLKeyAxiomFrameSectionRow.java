package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLPropertySetEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/*
* Copyright (C) 2007, University of Manchester
*
*
*/

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>

 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 4, 2009<br><br>
 */
public class OWLKeyAxiomFrameSectionRow extends AbstractOWLFrameSectionRow<OWLClass, OWLHasKeyAxiom, Set<OWLPropertyExpression>> {

    public OWLKeyAxiomFrameSectionRow(OWLEditorKit kit,
                                      OWLFrameSection<OWLClass, OWLHasKeyAxiom, Set<OWLPropertyExpression>> section,
                                      OWLOntology ontology,
                                      OWLClass rootObject,
                                      OWLHasKeyAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLHasKeyAxiom createAxiom(Set<OWLPropertyExpression> properties) {
        // Degenericized to be compatible with changing OWLAPI interfaces
        return getOWLDataFactory().getOWLHasKeyAxiom(getRoot(), properties);
    }

    @Override
    protected OWLPropertySetEditor getObjectEditor() {
        OWLPropertySetEditor editor = new OWLPropertySetEditor(getOWLEditorKit());
        // Degenericized to be compatible with changing OWLAPI interfaces
        editor.setEditedObject(getAxiom().propertyExpressions().collect(Collectors.toSet()));
        return editor;
    }

    @Override
    public Stream<OWLPropertyExpression> manipulatableObjects() {
        return getAxiom().propertyExpressions();
    }
}
