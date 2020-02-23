package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataPropertySetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLDisjointDataPropertiesFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLDataProperty, OWLDisjointDataPropertiesAxiom, Set<OWLDataProperty>> {

    public OWLDisjointDataPropertiesFrameSectionRow(OWLEditorKit owlEditorKit,
                                                    OWLFrameSection<OWLDataProperty, OWLDisjointDataPropertiesAxiom, Set<OWLDataProperty>> section,
                                                    OWLOntology ontology, OWLDataProperty rootObject,
                                                    OWLDisjointDataPropertiesAxiom axiom) {
        super(owlEditorKit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLDisjointDataPropertiesAxiom createAxiom(Set<OWLDataProperty> editedObject) {
        Set<OWLDataProperty> props = new HashSet<>();
        props.add(getRoot());
        props.addAll(editedObject);
        return getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(props);
    }

    @Override
    protected OWLObjectEditor<Set<OWLDataProperty>> getObjectEditor() {
        OWLDataPropertySetEditor editor = new OWLDataPropertySetEditor(getOWLEditorKit());
        Set<OWLDataProperty> namedDisjoints = manipulatableObjects()
                .filter(p -> !p.isAnonymous())
                .map(AsOWLDataProperty::asOWLDataProperty)
                .collect(Collectors.toSet());
        editor.setEditedObject(namedDisjoints);
        // @@TODO handle property expressions
        return editor;
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLDataProperty>> editor) {
        return !Objects.requireNonNull(editor.getEditedObject()).contains(getRoot());
    }

    @Override
    public Stream<OWLDataPropertyExpression> manipulatableObjects() {
        return withoutRoot(getAxiom().properties());
    }
}
