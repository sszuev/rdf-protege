package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLClassExpressionSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public class OWLDisjointClassesAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLClassExpression, OWLDisjointClassesAxiom, Set<OWLClassExpression>> {


    public OWLDisjointClassesAxiomFrameSectionRow(OWLEditorKit kit,
                                                  OWLFrameSection<OWLClassExpression, OWLDisjointClassesAxiom, Set<OWLClassExpression>> section,
                                                  OWLOntology ontology, OWLClassExpression rootObject,
                                                  OWLDisjointClassesAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<Set<OWLClassExpression>> getObjectEditor() {
        return new OWLClassExpressionSetEditor(getOWLEditorKit(), manipulatableObjects().collect(Collectors.toList()));
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLClassExpression>> editor) {
        Set<OWLClassExpression> disjoints = Objects.requireNonNull(editor.getEditedObject());
        return disjoints.size() != 1 || !disjoints.contains(getRoot());
    }

    @Override
    protected OWLDisjointClassesAxiom createAxiom(Set<OWLClassExpression> editedObject) {
        editedObject.add(getRoot());
        return getOWLDataFactory().getOWLDisjointClassesAxiom(editedObject);
    }

    @Override
    public Stream<OWLClassExpression> manipulatableObjects() {
        return withoutRoot(getAxiom().classExpressions());
    }
}
