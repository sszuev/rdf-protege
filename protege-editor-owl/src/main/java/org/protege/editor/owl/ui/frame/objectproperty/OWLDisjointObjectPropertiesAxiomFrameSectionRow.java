package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyTabbedSetEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLDisjointObjectPropertiesAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLObjectProperty, OWLDisjointObjectPropertiesAxiom, Set<OWLObjectPropertyExpression>> {


    public OWLDisjointObjectPropertiesAxiomFrameSectionRow(OWLEditorKit kit,
                                                           OWLFrameSection<OWLObjectProperty, OWLDisjointObjectPropertiesAxiom, Set<OWLObjectPropertyExpression>> section,
                                                           OWLOntology ontology,
                                                           OWLObjectProperty rootObject,
                                                           OWLDisjointObjectPropertiesAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<Set<OWLObjectPropertyExpression>> getObjectEditor() {
        OWLObjectPropertyTabbedSetEditor editor = new OWLObjectPropertyTabbedSetEditor(getOWLEditorKit());
        editor.setEditedObject(manipulatableObjects().collect(Collectors.toSet()));
        return editor;
    }

    @Override
    protected OWLDisjointObjectPropertiesAxiom createAxiom(Set<OWLObjectPropertyExpression> editedObject) {
        Set<OWLObjectPropertyExpression> props = new HashSet<>();
        props.add(getRoot());
        props.addAll(editedObject);
        return getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(props);
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLObjectPropertyExpression>> editor) {
        return !Objects.requireNonNull(editor.getEditedObject()).contains(getRoot());
    }

    @Override
    public Stream<OWLObjectPropertyExpression> manipulatableObjects() {
        return withoutRoot(getAxiom().properties());
    }
}

