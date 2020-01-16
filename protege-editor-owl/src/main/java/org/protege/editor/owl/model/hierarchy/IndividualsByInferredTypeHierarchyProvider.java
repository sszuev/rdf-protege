package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 24-May-2007<br><br>
 */
public class IndividualsByInferredTypeHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLObject> {

    private final Map<OWLObject, Set<OWLObject>> typeNodes = new HashMap<>();
    private OWLReasoner reasoner;
    private final boolean showDirect = true;


    public IndividualsByInferredTypeHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
    }

    public void setReasoner(OWLReasoner reasoner) {
        this.reasoner = reasoner;
        rebuild();
    }

    private void rebuild() {
        typeNodes.clear();
        if (reasoner == null) {
            fireHierarchyChanged();
            return;
        }
        reasoner.getRootOntology().importsClosure()
                .flatMap(HasClassesInSignature::classesInSignature)
                .forEach(x -> {
                    Set<OWLObject> inds = reasoner.getInstances(x, showDirect)
                            .entities().collect(Collectors.toSet());
                    if (!inds.isEmpty()) {
                        typeNodes.put(x, inds);
                    }
                });
        fireHierarchyChanged();
    }

    @Override
    public void setOntologies(Set<OWLOntology> ontologies) {
        throw new RuntimeException("Use setReasoner()");
    }

    @Override
    public Set<OWLObject> getRoots() {
        return typeNodes.keySet();
    }

    @Override
    public Set<OWLObject> getUnfilteredChildren(OWLObject object) {
        return reasoner != null && typeNodes.containsKey(object) ? typeNodes.get(object) : Collections.emptySet();
    }

    @Override
    public Set<OWLObject> getParents(OWLObject object) {
        if (reasoner != null && typeNodes.containsKey(object)) {
            return Collections.emptySet();
        }
        if (reasoner == null) throw new IllegalStateException();
        OWLNamedIndividual ind = (OWLNamedIndividual) object;
        Set<OWLObject> res = new HashSet<>();
        reasoner.getTypes(ind, showDirect).entities().forEach(res::add);
        return res;
    }

    @Override
    public Set<OWLObject> getEquivalents(OWLObject object) {
        return Collections.emptySet();
    }

    @Override
    public boolean containsReference(OWLObject object) {
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}