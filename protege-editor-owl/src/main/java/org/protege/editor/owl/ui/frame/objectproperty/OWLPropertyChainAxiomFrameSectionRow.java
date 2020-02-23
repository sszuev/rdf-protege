package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyChainEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import java.util.List;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLPropertyChainAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLObjectProperty, OWLSubPropertyChainOfAxiom, List<OWLObjectPropertyExpression>> {

    public OWLPropertyChainAxiomFrameSectionRow(OWLEditorKit kit,
                                                OWLFrameSection<OWLObjectProperty, OWLSubPropertyChainOfAxiom, List<OWLObjectPropertyExpression>> section,
                                                OWLOntology ontology,
                                                OWLObjectProperty rootObject,
                                                OWLSubPropertyChainOfAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<List<OWLObjectPropertyExpression>> getObjectEditor() {
        OWLObjectPropertyChainEditor editor = new OWLObjectPropertyChainEditor(getOWLEditorKit());
        editor.setAxiom(getAxiom());
        return editor;
    }

    @Override
    protected OWLSubPropertyChainOfAxiom createAxiom(List<OWLObjectPropertyExpression> editedObject) {
        return getOWLDataFactory().getOWLSubPropertyChainOfAxiom(editedObject, getRoot());
    }

    @Override
    public Stream<OWLSubPropertyChainOfAxiom> manipulatableObjects() {
        return Stream.of(getAxiom());
    }
}
