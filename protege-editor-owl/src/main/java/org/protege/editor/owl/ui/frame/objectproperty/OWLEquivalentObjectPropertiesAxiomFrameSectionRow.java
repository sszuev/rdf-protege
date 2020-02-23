package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyExpressionEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
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
public class OWLEquivalentObjectPropertiesAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLObjectProperty, OWLEquivalentObjectPropertiesAxiom, OWLObjectPropertyExpression> {

    public OWLEquivalentObjectPropertiesAxiomFrameSectionRow(OWLEditorKit kit,
                                                             OWLFrameSection<OWLObjectProperty, OWLEquivalentObjectPropertiesAxiom, OWLObjectPropertyExpression> section,
                                                             OWLOntology ontology,
                                                             OWLObjectProperty rootObject,
                                                             OWLEquivalentObjectPropertiesAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    public boolean isEditable() {
        return getAxiom().properties().limit(3).count() <= 2;
    }
    
    @Override
    public boolean isDeleteable() {
    	return true;
    }

    @Override
    protected OWLObjectEditor<OWLObjectPropertyExpression> getObjectEditor() {
        OWLObjectPropertyExpressionEditor editor = new OWLObjectPropertyExpressionEditor(getOWLEditorKit());
        Set<OWLObjectPropertyExpression> set = manipulatableObjects().limit(2).collect(Collectors.toSet());
        if (set.size() == 1) {
            editor.setEditedObject(set.iterator().next());
        }
        return editor;
    }
    
    @Override
    public boolean checkEditorResults(OWLObjectEditor<OWLObjectPropertyExpression> editor) {
        Set<OWLObjectPropertyExpression> equivalents = editor.getEditedObjects();
        return equivalents.size() != 1 || !equivalents.contains(getRoot());
    }

    @Override
    public void handleEditingFinished(Set<OWLObjectPropertyExpression> editedObjects) {
        super.handleEditingFinished(withoutRoot(editedObjects.stream()).collect(Collectors.toSet()));
    }

    protected OWLEquivalentObjectPropertiesAxiom createAxiom(OWLObjectPropertyExpression editedObject) {
        return getOWLDataFactory().getOWLEquivalentObjectPropertiesAxiom(getRoot(), editedObject);
    }

    @Override
    public Stream<OWLObjectPropertyExpression> manipulatableObjects() {
        return withoutRoot(getAxiom().properties());
    }
}
