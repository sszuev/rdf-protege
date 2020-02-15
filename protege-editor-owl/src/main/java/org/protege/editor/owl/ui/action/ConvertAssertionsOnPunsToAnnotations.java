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
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Jul-2007<br><br>
 */
public class ConvertAssertionsOnPunsToAnnotations extends ProtegeOWLAction {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConvertAssertionsOnPunsToAnnotations.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLDataFactory df = getOWLDataFactory();
        Set<OWLOntology> ontologies = getOWLModelManager().getOntologies();

        List<OWLOntologyChange> changes = new ArrayList<>();
        Set<OWLIndividual> individuals = ontologies.stream()
                .flatMap(HasIndividualsInSignature::individualsInSignature).collect(Collectors.toSet());

        Set<OWLDataProperty> props = new HashSet<>();
        for (OWLIndividual ind : individuals) {
            if (!isPunForClass(ind)) {
                continue;
            }
            OWLNamedIndividual pun = ind.asOWLNamedIndividual();
            ontologies.forEach(ont -> ont.dataPropertyAssertionAxioms(pun)
                    .filter(x -> !x.getProperty().isAnonymous())
                    .forEach(ax -> {
                        changes.add(new RemoveAxiom(ont, ax));
                        OWLAnnotationProperty p = df.getOWLAnnotationProperty(ax.getProperty().asOWLDataProperty().getIRI());
                        OWLAnnotation a = df.getOWLAnnotation(p, ax.getObject());
                        OWLAnnotationAssertionAxiom annoAx = df.getOWLAnnotationAssertionAxiom(pun.getIRI(), a);
                        changes.add(new AddAxiom(ont, annoAx));
                        props.add((OWLDataProperty) ax.getProperty());
                    }));
            ontologies.forEach(ont -> {
                ont.declarationAxioms(pun).forEach(ax -> changes.add(new RemoveAxiom(ont, ax)));
                ont.classAssertionAxioms(pun).forEach(ax -> changes.add(new RemoveAxiom(ont, ax)));
            });
            ontologies.forEach(ont -> props.forEach(prop -> {
                ont.declarationAxioms(prop).forEach(ax -> changes.add(new RemoveAxiom(ont, ax)));
                ont.axioms(prop).forEach(ax -> changes.add(new RemoveAxiom(ont, ax)));
            }));
        }
        getOWLModelManager().applyChanges(changes);

        if (LOGGER.isTraceEnabled()) {
            ontologies.forEach(ont -> ont.dataPropertiesInSignature().forEach(p -> {
                ont.referencingAxioms(p).forEach(ax -> LOGGER.trace("Axiom: {}", ax));
            }));
        }
    }

    private boolean isPunForClass(OWLIndividual ind) {
        if (ind.isAnonymous()) {
            return false;
        }
        for (OWLOntology ont : getOWLModelManager().getOntologies()) {
            if (ont.containsClassInSignature(ind.asOWLNamedIndividual().getIRI())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispose() throws Exception {
    }

    @Override
    public void initialise() throws Exception {
    }
}
