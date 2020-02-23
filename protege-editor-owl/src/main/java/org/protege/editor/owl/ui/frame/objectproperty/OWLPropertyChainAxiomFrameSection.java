package org.protege.editor.owl.ui.frame.objectproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectPropertyChainEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.*;

import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLPropertyChainAxiomFrameSection
        extends AbstractOWLFrameSection<OWLObjectProperty, OWLSubPropertyChainOfAxiom, List<OWLObjectPropertyExpression>> {

    public static final String LABEL = "SuperProperty Of (Chain)";

    public OWLPropertyChainAxiomFrameSection(OWLEditorKit owlEditorKit, OWLFrame<? extends OWLObjectProperty> frame) {
        super(owlEditorKit, LABEL, "Property chain", frame);
        setCacheEditor(false); // needs to be recreated every time
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLObjectProperty root = getRootObject();
        ontology.axioms(AxiomType.SUB_PROPERTY_CHAIN_OF)
                .filter(ax -> ax.getSuperProperty().equals(root))
                .map(ax -> new OWLPropertyChainAxiomFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    protected OWLSubPropertyChainOfAxiom createAxiom(List<OWLObjectPropertyExpression> object) {
        return getOWLDataFactory().getOWLSubPropertyChainOfAxiom(object, getRootObject());
    }

    @Override
    public OWLObjectEditor<List<OWLObjectPropertyExpression>> getObjectEditor() {
        OWLObjectPropertyChainEditor editor = new OWLObjectPropertyChainEditor(getOWLEditorKit());
        editor.setSuperProperty(getRootObject());
        return editor;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
    	if (!change.isAxiomChange()) {
    		return false;
    	}
    	OWLAxiom axiom = change.getAxiom();
    	if (axiom instanceof OWLSubPropertyChainOfAxiom) {
    		return ((OWLSubPropertyChainOfAxiom) axiom).getSuperProperty().equals(getRootObject());
    	}
    	return false;
    }
}
