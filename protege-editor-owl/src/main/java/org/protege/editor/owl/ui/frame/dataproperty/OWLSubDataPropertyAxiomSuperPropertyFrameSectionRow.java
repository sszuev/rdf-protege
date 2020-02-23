package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataPropertyEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLSubDataPropertyAxiomSuperPropertyFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLDataProperty, OWLSubDataPropertyOfAxiom, OWLDataProperty> {

    public OWLSubDataPropertyAxiomSuperPropertyFrameSectionRow(OWLEditorKit kit,
                                                               OWLFrameSection<OWLDataProperty, OWLSubDataPropertyOfAxiom, OWLDataProperty> section,
                                                               OWLOntology ontology,
                                                               OWLDataProperty rootObject,
                                                               OWLSubDataPropertyOfAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLSubDataPropertyOfAxiom createAxiom(OWLDataProperty editedObject) {
        return getOWLDataFactory().getOWLSubDataPropertyOfAxiom(getRoot(), editedObject);
    }

    @Override
    protected OWLObjectEditor<OWLDataProperty> getObjectEditor() {
        OWLDataPropertyEditor editor = new OWLDataPropertyEditor(getOWLEditorKit());
        OWLDataPropertyExpression p = getAxiom().getSuperProperty();
        if (!p.isAnonymous()) {
            editor.setEditedObject(p.asOWLDataProperty());
        }
        return editor;
    }

    @Override
    public Stream<OWLDataPropertyExpression> manipulatableObjects() {
        return Stream.of(getAxiom().getSuperProperty());
    }
}
