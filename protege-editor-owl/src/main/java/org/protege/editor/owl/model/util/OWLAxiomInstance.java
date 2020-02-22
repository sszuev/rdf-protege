package org.protege.editor.owl.model.util;

import com.google.common.base.MoreObjects;
import org.semanticweb.owlapi.model.HasAnnotations;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
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
 * <p>
 * This class is a pair that describes an instance of an axiom with resppect to
 * its containing ontology
 */
public class OWLAxiomInstance implements HasAnnotations {

    private final OWLAxiom ax;
    private final OWLOntology ont;

    public OWLAxiomInstance(OWLAxiom ax, OWLOntology ont) {
        this.ax = Objects.requireNonNull(ax);
        this.ont = Objects.requireNonNull(ont);
    }

    public OWLAxiom getAxiom() {
        return ax;
    }

    public OWLOntology getOntology() {
        return ont;
    }

    @Override
    public Stream<OWLAnnotation> annotations() {
        return ax.annotations();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("OWLAxiomInstance")
                .addValue(ax)
                .addValue(ont.getOntologyID())
                .toString();
    }
}
