package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public class OWLSubClassAxiomFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLClassExpression, OWLSubClassOfAxiom, OWLClassExpression> {

    public OWLSubClassAxiomFrameSectionRow(OWLEditorKit kit,
                                           OWLFrameSection<OWLClassExpression, OWLSubClassOfAxiom, OWLClassExpression> section,
                                           OWLOntology ontology,
                                           OWLClassExpression rootObject,
                                           OWLSubClassOfAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return getOWLComponentFactory().getOWLClassDescriptionEditor(getAxiom().getSuperClass(), AxiomType.SUBCLASS_OF);
    }

    @Override
    protected OWLSubClassOfAxiom createAxiom(OWLClassExpression editedObject) {
        return getOWLDataFactory().getOWLSubClassOfAxiom(getRoot(), editedObject);
    }

    @Override
    public Stream<OWLClassExpression> manipulatableObjects() {
        return Stream.of(getAxiom().getSuperClass());
    }
}
