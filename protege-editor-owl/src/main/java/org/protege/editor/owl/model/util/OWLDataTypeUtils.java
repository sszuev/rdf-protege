package org.protege.editor.owl.model.util;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Sep 11, 2008<br><br>
 */
public class OWLDataTypeUtils {

    private final OWLOntologyManager manager;

    public OWLDataTypeUtils(OWLOntologyManager mngr) {
        this.manager = Objects.requireNonNull(mngr);
    }

    public Set<OWLDatatype> getBuiltinDatatypes() {
        Set<OWLDatatype> datatypes = new HashSet<>();
        OWLDataFactory df = manager.getOWLDataFactory();
        datatypes.add(df.getTopDatatype());
        for (OWL2Datatype dt : OWL2Datatype.values()) {
            datatypes.add(df.getOWLDatatype(dt.getIRI()));
        }
        return datatypes;
    }

    public Stream<OWLDatatype> builtinDatatypes() {
        return getBuiltinDatatypes().stream();
    }

    public Set<OWLDatatype> getKnownDatatypes(Set<OWLOntology> ontologies) {
        return datatypes(ontologies).collect(Collectors.toSet());
    }

    public Stream<OWLDatatype> knownDatatypes(Set<OWLOntology> ontologies) {
        return datatypes(ontologies).distinct();
    }

    private Stream<OWLDatatype> datatypes(Set<OWLOntology> ontologies) {
        return Stream.concat(builtinDatatypes(),
                ontologies.stream().flatMap(HasDatatypesInSignature::datatypesInSignature));
    }
}
