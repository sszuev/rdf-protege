package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.frame.property.AbstractPropertyDomainFrameSectionRow;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLObjectPropertyDomainFrameSectionRow
        extends AbstractPropertyDomainFrameSectionRow<OWLObjectProperty, OWLObjectPropertyDomainAxiom> {


    public OWLObjectPropertyDomainFrameSectionRow(OWLEditorKit kit,
                                                  OWLFrameSection<OWLObjectProperty, OWLObjectPropertyDomainAxiom, OWLClassExpression> section,
                                                  OWLOntology ontology, OWLObjectProperty rootObject,
                                                  OWLObjectPropertyDomainAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectPropertyDomainAxiom createAxiom(OWLClassExpression editedObject) {
        return getOWLDataFactory().getOWLObjectPropertyDomainAxiom(getRoot(), editedObject);
    }
}
