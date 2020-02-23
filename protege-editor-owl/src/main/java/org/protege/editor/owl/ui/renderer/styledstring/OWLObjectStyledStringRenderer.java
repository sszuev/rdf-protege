package org.protege.editor.owl.ui.renderer.styledstring;

import org.protege.editor.owl.ui.renderer.context.OWLObjectRenderingContext;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 25/09/2012
 */
public class OWLObjectStyledStringRenderer {

    private final OWLObjectRenderingContext renderingContext;

    public OWLObjectStyledStringRenderer(OWLObjectRenderingContext renderingContext) {
        this.renderingContext = renderingContext;
    }

    public StyledString getRendering(OWLObject owlObject) {
        ObjectRenderer renderer = new ObjectRenderer();
        owlObject.accept(renderer);
        return renderer.builder.build();
    }

    private static boolean isAtomic(OWLObject object) {
        return object instanceof OWLEntity
                || object instanceof OWLLiteral
                || object instanceof OWLObjectOneOf
                || object instanceof OWLDataOneOf;
    }

    @SuppressWarnings("NullableProblems")
    private class ObjectRenderer implements OWLObjectVisitor {
        public static final String COMMA_SEPARATOR = ", ";
        private final StyledString.Builder builder = new StyledString.Builder();

        private void renderKeywordWithSpaces(ManchesterOWLSyntax keyword) {
            builder.appendSpace();
            renderKeyword(keyword);
            builder.appendSpace();
        }

        private void renderKeyword(ManchesterOWLSyntax keyword) {
            Style style = ProtegeStyles.getStyles().getKeywordStyle(keyword);
            builder.appendWithStyle(keyword.toString(), style);
        }

        private void renderSpace() {
            builder.appendSpace();
        }

        private void renderColonSpace() {
            builder.append(": ");
        }

        private void renderKeywordColonSpace(ManchesterOWLSyntax keyword) {
            renderKeyword(keyword);
            renderColonSpace();
        }

        @SuppressWarnings("SameParameterValue")
        private void renderCollection(OWLObject parent,
                                      Iterable<? extends OWLObject> collection,
                                      String separator,
                                      BracketingStrategy bracketingStrategy) {
            renderCollection(parent, collection, bracketingStrategy, separator, builder::append);
        }

        private void renderCollection(OWLObject parent,
                                      Stream<? extends OWLObject> collection,
                                      ManchesterOWLSyntax separator,
                                      BracketingStrategy bracketingStrategy) {
            renderCollection(parent, collection.iterator(), bracketingStrategy, separator, this::renderKeywordWithSpaces);
        }

        private void renderCollection(OWLObject parent,
                                      Stream<? extends OWLObject> collection,
                                      BracketingStrategy bracketingStrategy) {
            renderCollection(parent, collection.iterator(), bracketingStrategy, ObjectRenderer.COMMA_SEPARATOR, builder::append);
        }

        private <X> void renderCollection(OWLObject parent,
                                          Iterable<? extends OWLObject> collection,
                                          BracketingStrategy bracketingStrategy,
                                          X separator,
                                          Consumer<X> whenHasNext) {
            renderCollection(parent, collection.iterator(), bracketingStrategy, separator, whenHasNext);
        }

        private <X> void renderCollection(OWLObject parent,
                                          Iterator<? extends OWLObject> it,
                                          BracketingStrategy bracketingStrategy,
                                          X separator,
                                          Consumer<X> whenHasNext) {
            while (it.hasNext()) {
                OWLObject o = it.next();
                boolean bracket = bracketingStrategy.shouldBracket(parent, o);
                if (bracket) {
                    builder.append("(");
                }
                o.accept(this);
                if (bracket) {
                    builder.append(")");
                }
                if (it.hasNext()) {
                    whenHasNext.accept(separator);
                }
            }
        }

