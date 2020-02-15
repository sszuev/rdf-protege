package org.protege.editor.owl.model.classexpression.anonymouscls;

import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Date: Jan 8, 2009<br><br>
 */
public class ADCFactory implements OWLObjectVisitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ADCFactory.class);

    private final AnonymousDefinedClassManager adcManager;
    private final Set<OWLClassExpression> classes = new HashSet<>();

    public ADCFactory(AnonymousDefinedClassManager adcManager) {
        this.adcManager = adcManager;
    }

    @SuppressWarnings("UnusedReturnValue")
    public List<OWLOntologyChange> getADCsForOntology(OWLOntology ont) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        classes.clear();
        ont.generalClassAxioms().forEach(x -> x.accept(ADCFactory.this));
        ont.annotations().forEach(x -> x.accept(ADCFactory.this)); // get annotations on ontology

        for (OWLClassExpression d : classes) {
            OWLEntityCreationSet<OWLClass> chSet = adcManager.createAnonymousClass(ont, d);
            changes.addAll(chSet.getOntologyChanges());
        }
        return changes;
    }

    @Override
    public void visit(OWLSubClassOfAxiom ax) {
        if (ax.getSubClass().isAnonymous()) {
            classes.add(ax.getSubClass());
        }
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom ax) {
        ax.classExpressions().filter(IsAnonymous::isAnonymous).forEach(classes::add);
    }

    @Override
    public void visit(OWLAnnotation annotation) {
        if (annotation.getProperty().getIRI().toURI().equals(adcManager.getURI())) {
            annotation.getValue().accept(this);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void visit(OWLLiteral node) { //TODO: wtf?
        LOGGER.error("An error occurred whilst parsing a literal in the ADCFactory");
    }
}
