package org.protege.editor.owl.ui.ontology.annotation;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OntologyAnnotationContainer;
import org.protege.editor.owl.ui.list.AbstractAnnotationsList;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

import java.util.ArrayList;
import java.util.List;
/*
 * Copyright (C) 2007, University of Manchester
 *
 *
 */

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 1, 2009<br><br>
 */
public class OWLOntologyAnnotationList extends AbstractAnnotationsList<OntologyAnnotationContainer> {

    public OWLOntologyAnnotationList(OWLEditorKit eKit) {
        super(eKit);
    }

    @Override
    protected List<OWLOntologyChange> getAddChanges(OWLAnnotation annot) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddOntologyAnnotation(getRoot().getOntology(), annot));
        return changes;
    }

    @Override
    protected List<OWLOntologyChange> getReplaceChanges(OWLAnnotation oldAnnotation, OWLAnnotation newAnnotation) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new RemoveOntologyAnnotation(getRoot().getOntology(), oldAnnotation));
        changes.add(new AddOntologyAnnotation(getRoot().getOntology(), newAnnotation));
        return changes;
    }

    @Override
    protected List<OWLOntologyChange> getDeleteChanges(OWLAnnotation annotation) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new RemoveOntologyAnnotation(getRoot().getOntology(), annotation));
        return changes;
    }

    @Override
    protected void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            if (!(change instanceof AddOntologyAnnotation) && !(change instanceof RemoveOntologyAnnotation)) {
                continue;
            }
            if (!change.getOntology().equals(getRoot().getOntology())) {
                continue;
            }
            refresh();
            return;
        }
    }
}
