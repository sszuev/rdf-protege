package org.protege.editor.owl.ui.action;

import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: Nick Drummond<br>
 * The University Of Manchester<br>
 * BioHealth Informatics Group<br>
 * Date: May 19, 2008
 */
public class SplitDisjointClassesAction extends ProtegeOWLAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitDisjointClassesAction.class);

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        getOWLModelManager().getActiveOntologies().forEach(ont -> ont.axioms(AxiomType.DISJOINT_CLASSES)
                .forEach(ax -> {
                    Set<OWLDisjointClassesAxiom> res = split(ax);
                    if (res.isEmpty()) {
                        return;
                    }
                    changes.add(new RemoveAxiom(ont, ax));
                    res.stream().map(x -> new AddAxiom(ont, x)).forEach(changes::add);
                }));

        getOWLModelManager().applyChanges(changes);
        LOGGER.info("Split {} disjointClasses axioms into {} pairwise axioms",
                changes.stream().filter(OWLOntologyChange::isRemoveAxiom).count(),
                changes.stream().filter(OWLOntologyChange::isAddAxiom).count());
    }

    public Set<OWLDisjointClassesAxiom> split(OWLDisjointClassesAxiom ax) {
        Set<OWLDisjointClassesAxiom> res = new HashSet<>();
        Set<OWLClassExpression> classes = ax.classExpressions().collect(Collectors.toSet());
        if (classes.size() <= 2) {
            return res;
        }
        List<OWLClassExpression> ordered = new ArrayList<>(classes);
        for (int i = 0; i < ordered.size(); i++) {
            OWLClassExpression a = ordered.get(i);
            for (int j = i + 1; j < ordered.size(); j++) {
                OWLClassExpression b = ordered.get(j);
                OWLDisjointClassesAxiom p = getOWLDataFactory().getOWLDisjointClassesAxiom(a, b);
                res.add(p);
            }
        }
        return res;
    }

    @Override
    public void initialise() throws Exception {
        // do nothing
    }

    @Override
    public void dispose() throws Exception {
        // do nothing
    }
}