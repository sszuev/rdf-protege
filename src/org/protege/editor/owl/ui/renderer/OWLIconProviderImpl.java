package org.protege.editor.owl.ui.renderer;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.OWLIcons;
import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.OWLObjectVisitorAdapter;

import javax.swing.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Apr 2, 2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLIconProviderImpl extends OWLObjectVisitorAdapter implements OWLIconProvider {

    private Icon icon;

    private final Icon primitiveClassIcon = OWLIcons.getIcon("class.primitive.png");

    private final Icon definedClassIcon = OWLIcons.getIcon("class.defined.png");

    private final Icon objectPropertyIcon = OWLIcons.getIcon("property.object.png");

    private final Icon dataPropertyIcon = OWLIcons.getIcon("property.data.png");

    private final Icon individualIcon = OWLIcons.getIcon("individual.png");

    private final Icon dataTypeIcon = OWLIcons.getIcon("datarange.png");

    private final Icon ontologyIcon = OWLIcons.getIcon("ontology.png");

    private final Icon ontologyMissing = OWLIcons.getIcon("ontology.missing.png");


    private OWLModelManager owlModelManager;


    public OWLIconProviderImpl(OWLModelManager owlModelManager) {
        this.owlModelManager = owlModelManager;
    }


    public Icon getIcon() {
        return icon;
    }


    public Icon getIcon(OWLObject owlObject) {
        try {
            icon = null;
            owlObject.accept(this);
            return icon;
        }
        catch (Exception e) {
            return null;
        }
    }


    public void visit(OWLObjectIntersectionOf owlAnd) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDataType owlDataType) {
        icon = dataTypeIcon;
    }


    public void visit(OWLDataOneOf owlDataEnumeration) {
        icon = dataTypeIcon;
    }


    public void visit(OWLDataAllRestriction owlDataAllRestriction) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDataProperty owlDataProperty) {
        icon = dataPropertyIcon;
    }


    public void visit(OWLDataSomeRestriction owlDataSomeRestriction) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDataValueRestriction owlDataValueRestriction) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDifferentIndividualsAxiom owlDifferentIndividualsAxiom) {
        icon = individualIcon;
    }


    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLObjectSubPropertyAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLIndividual owlIndividual) {
        icon = individualIcon;
    }


    public void visit(OWLAnonymousIndividual individual) {
        icon = individualIcon;
    }


    public void visit(OWLObjectAllRestriction owlObjectAllRestriction) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectMinCardinalityRestriction desc) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectExactCardinalityRestriction desc) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectMaxCardinalityRestriction desc) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectSelfRestriction desc) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDataMinCardinalityRestriction desc) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDataExactCardinalityRestriction desc) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDataMaxCardinalityRestriction desc) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectProperty owlObjectProperty) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLObjectSomeRestriction owlObjectSomeRestriction) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectValueRestriction owlObjectValueRestriction) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectComplementOf owlNot) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLOntology owlOntology) {
        icon = ontologyIcon;
    }


    public void visit(OWLObjectUnionOf owlOr) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLImportsDeclaration axiom) {
        if (owlModelManager.getOWLOntologyManager().contains(axiom.getImportedOntologyURI())) {
            icon = ontologyIcon;
        }
        else {
            icon = ontologyMissing;
        }
    }


    public void visit(OWLSubClassAxiom owlSubClassAxiom) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDisjointClassesAxiom owlDisjointClassesAxiom) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLDisjointUnionAxiom owlDisjointUnionAxiom) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        icon = primitiveClassIcon;
    }


    public void visit(OWLSameIndividualsAxiom owlSameIndividualsAxiom) {
        icon = individualIcon;
    }

    
    public void visit(OWLClassAssertionAxiom owlClassAssertionAxiom) {
        icon = individualIcon;
    }


    public void visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
        icon = objectPropertyIcon;
    }


    public void visit(OWLClass owlClass) {
        for (OWLOntology ont : owlModelManager.getActiveOntologies()) {
            if (owlClass.isDefined(ont)) {
                icon = definedClassIcon;
                return;
            }
        }
        icon = primitiveClassIcon;
    }


    public void visit(OWLObjectOneOf owlEnumeration) {
        icon = primitiveClassIcon;
    }
}
