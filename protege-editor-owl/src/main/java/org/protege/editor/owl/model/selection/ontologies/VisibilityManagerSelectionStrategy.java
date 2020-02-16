package org.protege.editor.owl.model.selection.ontologies;

import org.protege.editor.owl.model.OntologyVisibilityManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 6, 2008<br><br>
 */
public class VisibilityManagerSelectionStrategy implements OntologySelectionStrategy {
    private final OntologyVisibilityManager vm;

    public VisibilityManagerSelectionStrategy(OntologyVisibilityManager vm) {
        this.vm = Objects.requireNonNull(vm);
    }

    @Override
    public Set<OWLOntology> getOntologies() {
        return vm.getVisibleOntologies();
    }

    @Override
    public Stream<OWLOntology> ontologies() {
        return vm.getVisibleOntologies().stream();
    }

    @Override
    public String getName() {
        return "Show user selected ontologies...";
    }
}