        @Override
        public void visit(OWLAnnotation annotation) {
            int propStart = builder.mark();
            annotation.getProperty().accept(this);
            int propEnd = builder.mark();
            builder.applyStyle(propStart, propEnd, ProtegeStyles.getStyles().getAnnotationPropertyStyle());
            renderSpace();
            OWLAnnotationValue value = annotation.getValue();
            if (!(value instanceof OWLLiteral)) {
                value.accept(this);
                return;
            }
            OWLLiteral literal = (OWLLiteral) value;
            Style langStyle = ProtegeStyles.getStyles().getAnnotationLangStyle();
            if (literal.isRDFPlainLiteral()) {
                if (!literal.getLang().isEmpty()) {
                    builder.appendWithStyle("[language: ", langStyle);
                    builder.appendWithStyle(literal.getLang(), langStyle);
                    builder.appendWithStyle("] ", langStyle);
                }
            } else {
                builder.appendWithStyle("[type: ", langStyle);
                int dtStart = builder.mark();
                literal.getDatatype().accept(this);
                int dtEnd = builder.mark();
                builder.applyStyle(dtStart, dtEnd, langStyle);
                builder.appendWithStyle("] ", langStyle);
            }
            builder.append(literal.getLiteral());

        }

        @Override
        public void visit(OWLSubClassOfAxiom axiom) {
            axiom.getSubClass().accept(this);
            renderSpace();
            renderKeywordWithSpaces(ManchesterOWLSyntax.SUBCLASS_OF);
            renderSpace();
            axiom.getSuperClass().accept(this);
        }

        @Override
        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        }

