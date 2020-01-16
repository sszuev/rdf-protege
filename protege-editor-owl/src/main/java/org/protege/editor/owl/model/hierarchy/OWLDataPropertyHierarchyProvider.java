package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Jan-2007<br><br>
 */
public class OWLDataPropertyHierarchyProvider
        extends AbstractOWLPropertyHierarchyProvider<OWLDataPropertyExpression, OWLDataProperty> {

    public OWLDataPropertyHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
    }

    @Override
    protected Set<OWLDataProperty> getPropertiesReferencedInChange(List<? extends OWLOntologyChange> changes) {
        return changes.stream().filter(OWLOntologyChange::isAxiomChange)
                .flatMap(HasSignature::signature)
                .filter(AsOWLDataProperty::isOWLDataProperty)
                .map(AsOWLDataProperty::asOWLDataProperty)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the relevant properties in the specified ontology that are contained
     * within the property hierarchy.  For example, for an object property hierarchy
     * this would constitute the set of referenced object properties in the specified
     * ontology.
     *
     * @param ont The ontology
     */
    @Override
    protected Set<? extends OWLDataProperty> getReferencedProperties(OWLOntology ont) {
        return ont.dataPropertiesInSignature().collect(Collectors.toSet());
    }

    @Override
    protected boolean containsReference(OWLOntology ont, OWLDataProperty prop) {
        return ont.containsDataPropertyInSignature(prop.getIRI());
    }

    @Override
    protected OWLDataProperty getRoot() {
        return getManager().getOWLDataFactory().getOWLTopDataProperty();
    }

    @Override
    protected Collection<OWLDataProperty> getSuperProperties(OWLDataProperty subProperty, Set<OWLOntology> ontologies) {
        return EntitySearcher.getSuperProperties(subProperty, ontologies.stream())
                .filter(p -> !p.isAnonymous())
                .collect(toList());
    }

    @Override
    protected Collection<OWLDataProperty> getSubProperties(OWLDataProperty superProp, Set<OWLOntology> ontologies) {
        return ontologies.stream()
                .flatMap(x -> x.dataSubPropertyAxiomsForSuperProperty(superProp))
                .map(OWLSubPropertyAxiom::getSubProperty)
                .filter(p -> !p.isAnonymous())
                .map(AsOWLDataProperty::asOWLDataProperty)
                .collect(Collectors.toList());
    }
}
