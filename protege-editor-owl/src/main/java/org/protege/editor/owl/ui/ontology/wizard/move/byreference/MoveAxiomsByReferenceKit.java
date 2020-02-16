package org.protege.editor.owl.ui.ontology.wizard.move.byreference;

import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKit;
import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKitConfigurationPanel;
import org.protege.editor.owl.ui.ontology.wizard.move.common.SignatureDependentSelectionPreviewPanel;
import org.protege.editor.owl.ui.ontology.wizard.move.common.SignatureSelection;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 23-Sep-2008<br><br>
 */
public class MoveAxiomsByReferenceKit extends MoveAxiomsKit implements SignatureSelection {

    private Set<OWLEntity> selectedEntities;
    private SelectSignaturePanel selectSignaturePanel;
    private SignatureDependentSelectionPreviewPanel selectPreviewPanel;

    @Override
    public List<MoveAxiomsKitConfigurationPanel> getConfigurationPanels() {
        List<MoveAxiomsKitConfigurationPanel> panels = new ArrayList<>();
        panels.add(selectSignaturePanel);
        panels.add(selectPreviewPanel);
        return panels;
    }

    @Override
    public Set<OWLAxiom> getAxioms(Set<OWLOntology> sourceOntologies) {
        return getAxioms(sourceOntologies, selectedEntities);
    }

    @Override
    public Set<OWLAxiom> getAxioms(Set<OWLOntology> ontologies, Set<OWLEntity> entities) {
        Set<OWLAxiom> result = new HashSet<>();
        for (OWLEntity e : entities) {
            for (OWLOntology ont : ontologies) {
                forEntity(ont, e).forEach(result::add);
            }
        }
        return result;
    }

    private static Stream<OWLAxiom> forEntity(OWLOntology ont, OWLEntity e) {
        return Stream.concat(ont.referencingAxioms(e), ont.annotationAssertionAxioms(e.getIRI()));
    }

    @Override
    public void initialise() throws Exception {
        selectedEntities = new HashSet<>();
        selectSignaturePanel = new SelectSignaturePanel(this);

        selectPreviewPanel = new SignatureDependentSelectionPreviewPanel(this);
    }

    @Override
    public void dispose() throws Exception {
    }

    @Override
    public Set<OWLEntity> getSignature() {
        return selectedEntities;
    }

    @Override
    public void setSignature(Set<OWLEntity> entities) {
        selectedEntities.clear();
        selectedEntities.addAll(entities);
    }
}