        @Override
        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.ASYMMETRIC);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.REFLEXIVE);
            axiom.getProperty().accept(this);
        }

        private void renderBinaryOrNaryList(List<? extends OWLObject> collection,
                                            ManchesterOWLSyntax binaryKeyword,
                                            ManchesterOWLSyntax naryKeyword) {
            renderBinaryOrNaryList(collection, binaryKeyword, naryKeyword, COMMA_SEPARATOR);
        }

        @SuppressWarnings("SameParameterValue")
        private void renderBinaryOrNaryList(List<? extends OWLObject> list,
                                            ManchesterOWLSyntax binaryKeyword,
                                            ManchesterOWLSyntax naryKeyword,
                                            String separator) {
            if (list.size() == 2) {
                list.get(0).accept(this);
                renderKeywordWithSpaces(binaryKeyword);
                list.get(1).accept(this);
                return;
            }
            renderKeywordColonSpace(naryKeyword);
            Iterator<? extends OWLObject> it = list.iterator();
            while (it.hasNext()) {
                OWLObject o = it.next();
                o.accept(this);
                if (it.hasNext()) {
                    builder.append(separator);
                }
            }
        }

        @Override
        public void visit(OWLDisjointClassesAxiom axiom) {
            renderBinaryOrNaryList(axiom.classExpressions().collect(Collectors.toList()),
                    ManchesterOWLSyntax.DISJOINT_WITH, ManchesterOWLSyntax.DISJOINT_CLASSES);
        }

        private void renderDomainAxiom(OWLPropertyDomainAxiom<?> axiom) {
            axiom.getProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.DOMAIN);
            axiom.getDomain().accept(this);
        }

        @Override
        public void visit(OWLDataPropertyDomainAxiom axiom) {
            renderDomainAxiom(axiom);
        }

        @Override
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            renderDomainAxiom(axiom);
        }

        @Override
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            renderBinaryOrNaryList(axiom.properties().collect(Collectors.toList()),
                    ManchesterOWLSyntax.EQUIVALENT_TO, ManchesterOWLSyntax.EQUIVALENT_PROPERTIES);
        }

        @Override
        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        }

        @Override
        public void visit(OWLDifferentIndividualsAxiom axiom) {
            renderBinaryOrNaryList(axiom.individuals().collect(Collectors.toList()),
                    ManchesterOWLSyntax.SAME_AS, ManchesterOWLSyntax.SAME_INDIVIDUAL);
        }

        @Override
        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            renderBinaryOrNaryList(axiom.properties().collect(Collectors.toList()),
                    ManchesterOWLSyntax.DISJOINT_WITH, ManchesterOWLSyntax.DISJOINT_PROPERTIES);
        }

        @Override
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            renderBinaryOrNaryList(axiom.properties().collect(Collectors.toList()),
                    ManchesterOWLSyntax.DISJOINT_WITH, ManchesterOWLSyntax.DISJOINT_PROPERTIES);
        }

        private void renderRangeAxiom(OWLPropertyRangeAxiom<?, ?> axiom) {
            axiom.getProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.RANGE);
            axiom.getRange().accept(this);
        }

        @Override
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            renderRangeAxiom(axiom);
        }

        @Override
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            renderPropertyAssertion(axiom);
        }

        @Override
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.FUNCTIONAL);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.SUB_PROPERTY_OF);
            axiom.getSuperProperty().accept(this);
        }

        @Override
        public void visit(OWLDisjointUnionAxiom axiom) {
            axiom.getOWLClass().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.DISJOINT_UNION_OF);
            renderCollection(axiom, axiom.classExpressions(),
                    ComplexClassExpressionBracketingStrategy.get());
        }

        @Override
        public void visit(OWLDeclarationAxiom axiom) {
            axiom.getEntity().accept(new OWLEntityVisitor() {
                @Override
                public void visit(OWLClass owlClass) {
                    renderKeywordColonSpace(ManchesterOWLSyntax.CLASS);
                }
                @Override
                public void visit(OWLObjectProperty property) {
                    renderKeywordColonSpace(ManchesterOWLSyntax.OBJECT_PROPERTY);
                }
                @Override
                public void visit(OWLDataProperty dataProperty) {
                    renderKeywordColonSpace(ManchesterOWLSyntax.DATA_PROPERTY);
                }
                @Override
                public void visit(OWLNamedIndividual individual) {
                    renderKeywordColonSpace(ManchesterOWLSyntax.INDIVIDUAL);
                }
                @Override
                public void visit(OWLDatatype owlDatatype) {
                    renderKeywordColonSpace(ManchesterOWLSyntax.DATATYPE);
                }
                @Override
                public void visit(OWLAnnotationProperty property) {
                    renderKeywordColonSpace(ManchesterOWLSyntax.ANNOTATION_PROPERTY);
                }
            });
        }

        private void renderPropertyAssertion(OWLPropertyAssertionAxiom<?, ?> axiom) {
            axiom.getSubject().accept(this);
            renderSpace();
            axiom.getProperty().accept(this);
            renderSpace();
            axiom.getObject().accept(this);
        }

        public void visit(OWLAnnotationAssertionAxiom axiom) {
            if (axiom.getSubject() instanceof IRI) {
                builder.append(renderingContext.getIriShortFormProvider().getShortForm((IRI) axiom.getSubject()));
            } else {
                axiom.getSubject().accept(this);
            }
            renderSpace();
            axiom.getAnnotation().accept(this);
        }

        @Override
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.SYMMETRIC);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLDataPropertyRangeAxiom axiom) {
            renderRangeAxiom(axiom);
        }

        @Override
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.FUNCTIONAL);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            renderBinaryOrNaryList(axiom.properties().collect(Collectors.toList()),
                    ManchesterOWLSyntax.EQUIVALENT_TO, ManchesterOWLSyntax.EQUIVALENT_PROPERTIES);
        }

        @Override
        public void visit(OWLClassAssertionAxiom axiom) {
            axiom.getIndividual().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.TYPE);
            axiom.getClassExpression().accept(this);
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            renderBinaryOrNaryList(axiom.classExpressions().collect(Collectors.toList()),
                    ManchesterOWLSyntax.EQUIVALENT_TO, ManchesterOWLSyntax.EQUIVALENT_CLASSES);
        }

        @Override
        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            renderPropertyAssertion(axiom);
        }

        @Override
        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.TRANSITIVE);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.IRREFLEXIVE);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.SUB_PROPERTY_OF);
            axiom.getSuperProperty().accept(this);
        }

        @Override
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            renderKeywordColonSpace(ManchesterOWLSyntax.INVERSE_FUNCTIONAL);
            axiom.getProperty().accept(this);
        }

        @Override
        public void visit(OWLSameIndividualAxiom axiom) {
            renderBinaryOrNaryList(axiom.individuals().collect(Collectors.toList()),
                    ManchesterOWLSyntax.SAME_AS, ManchesterOWLSyntax.SAME_INDIVIDUAL);
        }

        @Override
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
            renderCollection(axiom, axiom.getPropertyChain(), " o ", NullBracketingStrategy.get());
            renderKeywordWithSpaces(ManchesterOWLSyntax.SUB_PROPERTY_OF);
            axiom.getSuperProperty().accept(this);
        }

        @Override
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            axiom.getFirstProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.INVERSE_OF);
            axiom.getSecondProperty().accept(this);
        }

        @Override
        public void visit(OWLHasKeyAxiom axiom) {
        }

        @Override
        public void visit(OWLDatatypeDefinitionAxiom axiom) {
        }

        @Override
        public void visit(SWRLRule swrlRule) {
            renderCollection(swrlRule, swrlRule.body(), NullBracketingStrategy.get());
            builder.append(" -> ");
            renderCollection(swrlRule, swrlRule.head(), NullBracketingStrategy.get());
        }

        @Override
        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
            axiom.getSubProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.SUB_PROPERTY_OF);
            axiom.getSuperProperty().accept(this);
        }

        @Override
        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
            axiom.getProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.DOMAIN);
            axiom.getDomain().accept(this);
        }

        @Override
        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
            axiom.getProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.RANGE);
            axiom.getRange().accept(this);
        }

        @Override
        public void visit(IRI iri) {
            builder.append(iri.toQuotedString());
        }

        @Override
        public void visit(OWLAnonymousIndividual individual) {
            builder.append(individual.getID().getID());
        }

        private void renderEntity(OWLEntity entity) {
            ShortFormProvider sfp = renderingContext.getShortFormProvider();
            String rendering = sfp.getShortForm(entity);
            int renderingStart = builder.mark();
            builder.append(rendering);
            int renderingEnd = builder.mark();
            Style style = new Style(ForegroundAttribute.get(Color.BLACK));
            if (renderingContext.getDeprecatedObjectChecker().isDeprecated(entity)) {
                style = style.append(ProtegeStyles.getStyles().getDeprecatedEntityStyle());
            }
            if (entity.isOWLClass()) {
                if (!renderingContext.getClassSatisfiabilityChecker().isSatisfiable(entity.asOWLClass())) {
                    style = style.append(ProtegeStyles.getStyles().getUnsatisfiableClassStyle());
                }
            }
            builder.applyStyle(renderingStart, renderingEnd, style);
        }

        @Override
        public void visit(OWLClass owlClass) {
            renderEntity(owlClass);
        }

        @Override
        public void visit(OWLObjectIntersectionOf clazz) {
            renderCollection(clazz, clazz.operands(), ManchesterOWLSyntax.AND, ComplexClassExpressionBracketingStrategy.get());
        }

        @Override
        public void visit(OWLObjectUnionOf clazz) {
            renderCollection(clazz, clazz.operands(), ManchesterOWLSyntax.OR, ComplexClassExpressionBracketingStrategy.get());
        }

        @Override
        public void visit(OWLObjectComplementOf clazz) {
            renderKeyword(ManchesterOWLSyntax.NOT);
            renderSpace();
            clazz.getOperand().accept(this);
        }

        private void renderRestrictionFiller(OWLObject filler) {
            if (isAtomic(filler)) {
                filler.accept(this);
            } else {
                builder.append("(");
                filler.accept(this);
                builder.append(")");
            }
        }

        private void renderQuantifiedRestriction(OWLQuantifiedRestriction<?> restriction, ManchesterOWLSyntax keyword) {
            restriction.getProperty().accept(this);
            renderKeywordWithSpaces(keyword);
            renderRestrictionFiller(restriction.getFiller());
        }

        @Override
        public void visit(OWLObjectSomeValuesFrom clazz) {
            renderQuantifiedRestriction(clazz, ManchesterOWLSyntax.SOME);
        }

        @Override
        public void visit(OWLObjectAllValuesFrom clazz) {
            renderQuantifiedRestriction(clazz, ManchesterOWLSyntax.ONLY);
        }

        private void renderHasValueRestriction(OWLHasValueRestriction<?> restriction) {
            restriction.getProperty().accept(this);
            renderKeywordWithSpaces(ManchesterOWLSyntax.VALUE);
            restriction.getFiller().accept(this);
        }

        @Override
        public void visit(OWLObjectHasValue owlObjectHasValue) {
            renderHasValueRestriction(owlObjectHasValue);
        }

        private void renderCardinalityRestriction(OWLCardinalityRestriction<?> restriction, ManchesterOWLSyntax keyword) {
            restriction.getProperty().accept(this);
            renderKeywordWithSpaces(keyword);
            builder.append(restriction.getCardinality());
            renderSpace();
            renderRestrictionFiller(restriction.getFiller());
        }

        @Override
        public void visit(OWLObjectMinCardinality owlObjectMinCardinality) {
            renderCardinalityRestriction(owlObjectMinCardinality, ManchesterOWLSyntax.MIN);
        }

        @Override
        public void visit(OWLObjectExactCardinality owlObjectExactCardinality) {
            renderCardinalityRestriction(owlObjectExactCardinality, ManchesterOWLSyntax.EXACTLY);
        }

        @Override
        public void visit(OWLObjectMaxCardinality owlObjectMaxCardinality) {
            renderCardinalityRestriction(owlObjectMaxCardinality, ManchesterOWLSyntax.MAX);
        }

        @Override
        public void visit(OWLObjectHasSelf owlObjectHasSelf) {
            owlObjectHasSelf.getProperty().accept(this);
            renderSpace();
            renderKeyword(ManchesterOWLSyntax.SELF);
        }

        @Override
        public void visit(OWLObjectOneOf clazz) {
            builder.append("{");
            renderCollection(clazz, clazz.individuals(), NullBracketingStrategy.get());
            builder.append("}");
        }

        @Override
        public void visit(OWLDataSomeValuesFrom clazz) {
            renderQuantifiedRestriction(clazz, ManchesterOWLSyntax.SOME);
        }

        @Override
        public void visit(OWLDataAllValuesFrom owlDataAllValuesFrom) {
            renderQuantifiedRestriction(owlDataAllValuesFrom, ManchesterOWLSyntax.ONLY);
        }

        @Override
        public void visit(OWLDataHasValue owlDataHasValue) {
            renderHasValueRestriction(owlDataHasValue);
        }

        @Override
        public void visit(OWLDataMinCardinality owlDataMinCardinality) {
            renderCardinalityRestriction(owlDataMinCardinality, ManchesterOWLSyntax.MIN);
        }

        @Override
        public void visit(OWLDataExactCardinality owlDataExactCardinality) {
            renderCardinalityRestriction(owlDataExactCardinality, ManchesterOWLSyntax.EXACTLY);
        }

        @Override
        public void visit(OWLDataMaxCardinality owlDataMaxCardinality) {
            renderCardinalityRestriction(owlDataMaxCardinality, ManchesterOWLSyntax.MAX);
        }

        @Override
        public void visit(OWLDatatype owlDatatype) {
            renderEntity(owlDatatype);
        }

        @Override
        public void visit(OWLDataComplementOf owlDataComplementOf) {
            renderKeyword(ManchesterOWLSyntax.NOT);
            renderSpace();
            owlDataComplementOf.getDataRange().accept(this);
        }

        @Override
        public void visit(OWLDataOneOf dr) {
            builder.append("{");
            renderCollection(dr, dr.values(), NullBracketingStrategy.get());
            builder.append("}");
        }

        @Override
        public void visit(OWLDataIntersectionOf ce) {
            renderCollection(ce, ce.operands(), ManchesterOWLSyntax.AND, NullBracketingStrategy.get());
        }

        @Override
        public void visit(OWLDataUnionOf dr) {
            renderCollection(dr, dr.operands(), ManchesterOWLSyntax.OR, NullBracketingStrategy.get());
        }

        @Override
        public void visit(OWLDatatypeRestriction r) {
            r.getDatatype().accept(this);
            renderSpace();
            builder.append("[");
            renderCollection(r, r.facetRestrictions(), ManchesterOWLSyntax.COMMA, NullBracketingStrategy.get());
            builder.append("]");
        }

        @Override
        public void visit(OWLLiteral owlLiteral) {
            if (owlLiteral.isBoolean()) {
                builder.append(Boolean.toString(owlLiteral.parseBoolean()));
            } else if (owlLiteral.isDouble()) {
                builder.append(owlLiteral.parseDouble());
            } else if (owlLiteral.isFloat()) {
                builder.append(owlLiteral.parseFloat());
            } else if (owlLiteral.isInteger()) {
                builder.append(owlLiteral.parseInteger());
            } else {
                builder.append("\"");
                builder.append(owlLiteral.getLiteral());
                builder.append("\"^^");
                owlLiteral.getDatatype().accept(this);
            }
        }

        @Override
        public void visit(OWLFacetRestriction owlFacetRestriction) {
            builder.append(owlFacetRestriction.getFacet().getPrefixedName());
            renderSpace();
            owlFacetRestriction.getFacetValue().accept(this);
        }

        @Override
        public void visit(OWLNamedIndividual individual) {
            renderEntity(individual);
        }

        @Override
        public void visit(OWLAnnotationProperty property) {
            renderEntity(property);
        }

        @Override
        public void visit(OWLOntology owlOntology) {
            OntologyIRIShortFormProvider ontSfp = renderingContext.getOntologyIRIShortFormProvider();
            String ontShortForm = ontSfp.getShortForm(owlOntology);
            builder.append(ontShortForm);
            builder.appendSpace();
            OWLOntologyID id = owlOntology.getOntologyID();
            if (id.isAnonymous()) {
                return;
            }
            builder.appendWithStyle(id.getOntologyIRI().orElseThrow(IllegalStateException::new)
                    .toQuotedString(), Style.getForeground(Color.DARK_GRAY));
            id.getVersionIRI().ifPresent(iri -> {
                builder.appendSpace();
                builder.appendWithStyle(iri.toQuotedString(), Style.getForeground(Color.GRAY));
            });
        }

        @Override
        public void visit(OWLObjectProperty property) {
            renderEntity(property);
        }

        @Override
        public void visit(OWLObjectInverseOf owlObjectInverseOf) {
            renderKeyword(ManchesterOWLSyntax.INVERSE_OF);
            renderSpace();
            owlObjectInverseOf.getInverse().accept(this);
        }

        @Override
        public void visit(OWLDataProperty dataProperty) {
            renderEntity(dataProperty);
        }

        private void renderSWRLAtom(SWRLAtom atom, OWLObject predicate) {
            predicate.accept(this);
            builder.append("(");
            renderCollection(atom, atom.allArguments(), NullBracketingStrategy.get());
            builder.append(")");
        }

        @Override
        public void visit(SWRLClassAtom swrlClassAtom) {
            renderSWRLAtom(swrlClassAtom, swrlClassAtom.getPredicate());
        }

        @Override
        public void visit(SWRLDataRangeAtom swrlDataRangeAtom) {
            renderSWRLAtom(swrlDataRangeAtom, swrlDataRangeAtom.getPredicate());
        }

        @Override
        public void visit(SWRLObjectPropertyAtom swrlObjectPropertyAtom) {
            renderSWRLAtom(swrlObjectPropertyAtom, swrlObjectPropertyAtom.getPredicate());
        }

        @Override
        public void visit(SWRLDataPropertyAtom swrlDataPropertyAtom) {
            renderSWRLAtom(swrlDataPropertyAtom, swrlDataPropertyAtom.getPredicate());
        }

        @Override
        public void visit(SWRLBuiltInAtom atom) {
            atom.getPredicate().accept(this);
            builder.append("(");
            renderCollection(atom, atom.allArguments(), NullBracketingStrategy.get());
            builder.append(")");
        }

        @Override
        public void visit(SWRLVariable swrlVariable) {
            builder.append("?");
            builder.append(swrlVariable.getIRI().getRemainder().orElse(swrlVariable.getIRI().toString()));
        }

        @Override
        public void visit(SWRLIndividualArgument swrlIndividualArgument) {
            swrlIndividualArgument.getIndividual().accept(this);
        }

        @Override
        public void visit(SWRLLiteralArgument swrlLiteralArgument) {
            swrlLiteralArgument.getLiteral().accept(this);
        }

        @Override
        public void visit(SWRLSameIndividualAtom atom) {
            builder.append(ManchesterOWLSyntax.SAME_INDIVIDUAL.toString());
            builder.append("(");
            renderCollection(atom, atom.allArguments(), NullBracketingStrategy.get());
            builder.append(")");
        }

        @Override
        public void visit(SWRLDifferentIndividualsAtom atom) {
            builder.append(ManchesterOWLSyntax.DIFFERENT_INDIVIDUALS.toString());
            builder.append("(");
            renderCollection(atom, atom.allArguments(), NullBracketingStrategy.get());
            builder.append(")");
        }
    }

}
