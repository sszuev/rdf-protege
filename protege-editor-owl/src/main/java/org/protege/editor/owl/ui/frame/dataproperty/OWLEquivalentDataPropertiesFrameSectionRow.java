package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataPropertyEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLEquivalentDataPropertiesFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLDataProperty, OWLEquivalentDataPropertiesAxiom, OWLDataProperty> {

    public OWLEquivalentDataPropertiesFrameSectionRow(OWLEditorKit kit,
                                                      OWLFrameSection<OWLDataProperty, OWLEquivalentDataPropertiesAxiom, OWLDataProperty> section,
                                                      OWLOntology ontology,
                                                      OWLDataProperty rootObject,
                                                      OWLEquivalentDataPropertiesAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLEquivalentDataPropertiesAxiom createAxiom(OWLDataProperty object) {
        return getOWLDataFactory().getOWLEquivalentDataPropertiesAxiom(CollectionFactory.createSet(getRoot(), object));
    }

    @Override
    public boolean isEditable() {
        return getAxiom().properties().limit(3).count() <= 2;
    }

    @Override
    public boolean isDeleteable() {
        return true;
    }

    protected OWLObjectEditor<OWLDataProperty> getObjectEditor() {
        OWLDataPropertyEditor editor = new OWLDataPropertyEditor(getOWLEditorKit());
        Set<OWLDataPropertyExpression> set = manipulatableObjects().limit(2).collect(Collectors.toSet());
        if (set.size() == 1) {
            OWLDataPropertyExpression p = set.iterator().next();
            if (!p.isAnonymous()) {
                editor.setEditedObject(p.asOWLDataProperty());
            }
        }
        return editor;
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<OWLDataProperty> editor) {
        Set<OWLDataProperty> equivalents = editor.getEditedObjects();
        return equivalents.size() != 1 || !equivalents.contains(getRoot());
    }

    @Override
    public void handleEditingFinished(Set<OWLDataProperty> editedObjects) {
        super.handleEditingFinished(withoutRoot(editedObjects.stream()).collect(Collectors.toSet()));
    }

    @Override
    public Stream<OWLDataPropertyExpression> manipulatableObjects() {
        return withoutRoot(getAxiom().properties());
    }
}
