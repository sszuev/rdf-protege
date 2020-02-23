package org.protege.editor.owl.ui.renderer;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.renderer.context.DefinedClassChecker;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.annotation.Nonnull;
import javax.swing.*;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Apr 2, 2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
@SuppressWarnings("NullableProblems")
public class OWLIconProviderImpl implements OWLObjectVisitor, OWLIconProvider {

    private Icon icon;

    private final Icon primitiveClassIcon = new OWLClassIcon(OWLClassIcon.Type.PRIMITIVE);//OWLIcons.getIcon("class.primitive.png");
    private final Icon definedClassIcon = new OWLClassIcon(OWLClassIcon.Type.DEFINED);
    private final Icon objectPropertyIcon = new OWLObjectPropertyIcon();
    private final Icon dataPropertyIcon = new OWLDataPropertyIcon();
    private final Icon annotationPropertyIcon = new OWLAnnotationPropertyIcon();
    private final Icon individualIcon = new OWLIndividualIcon(OWLEntityIcon.FillType.FILLED);
    private final Icon dataTypeIcon = new OWLDatatypeIcon();
    private final Icon ontologyIcon = OWLIcons.getIcon("ontology.png");
    //private final Icon ontologyMissing = OWLIcons.getIcon("ontology.missing.png");

    private final DefinedClassChecker definedClassChecker;

    @Deprecated
    public OWLIconProviderImpl(@Nonnull final OWLModelManager owlModelManager) {
        definedClassChecker = c -> {
            for (OWLOntology ont : owlModelManager.getActiveOntologies()) {
                if (isDefined(c, ont)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Creates an icon provider implementation.
     *
     * @param definedClassChecker A checker that can be used to determine whether or not a class is a defined class.
     */
    public OWLIconProviderImpl(@Nonnull DefinedClassChecker definedClassChecker) {
        this.definedClassChecker = checkNotNull(definedClassChecker);
    }

    private static boolean isDefined(OWLClass owlClass, OWLOntology ontology) {
        if (EntitySearcher.isDefined(owlClass, ontology)) {
            return true;
        }
        return ontology.disjointUnionAxioms(owlClass).findFirst().isPresent();
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public Icon getIcon(OWLObject owlObject) {
        try {
            icon = null;
            owlObject.accept(this);
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void visit(OWLObjectIntersectionOf owlAnd) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDatatype owlDatatype) {
        icon = dataTypeIcon;
    }

    @Override
    public void visit(OWLDataOneOf owlDataEnumeration) {
        icon = dataTypeIcon;
    }

    @Override
    public void visit(OWLDataAllValuesFrom owlDataAllRestriction) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDataProperty owlDataProperty) {
        icon = dataPropertyIcon;
    }

    @Override
    public void visit(OWLDataSomeValuesFrom owlDataSomeValuesFrom) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDataHasValue owlDataValueRestriction) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom owlDifferentIndividualsAxiom) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLDisjointDataPropertiesAxiom owlDisjointDataPropertiesAxiom) {
        icon = dataPropertyIcon;
    }

    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLHasKeyAxiom owlHasKeyAxiom) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDatatypeDefinitionAxiom owlDatatypeDefinitionAxiom) {
        icon = dataTypeIcon;
    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom owlEquivalentObjectPropertiesAxiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom owlNegativeDataPropertyAssertionAxiom) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLNamedIndividual owlIndividual) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLAnonymousIndividual individual) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLObjectAllValuesFrom owlObjectAllRestriction) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectMinCardinality desc) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectExactCardinality desc) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectMaxCardinality desc) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectHasSelf desc) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDataMinCardinality desc) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDataExactCardinality desc) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDataMaxCardinality desc) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectProperty owlObjectProperty) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectHasValue owlObjectValueRestriction) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectComplementOf owlNot) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLOntology owlOntology) {
        icon = ontologyIcon;
    }

    @Override
    public void visit(OWLObjectUnionOf owlOr) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDeclarationAxiom owlDeclarationAxiom) {
        owlDeclarationAxiom.getEntity().accept(this);
    }

    @Override
    public void visit(OWLSubClassOfAxiom owlSubClassAxiom) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom owlNegativeObjectPropertyAssertionAxiom) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom owlAntiSymmetricObjectPropertyAxiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom owlReflexiveObjectPropertyAxiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLDisjointClassesAxiom owlDisjointClassesAxiom) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDataPropertyDomainAxiom owlDataPropertyDomainAxiom) {
        icon = dataPropertyIcon;
    }

    @Override
    public void visit(OWLDisjointUnionAxiom owlDisjointUnionAxiom) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom owlSymmetricObjectPropertyAxiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLDataPropertyRangeAxiom owlDataPropertyRangeAxiom) {
        icon = dataPropertyIcon;
    }

    @Override
    public void visit(OWLFunctionalDataPropertyAxiom owlFunctionalDataPropertyAxiom) {
        icon = dataPropertyIcon;
    }

    @Override
    public void visit(OWLEquivalentDataPropertiesAxiom owlEquivalentDataPropertiesAxiom) {
        icon = dataPropertyIcon;
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom owlTransitiveObjectPropertyAxiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom owlIrreflexiveObjectPropertyAxiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLSubDataPropertyOfAxiom owlDataSubPropertyAxiom) {
        icon = dataPropertyIcon;
    }

    @Override
    public void visit(OWLSameIndividualAxiom owlSameIndividualsAxiom) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLClassAssertionAxiom owlClassAssertionAxiom) {
        icon = individualIcon;
    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        icon = objectPropertyIcon;
    }

    @Override
    public void visit(OWLClass owlClass) {
        icon = definedClassChecker.isDefinedClass(owlClass) ? definedClassIcon : primitiveClassIcon;
    }

    @Override
    public void visit(OWLObjectOneOf owlEnumeration) {
        icon = primitiveClassIcon;
    }

    @Override
    public void visit(OWLAnnotationProperty owlAnnotationProperty) {
        icon = annotationPropertyIcon;
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom owlAnnotationAssertionAxiom) {
        icon = annotationPropertyIcon;
    }

    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom owlSubAnnotationPropertyOfAxiom) {
        icon = annotationPropertyIcon;
    }

    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom owlAnnotationPropertyDomainAxiom) {
        icon = annotationPropertyIcon;
    }

    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom owlAnnotationPropertyRangeAxiom) {
        icon = annotationPropertyIcon;
    }
}
