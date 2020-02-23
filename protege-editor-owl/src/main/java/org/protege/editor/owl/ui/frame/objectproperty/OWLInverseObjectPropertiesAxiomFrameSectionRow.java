package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLInverseObjectPropertiesAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLObjectProperty, OWLInverseObjectPropertiesAxiom, OWLObjectProperty> {

    public OWLInverseObjectPropertiesAxiomFrameSectionRow(OWLEditorKit kit,
                                                          OWLFrameSection<OWLObjectProperty, OWLInverseObjectPropertiesAxiom, OWLObjectProperty> section,
                                                          OWLOntology ontology,
                                                          OWLObjectProperty rootObject,
                                                          OWLInverseObjectPropertiesAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLObjectProperty> getObjectEditor() {
        OWLObjectPropertyEditor editor = new OWLObjectPropertyEditor(getOWLEditorKit());
        OWLObjectPropertyExpression p = getAxiom().getFirstProperty();
        if (p.equals(getRoot())) {
            p = getAxiom().getSecondProperty();
        }
        if (!p.isAnonymous()) {
            editor.setEditedObject(p.asOWLObjectProperty());
        }
        return editor;
    }

    @Override
    protected OWLInverseObjectPropertiesAxiom createAxiom(OWLObjectProperty editedObject) {
        return getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(getRoot(), editedObject);
    }

    @Override
    public Stream<OWLObjectPropertyExpression> manipulatableObjects() {
        Set<OWLObjectPropertyExpression> props = getAxiom().properties().collect(Collectors.toSet());
        if (props.size() > 1) {
            props.remove(getRoot());
        }
        return props.stream();
    }
}
