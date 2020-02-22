package org.protege.editor.owl.model;

import org.semanticweb.owlapi.model.HasAnnotations;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 8, 2009<br><br>
 */
public class OntologyAnnotationContainer implements HasAnnotations {

    private final OWLOntology ont;

    public OntologyAnnotationContainer(OWLOntology ont) {
        this.ont = Objects.requireNonNull(ont);
    }

    public OWLOntology getOntology() {
        return ont;
    }

    @Override
    public Stream<OWLAnnotation> annotations() {
        return ont.annotations();
    }
}
