package org.github.owlcs.ontapi.metrics;

import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.jena.model.OntModel;
import org.apache.jena.graph.Graph;
import org.semanticweb.owlapi.metrics.IntegerValuedMetric;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;

/**
 * An impl of {@link org.semanticweb.owlapi.metrics.OWLMetric} to calculate triples count.
 * Created by @ssz on 07.01.2020.
 */
public class TripleCount extends IntegerValuedMetric {

    public TripleCount(OWLOntology o) {
        super(o);
    }

    @Override
    public Ontology getOntology() {
        return (Ontology) super.getOntology();
    }

    @Override
    protected Integer recomputeMetric() {
        return getGraph().size();
    }

    protected Graph getGraph() {
        OntModel m = getOntology().asGraphModel();
        return isImportsClosureUsed() ? m.getGraph() : m.getBaseGraph();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected boolean isMetricInvalidated(List<? extends OWLOntologyChange> changes) {
        return true;
    }

    @Override
    public String getName() {
        return "Triple";
    }

}
