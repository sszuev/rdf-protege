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
public class OWLObjectPropertyHierarchyProvider
        extends AbstractOWLPropertyHierarchyProvider<OWLObjectPropertyExpression, OWLObjectProperty> {

    public OWLObjectPropertyHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
    }

    @Override
    protected Set<OWLObjectProperty> getPropertiesReferencedInChange(List<? extends OWLOntologyChange> changes) {
        return changes.stream().filter(OWLOntologyChange::isAxiomChange)
                .flatMap(HasSignature::signature)
                .filter(AsOWLObjectProperty::isOWLObjectProperty)
                .map(AsOWLObjectProperty::asOWLObjectProperty)
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
    protected Set<? extends OWLObjectProperty> getReferencedProperties(OWLOntology ont) {
        return ont.objectPropertiesInSignature().collect(Collectors.toSet());
    }

    @Override
    protected OWLObjectProperty getRoot() {
        return getManager().getOWLDataFactory().getOWLTopObjectProperty();
    }

    @Override
    protected boolean containsReference(OWLOntology ont, OWLObjectProperty prop) {
        return ont.containsObjectPropertyInSignature(prop.getIRI());
    }

    @Override
    protected Collection<OWLObjectProperty> getSuperProperties(OWLObjectProperty subProperty, Set<OWLOntology> ontologies) {
        return EntitySearcher.getSuperProperties(subProperty, ontologies.stream())
                .filter(p -> !p.isAnonymous())
                .collect(toList());
    }

    @Override
    protected Collection<OWLObjectProperty> getSubProperties(OWLObjectProperty superProp, Set<OWLOntology> ontologies) {
        return ontologies.stream()
                .flatMap(x -> x.objectSubPropertyAxiomsForSuperProperty(superProp))
                .map(OWLSubPropertyAxiom::getSubProperty)
                .filter(p -> !p.isAnonymous())
                .map(AsOWLObjectProperty::asOWLObjectProperty)
                .collect(Collectors.toList());
    }
}
