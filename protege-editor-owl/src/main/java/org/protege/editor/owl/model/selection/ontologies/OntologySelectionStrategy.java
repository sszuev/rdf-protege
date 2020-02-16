package org.protege.editor.owl.model.selection.ontologies;

import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 6, 2008<br><br>
 */
public interface OntologySelectionStrategy {

    Stream<OWLOntology> ontologies();

    String getName();

    default Set<OWLOntology> getOntologies() {
        return ontologies().collect(Collectors.toSet());
    }
}
