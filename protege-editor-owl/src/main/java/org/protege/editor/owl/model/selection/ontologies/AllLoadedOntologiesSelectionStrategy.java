package org.protege.editor.owl.model.selection.ontologies;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>

 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 6, 2008<br><br>
 */
public class AllLoadedOntologiesSelectionStrategy implements OntologySelectionStrategy {

    private final OWLModelManager mngr;

    public AllLoadedOntologiesSelectionStrategy(OWLModelManager mngr){
        this.mngr = Objects.requireNonNull(mngr);
    }

    @Override
    public Stream<OWLOntology> ontologies() {
        return mngr.getOWLOntologyManager().ontologies();
    }

    @Override
    public String getName() {
        return "Show all loaded ontologies";
    }
}
