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
public class OWLObjectPropertyHierarchyProvider
        extends AbstractOWLPropertyHierarchyProvider<OWLObjectPropertyExpression, OWLObjectProperty> {

    public OWLObjectPropertyHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
    }

    @Override
    protected Stream<OWLObjectProperty> propertiesReferencedInChange(List<? extends OWLOntologyChange> changes) {
        return changes.stream()
                .filter(OWLOntologyChange::isAxiomChange)
                .flatMap(HasSignature::signature)
                .filter(AsOWLObjectProperty::isOWLObjectProperty)
                .map(AsOWLObjectProperty::asOWLObjectProperty);
    }

    @Override
    protected Stream<? extends OWLObjectProperty> referencedProperties(OWLOntology ont) {
        return ont.objectPropertiesInSignature();
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
    protected Stream<OWLObjectProperty> superProperties(OWLObjectProperty subProperty, Set<OWLOntology> ontologies) {
        return EntitySearcher.getSuperProperties(subProperty, ontologies.stream())
                .filter(p -> !p.isAnonymous());
    }

    @Override
    protected Stream<OWLObjectProperty> subProperties(OWLObjectProperty superProp, Set<OWLOntology> ontologies) {
        return ontologies.stream()
                .flatMap(x -> x.objectSubPropertyAxiomsForSuperProperty(superProp))
                .map(OWLSubPropertyAxiom::getSubProperty)
                .filter(p -> !p.isAnonymous())
                .map(AsOWLObjectProperty::asOWLObjectProperty);
    }
}
