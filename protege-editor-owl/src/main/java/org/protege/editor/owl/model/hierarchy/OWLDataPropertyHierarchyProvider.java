package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


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
    protected Stream<OWLDataProperty> propertiesReferencedInChange(List<? extends OWLOntologyChange> changes) {
        return changes.stream()
                .filter(OWLOntologyChange::isAxiomChange)
                .flatMap(HasSignature::signature)
                .filter(AsOWLDataProperty::isOWLDataProperty)
                .map(AsOWLDataProperty::asOWLDataProperty);
    }

    @Override
    protected Stream<? extends OWLDataProperty> referencedProperties(OWLOntology ont) {
        return ont.dataPropertiesInSignature();
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
    protected Stream<OWLDataProperty> superProperties(OWLDataProperty subProperty, Set<OWLOntology> ontologies) {
        return EntitySearcher.getSuperProperties(subProperty, ontologies.stream())
                .filter(p -> !p.isAnonymous());
    }

    @Override
    protected Stream<OWLDataProperty> subProperties(OWLDataProperty superProp, Set<OWLOntology> ontologies) {
        return ontologies.stream()
                .flatMap(x -> x.dataSubPropertyAxiomsForSuperProperty(superProp))
                .map(OWLSubPropertyAxiom::getSubProperty)
                .filter(p -> !p.isAnonymous())
                .map(AsOWLDataProperty::asOWLDataProperty);
    }
}
