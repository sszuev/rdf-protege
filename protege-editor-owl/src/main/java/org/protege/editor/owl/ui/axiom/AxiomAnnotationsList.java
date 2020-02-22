package org.protege.editor.owl.ui.axiom;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.util.OWLAxiomInstance;
import org.protege.editor.owl.ui.list.AbstractAnnotationsList;
import org.semanticweb.owlapi.model.*;

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
 * Date: Jun 8, 2009<br><br>
 */
public class AxiomAnnotationsList extends AbstractAnnotationsList<OWLAxiomInstance> {

    private OWLAxiom newAxiom;

    public AxiomAnnotationsList(OWLEditorKit eKit) {
        super(eKit);
    }

    @Override
    protected List<OWLOntologyChange> getAddChanges(OWLAnnotation annotation) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        OWLAxiom oldAxiom = getRoot().getAxiom();

        Set<OWLAnnotation> annotations = oldAxiom.annotations().collect(Collectors.toSet());
        annotations.add(annotation);

        // because for some reason the merge does not work
        newAxiom = oldAxiom.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);

        final OWLOntology ont = getRoot().getOntology();
        changes.add(new RemoveAxiom(ont, oldAxiom));
        changes.add(new AddAxiom(ont, newAxiom));
        return changes;
    }

    @Override
    protected List<OWLOntologyChange> getReplaceChanges(OWLAnnotation oldAnnotation, OWLAnnotation newAnnotation) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        OWLAxiom ax = getRoot().getAxiom();
        OWLOntology ont = getRoot().getOntology();

        Set<OWLAnnotation> annotations = ax.annotations().collect(Collectors.toSet());
        annotations.remove(oldAnnotation);
        annotations.add(newAnnotation);

        newAxiom = ax.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);

        changes.add(new RemoveAxiom(ont, ax));
        changes.add(new AddAxiom(ont, newAxiom));
        return changes;
    }

    @Override
    protected List<OWLOntologyChange> getDeleteChanges(OWLAnnotation annotation) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        OWLAxiom ax = getRoot().getAxiom();
        OWLOntology ont = getRoot().getOntology();

        Set<OWLAnnotation> annotations = ax.annotations().collect(Collectors.toSet());
        annotations.remove(annotation);

        newAxiom = ax.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);

        changes.add(new RemoveAxiom(ont, ax));
        changes.add(new AddAxiom(ont, newAxiom));
        return changes;
    }

    @Override
    protected void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {
        if (newAxiom == null) {
            return;
        }
        // this is complicated by the fact that annotating an axiom produces a new axiom
        for (OWLOntologyChange change : changes) {
            if (!(change instanceof OWLAxiomChange)) {
                continue;
            }
            if (!change.getAxiom().equalsIgnoreAnnotations(getRoot().getAxiom())) {
                continue;
            }
            // @@TODO should check that ontology contains the new axiom
            setRootObject(new OWLAxiomInstance(newAxiom, getRoot().getOntology()));
            newAxiom = null;
            return;
        }
    }
}
