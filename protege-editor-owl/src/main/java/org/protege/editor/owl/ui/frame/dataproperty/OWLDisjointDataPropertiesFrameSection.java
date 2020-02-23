package org.protege.editor.owl.ui.frame.dataproperty;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLDataPropertySetEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 16-Feb-2007<br><br>
 */
public class OWLDisjointDataPropertiesFrameSection
        extends AbstractOWLFrameSection<OWLDataProperty, OWLDisjointDataPropertiesAxiom, Set<OWLDataProperty>> {

    public static final String LABEL = "Disjoint With";

    public OWLDisjointDataPropertiesFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLDataProperty> frame) {
        super(editorKit, LABEL, "Disjoint properties", frame);
    }

    @Override
    protected OWLDisjointDataPropertiesAxiom createAxiom(Set<OWLDataProperty> object) {
        Set<OWLDataProperty> disjoints = new HashSet<>(object);
        disjoints.add(getRootObject());
        return getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(disjoints);
    }

    @Override
    protected void clear() {
    }

    @Override
    protected void refill(OWLOntology ontology) {
        OWLDataProperty root = getRootObject();
        ontology.disjointDataPropertiesAxioms(root)
                .map(ax -> new OWLDisjointDataPropertiesFrameSectionRow(getOWLEditorKit(), this, ontology, root, ax))
                .forEach(this::addRow);
    }

    @Override
    public OWLObjectEditor<Set<OWLDataProperty>> getObjectEditor() {
        return new OWLDataPropertySetEditor(getOWLEditorKit());
    }

    @Override
    public boolean checkEditorResults(OWLObjectEditor<Set<OWLDataProperty>> editor) {
        return !Objects.requireNonNull(editor.getEditedObject()).contains(getRootObject());
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && change.getAxiom() instanceof OWLDisjointDataPropertiesAxiom
                && ((OWLDisjointDataPropertiesAxiom) change.getAxiom()).properties().anyMatch(x -> x.equals(getRootObject()));
    }
}
