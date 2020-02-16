package org.protege.editor.owl.ui.ontology.wizard.move.byprofile;

import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKit;
import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKitConfigurationPanel;
import org.semanticweb.owlapi.model.HasAxioms;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Dec 3, 2008<br><br>
 */
public class MoveAxiomsByProfileKit extends MoveAxiomsKit {

    private ProfileSelectorPanel profileSelectorPanel;
    private OWLProfile profile;

    @Override
    public List<MoveAxiomsKitConfigurationPanel> getConfigurationPanels() {
        List<MoveAxiomsKitConfigurationPanel> panels = new ArrayList<>();
        panels.add(profileSelectorPanel);
        return panels;
    }

    @Override
    public Set<OWLAxiom> getAxioms(Set<OWLOntology> sourceOntologies) {
        Set<OWLAxiom> axioms = sourceOntologies.stream().flatMap(HasAxioms::axioms).collect(Collectors.toSet());
        sourceOntologies.stream().map(o -> profile.checkOntology(o)).flatMap(r -> r.getViolations().stream())
                .map(OWLProfileViolation::getAxiom).forEach(axioms::remove);
        return axioms;
    }

    @Override
    public void initialise() throws Exception {
        profileSelectorPanel = new ProfileSelectorPanel(this);
    }

    @Override
    public void dispose() throws Exception {
        profileSelectorPanel.dispose();
    }

    public void setProfile(OWLProfile profile) {
        this.profile = profile;
    }
}
