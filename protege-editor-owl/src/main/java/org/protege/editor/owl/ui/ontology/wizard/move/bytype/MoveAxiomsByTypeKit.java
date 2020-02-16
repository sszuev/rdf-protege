package org.protege.editor.owl.ui.ontology.wizard.move.bytype;

import org.protege.editor.owl.ui.ontology.wizard.move.FilteredAxiomsModel;
import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKit;
import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKitConfigurationPanel;
import org.protege.editor.owl.ui.ontology.wizard.move.SelectAxiomsPanel;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 28, 2008<br><br>
 */
public class MoveAxiomsByTypeKit extends MoveAxiomsKit implements FilteredAxiomsModel {

    private Set<AxiomType<?>> types;

    private AxiomTypeSelectorPanel axiomTypeSelectorPanel;
    private SelectAxiomsPanel selectAxiomsPanel;
    private Set<OWLAxiom> axioms;

    @Override
    public List<MoveAxiomsKitConfigurationPanel> getConfigurationPanels() {
        List<MoveAxiomsKitConfigurationPanel> panels = new ArrayList<>();
        panels.add(axiomTypeSelectorPanel);
        panels.add(selectAxiomsPanel);
        return panels;
    }

    @Override
    public Set<OWLAxiom> getAxioms(Set<OWLOntology> sourceOntologies) {
        return axioms;
    }

    @Override
    public void initialise() throws Exception {
        types = new HashSet<>();
        axiomTypeSelectorPanel = new AxiomTypeSelectorPanel(this);
        selectAxiomsPanel = new SelectAxiomsPanel(this, "axioms.type");
    }

    @Override
    public void dispose() throws Exception {
        axiomTypeSelectorPanel.dispose();
    }

    public void setTypes(Set<AxiomType<?>> types) {
        this.types.clear();
        this.types.addAll(types);
    }

    @Override
    public void setFilteredAxioms(Set<OWLAxiom> axioms) {
        this.axioms = axioms;
    }

    @Override
    public Set<OWLAxiom> getUnfilteredAxioms(Set<OWLOntology> sourceOntologies) {
        Set<OWLAxiom> res = new HashSet<>();
        for (OWLOntology ont : sourceOntologies) {
            types.forEach(x -> ont.axioms(x).forEach(res::add));
        }
        return res;
    }
}
