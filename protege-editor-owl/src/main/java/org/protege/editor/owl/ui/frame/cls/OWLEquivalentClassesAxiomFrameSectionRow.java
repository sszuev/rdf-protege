package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public class OWLEquivalentClassesAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLClassExpression, OWLEquivalentClassesAxiom, OWLClassExpression> {

    public OWLEquivalentClassesAxiomFrameSectionRow(OWLEditorKit kit,
                                                    OWLFrameSection<OWLClassExpression, OWLEquivalentClassesAxiom, OWLClassExpression> section,
                                                    OWLOntology ontology, OWLClassExpression rootObject,
                                                    OWLEquivalentClassesAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    public boolean isEditable() {
        return manipulatableObjects().limit(2).count() == 1;
    }

    @Override
    public boolean isDeleteable() {
        return true;
    }

    @Override
    protected OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        Set<OWLClassExpression> res = manipulatableObjects().limit(2).collect(Collectors.toSet());
        return res.size() != 1 ? null :
                getOWLComponentFactory().getOWLClassDescriptionEditor(res.iterator().next(), AxiomType.EQUIVALENT_CLASSES);
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<OWLClassExpression> editor) {
        Set<OWLClassExpression> equivalents = editor.getEditedObjects();
        return equivalents.size() != 1 || !equivalents.contains(getRoot());
    }

    @Override
    public void handleEditingFinished(Set<OWLClassExpression> editedObjects) {
        super.handleEditingFinished(withoutRoot(editedObjects.stream()).collect(Collectors.toSet()));
    }

    @Override
    protected OWLEquivalentClassesAxiom createAxiom(OWLClassExpression editedObject) {
        return getOWLDataFactory().getOWLEquivalentClassesAxiom(CollectionFactory.createSet(getRoot(), editedObject));
    }

    @Override
    public Stream<OWLClassExpression> manipulatableObjects() {
        return withoutRoot(getAxiom().classExpressions());
    }
}

