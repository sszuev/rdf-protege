package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLClassExpressionSetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OWLDisjointUnionAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLClass, OWLDisjointUnionAxiom, Set<OWLClassExpression>> {

    public OWLDisjointUnionAxiomFrameSectionRow(OWLEditorKit kit,
                                                OWLFrameSection<OWLClass, OWLDisjointUnionAxiom, Set<OWLClassExpression>> section,
                                                OWLOntology ontology, OWLClass rootObject,
                                                OWLDisjointUnionAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<Set<OWLClassExpression>> getObjectEditor() {
        return new OWLClassExpressionSetEditor(getOWLEditorKit(), manipulatableObjects().collect(Collectors.toList()));
    }

    @Override
    protected OWLDisjointUnionAxiom createAxiom(Set<OWLClassExpression> editedObject) {
        return getOWLDataFactory().getOWLDisjointUnionAxiom(getRoot(), editedObject);
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLClassExpression>> editor) {
        return Objects.requireNonNull(editor.getEditedObject()).size() >= 2;
    }

    @Override
    public Stream<OWLClassExpression> manipulatableObjects() {
        return getAxiom().classExpressions();
    }
}
