package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyExpressionEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLSubObjectPropertyAxiomSuperPropertyFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLObjectProperty, OWLSubObjectPropertyOfAxiom, OWLObjectPropertyExpression> {

    public OWLSubObjectPropertyAxiomSuperPropertyFrameSectionRow(OWLEditorKit owlEditorKit,
                                                                 OWLFrameSection<OWLObjectProperty, OWLSubObjectPropertyOfAxiom, OWLObjectPropertyExpression> section,
                                                                 OWLOntology ontology,
                                                                 OWLObjectProperty rootObject,
                                                                 OWLSubObjectPropertyOfAxiom axiom) {
        super(owlEditorKit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLObjectPropertyExpression> getObjectEditor() {
        OWLObjectPropertyExpressionEditor editor = new OWLObjectPropertyExpressionEditor(getOWLEditorKit());
        OWLObjectPropertyExpression p = getAxiom().getSuperProperty();
        editor.setEditedObject(p);
        return editor;
    }

    @Override
    protected OWLSubObjectPropertyOfAxiom createAxiom(OWLObjectPropertyExpression editedObject) {
        return getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(getRoot(), editedObject);
    }

    @Override
    public Stream<OWLObjectPropertyExpression> manipulatableObjects() {
        return Stream.of(getAxiom().getSuperProperty());
    }
}
