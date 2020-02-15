package org.protege.editor.owl.model.classexpression.anonymouscls;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 7, 2009<br><br>
 */
public class ADCRewriter {

    private final AnonymousDefinedClassManager adcManager;
    private final OWLObjectDuplicator duplicator;

    public ADCRewriter(AnonymousDefinedClassManager adcManager, OWLOntologyManager om) {
        this.adcManager = adcManager;
        this.duplicator = new OWLObjectDuplicator(om);
    }

    public List<OWLOntologyChange> rewriteChanges(List<? extends OWLOntologyChange> changes) {
        List<OWLOntologyChange> rewrittenChanges = new ArrayList<>();
        for (OWLOntologyChange chg : changes) {
            rewrittenChanges.add(rewriteChange(chg));
        }
        return rewrittenChanges;
    }

    public OWLOntologyChange rewriteChange(OWLOntologyChange chg) {
        if (!chg.isAxiomChange()) {
            return chg;
        }
        if (chg.getAxiom().signature()
                .noneMatch(e -> e.isOWLClass() && adcManager.isAnonymous(e.asOWLClass()))) {
            return chg;
        }
        if (chg instanceof AddAxiom) {
            return new AddAxiom(chg.getOntology(), rewriteAxiom(chg.getAxiom()));
        }
        if (chg instanceof RemoveAxiom) {
            return new RemoveAxiom(chg.getOntology(), rewriteAxiom(chg.getAxiom()));
        }
        return chg;
    }

    private OWLAxiom rewriteAxiom(OWLAxiom axiom) {
        return duplicator.duplicateObject(axiom);
    }
}
