package org.protege.editor.owl.ui.ontology.wizard.move.bydefinition;

import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKit;
import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKitConfigurationPanel;
import org.protege.editor.owl.ui.ontology.wizard.move.byreference.SelectSignaturePanel;
import org.protege.editor.owl.ui.ontology.wizard.move.common.SignatureDependentSelectionPreviewPanel;
import org.protege.editor.owl.ui.ontology.wizard.move.common.SignatureSelection;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 19/09/2012
 */
public class MoveAxiomsByDefinitionKit extends MoveAxiomsKit implements SignatureSelection {

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
        Set<OWLAxiom> res = new HashSet<>();
        for (OWLEntity e : entities) {
            for (OWLOntology ont : ontologies) {
                Set<? extends OWLAxiom> axioms = e.accept(streamEntityAxiomVisitor(ont)).collect(Collectors.toSet());
                ont.declarationAxioms(e).forEach(res::add);
                res.addAll(axioms);
                ont.annotationAssertionAxioms(e.getIRI()).forEach(res::add);
            }
        }
        return res;
    }

    @SuppressWarnings("NullableProblems")
    private OWLEntityVisitorEx<Stream<? extends OWLAxiom>> streamEntityAxiomVisitor(OWLOntology ont) {
        return new OWLEntityVisitorEx<Stream<? extends OWLAxiom>>() {

            @Override
            public Stream<? extends OWLAxiom> visit(OWLClass owlClass) {
                return ont.axioms(owlClass);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLObjectProperty property) {
                return ont.axioms(property);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLDataProperty dataProperty) {
                return ont.axioms(dataProperty);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLNamedIndividual individual) {
                return ont.axioms(individual);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLDatatype owlDatatype) {
                return ont.axioms(owlDatatype);
            }

            @Override
            public Stream<? extends OWLAxiom> visit(OWLAnnotationProperty property) {
                return ont.axioms(property);
            }
        };
    }

    @Override
    public void initialise() throws Exception {
        selectedEntities = new HashSet<>();
        selectSignaturePanel = new SelectSignaturePanel(this) {
            @Override
            public String getID() {
                return "MoveAxiomsByDefinition.Select.Signature";
            }
        };

        selectPreviewPanel = new SignatureDependentSelectionPreviewPanel(this) {
            @Override
            public String getID() {
                return "MoveAxiomsByDefinition.Signature.Preview";
            }
        };
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
