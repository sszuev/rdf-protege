package org.protege.editor.owl.ui.frame.annotationproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLAnnotationPropertyDomainEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 4, 2009<br><br>
 */
public class OWLAnnotationPropertyDomainFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLAnnotationProperty, OWLAnnotationPropertyDomainAxiom, IRI> {

    public OWLAnnotationPropertyDomainFrameSectionRow(OWLEditorKit kit,
                                                      OWLFrameSection<OWLAnnotationProperty, OWLAnnotationPropertyDomainAxiom, IRI> section,
                                                      OWLOntology ontology,
                                                      OWLAnnotationProperty property,
                                                      OWLAnnotationPropertyDomainAxiom axiom) {
        super(kit, section, ontology, property, axiom);
    }

    @Override
    protected OWLAnnotationPropertyDomainEditor getObjectEditor() {
        final OWLAnnotationPropertyDomainEditor editor = new OWLAnnotationPropertyDomainEditor(getOWLEditorKit());
        editor.setEditedObject(getAxiom().getDomain());
        return editor;
    }

    @Override
    protected OWLAnnotationPropertyDomainAxiom createAxiom(IRI iri) {
        return getOWLDataFactory().getOWLAnnotationPropertyDomainAxiom(getRoot(), iri);
    }

    @Override
    public Stream<IRI> manipulatableObjects() {
        return Stream.of(getAxiom().getDomain());
    }
}
