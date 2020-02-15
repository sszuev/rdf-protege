package org.protege.editor.owl.ui.action;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Nick Drummond<br>
 * The University Of Manchester<br>
 * BioHealth Informatics Group<br>
 * Date: May 19, 2008
 */
public class ConvertMinOneToSomeValuesFromAction extends ProtegeOWLAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertMinOneToSomeValuesFromAction.class);

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        MinCardiOneReplacer replacer = new MinCardiOneReplacer(getOWLModelManager().getOWLOntologyManager());
        List<OWLOntologyChange> changes = new ArrayList<>();
        getOWLModelManager().getActiveOntologies().forEach(ont -> ont.logicalAxioms().forEach(ax -> {
            // duplicates, but switching min 1 with svf
            OWLAxiom ax2 = replacer.duplicateObject(ax);
            // so if they are different, the axiom using the svf
            // needs to replace the axiom using the min 1 in the ontology
            if (ax.equals(ax2)) {
                return;
            }
            changes.add(new RemoveAxiom(ont, ax));
            changes.add(new AddAxiom(ont, ax2));
        }));
        getOWLModelManager().applyChanges(changes);
        LOGGER.info("Converted {} qualified min 1 restrictions to someValuesFrom restrictions", changes.size() / 2);
    }

    @Override
    public void initialise() throws Exception {
        // do nothing
    }

    @Override
    public void dispose() throws Exception {
        // do nothing
    }

    /**
     * A variant of the duplicator that changes qualified MinCardi1
     * restrictions into someValueFrom restrictions
     */
    static class MinCardiOneReplacer extends OWLObjectDuplicator {
        private final OWLDataFactory df;

        public MinCardiOneReplacer(OWLOntologyManager manager) {
            super(manager);
            df = manager.getOWLDataFactory();
        }

        @Override
        public OWLObjectMinCardinality visit(OWLObjectMinCardinality min) {
            if (min.getCardinality() != 1 || !min.isQualified()) {
                return super.visit(min);
            }
            OWLObjectSomeValuesFrom someValuesFrom = df.getOWLObjectSomeValuesFrom(min.getProperty(), min.getFiller());
            return (OWLObjectMinCardinality) visit(someValuesFrom);
        }

        @Override
        public OWLDataMinCardinality visit(OWLDataMinCardinality min) {
            if (min.getCardinality() != 1 || !min.isQualified()) {
                return super.visit(min);
            }
            OWLDataSomeValuesFrom someValuesFrom = df.getOWLDataSomeValuesFrom(min.getProperty(), min.getFiller());
            return (OWLDataMinCardinality) visit(someValuesFrom);
        }
    }
}
