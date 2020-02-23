package org.protege.editor.owl.model.find;

import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.model.cache.OWLEntityRenderingCache;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 16-May-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLEntityFinderImpl implements OWLEntityFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(OWLEntityFinderImpl.class);
    private static final String WILDCARD = "*";
    private static final String ESCAPE_CHAR = "'";

    private final OWLEntityRenderingCache renderingCache;
    private final OWLModelManagerImpl manager;

    public OWLEntityFinderImpl(OWLModelManagerImpl manager, OWLEntityRenderingCache renderingCache) {
        this.manager = manager;
        this.renderingCache = renderingCache;
    }

    @Override
    public OWLClass getOWLClass(String rendering) {
        OWLClass cls = renderingCache.getOWLClass(rendering);
        if (cls == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            cls = renderingCache.getOWLClass(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return cls;
    }

    @Override
    public OWLObjectProperty getOWLObjectProperty(String rendering) {
        OWLObjectProperty prop = renderingCache.getOWLObjectProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            prop = renderingCache.getOWLObjectProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }

    @Override
    public OWLDataProperty getOWLDataProperty(String rendering) {
        OWLDataProperty prop = renderingCache.getOWLDataProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            prop = renderingCache.getOWLDataProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }

    @Override
    public OWLAnnotationProperty getOWLAnnotationProperty(String rendering) {
        OWLAnnotationProperty prop = renderingCache.getOWLAnnotationProperty(rendering);
        if (prop == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            prop = renderingCache.getOWLAnnotationProperty(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return prop;
    }

    @Override
    public OWLNamedIndividual getOWLIndividual(String rendering) {
        OWLNamedIndividual individual = renderingCache.getOWLIndividual(rendering);
        if (individual == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            individual = renderingCache.getOWLIndividual(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return individual;
    }

    @Override
    public OWLDatatype getOWLDatatype(String rendering) {
        OWLDatatype dataType = renderingCache.getOWLDatatype(rendering);
        if (dataType == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            dataType = renderingCache.getOWLDatatype(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return dataType;
    }

    @Override
    public OWLEntity getOWLEntity(String rendering) {
        OWLEntity entity = renderingCache.getOWLEntity(rendering);
        if (entity == null && !rendering.startsWith(ESCAPE_CHAR) && !rendering.endsWith(ESCAPE_CHAR)) {
            entity = renderingCache.getOWLEntity(ESCAPE_CHAR + rendering + ESCAPE_CHAR);
        }
        return entity;
    }

    @Override
    public Set<OWLClass> getMatchingOWLClasses(String match) {
        return getEntities(match, OWLClass.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    @Override
    public Set<OWLClass> getMatchingOWLClasses(String match, boolean fullRegExp) {
        return getEntities(match, OWLClass.class, fullRegExp);
    }

    @Override
    public Set<OWLObjectProperty> getMatchingOWLObjectProperties(String match) {
        return getEntities(match, OWLObjectProperty.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    @Override
    public Set<OWLObjectProperty> getMatchingOWLObjectProperties(String match, boolean fullRegExp) {
        return getEntities(match, OWLObjectProperty.class, fullRegExp);
    }

    @Override
    public Set<OWLDataProperty> getMatchingOWLDataProperties(String match) {
        return getEntities(match, OWLDataProperty.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    @Override
    public Set<OWLDataProperty> getMatchingOWLDataProperties(String match, boolean fullRegExp) {
        return getEntities(match, OWLDataProperty.class, fullRegExp);
    }

    @Override
    public Set<OWLNamedIndividual> getMatchingOWLIndividuals(String match) {
        return getEntities(match, OWLNamedIndividual.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    @Override
    public Set<OWLNamedIndividual> getMatchingOWLIndividuals(String match, boolean fullRegExp) {
        return getEntities(match, OWLNamedIndividual.class, fullRegExp);
    }

    @Override
    public Set<OWLDatatype> getMatchingOWLDatatypes(String match) {
        return getEntities(match, OWLDatatype.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    @Override
    public Set<OWLDatatype> getMatchingOWLDatatypes(String match, boolean fullRegExp) {
        return getEntities(match, OWLDatatype.class, fullRegExp);
    }

    @Override
    public Set<OWLAnnotationProperty> getMatchingOWLAnnotationProperties(String match) {
        return getEntities(match, OWLAnnotationProperty.class, OWLEntityFinderPreferences.getInstance().isUseRegularExpressions());
    }

    @Override
    public Set<OWLAnnotationProperty> getMatchingOWLAnnotationProperties(String match, boolean fullRegExp) {
        return getEntities(match, OWLAnnotationProperty.class, fullRegExp);
    }

    @Override
    public Set<OWLEntity> getEntities(IRI iri) {

        Set<OWLEntity> entities = new HashSet<>();

        for (OWLOntology ont : manager.getActiveOntologies()) {
            if (ont.containsClassInSignature(iri)) {
                entities.add(manager.getOWLDataFactory().getOWLClass(iri));
            }
            if (ont.containsObjectPropertyInSignature(iri)) {
                entities.add(manager.getOWLDataFactory().getOWLObjectProperty(iri));
            }
            if (ont.containsDataPropertyInSignature(iri)) {
                entities.add(manager.getOWLDataFactory().getOWLDataProperty(iri));
            }
            if (ont.containsIndividualInSignature(iri)) {
                entities.add(manager.getOWLDataFactory().getOWLNamedIndividual(iri));
            }
            if (ont.containsAnnotationPropertyInSignature(iri)) {
                entities.add(manager.getOWLDataFactory().getOWLAnnotationProperty(iri));
            }
            if (ont.containsDatatypeInSignature(iri)) {
                entities.add(manager.getOWLDataFactory().getOWLDatatype(iri));
            }
        }
        return entities;
    }

    private <T extends OWLEntity> Set<T> getEntities(String match, Class<T> type, boolean fullRegExp) {
        return getEntities(match, type, fullRegExp, Pattern.CASE_INSENSITIVE);
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends OWLEntity> Set<T> getEntities(String match, Class<T> type, boolean fullRegExp, int flags) {
        if (match.length() == 0) {
            return Collections.emptySet();
        }
        return fullRegExp ? doRegExpSearch(match, type, flags) : doWildcardSearch(match, type);
    }

    private <T extends OWLEntity> Set<T> doRegExpSearch(String match, Class<T> type, int flags) {
        Set<T> results = new HashSet<>();
        try {
            Pattern pattern = Pattern.compile(match, flags);
            for (String rendering : getRenderings(type)) {
                Matcher m = pattern.matcher(rendering);
                if (!m.find()) {
                    continue;
                }
                T ent = getEntity(rendering, type);
                if (ent != null) {
                    results.add(ent);
                }
            }
        } catch (PatternSyntaxException e) {
            LOGGER.warn("Invalid regular expression: '{}'", e.getMessage(), e);
        }
        return results;
    }

    /* @@TODO fix wildcard searching - it does not handle the usecases correctly
     * eg A*B will not work, and endsWith is implemented the same as contains
     * (probably right but this should not be implemented separately)
     */
    private <T extends OWLEntity> Set<T> doWildcardSearch(String match, Class<T> type) {
        if (match.equals(WILDCARD)) {
            return entities(type).collect(Collectors.toSet());
        }
        Set<T> res = new HashSet<>();
        SimpleWildCardMatcher matcher;
        if (match.startsWith(WILDCARD)) {
            if (match.length() > 1 && match.endsWith(WILDCARD)) {
                // Contains
                matcher = String::contains;
                match = match.substring(1, match.length() - 1);
            } else {
                // Ends with
                matcher = String::contains;
                match = match.substring(1);
            }
        } else {
            // Starts with
            if (match.endsWith(WILDCARD) && match.length() > 1) {
                match = match.substring(0, match.length() - 1);
            }
            // @@TODO handle matches exactly?
            matcher = (rendering, s) -> rendering.startsWith(s) || rendering.startsWith("'" + s);
        }

        if (match.trim().isEmpty()) {
            LOGGER.debug("Attempt to match the empty string (no results)");
            return res;
        }
        match = match.toLowerCase();
        LOGGER.debug("Match: {}", match);
        for (String rendering : getRenderings(type)) {
            if (rendering.length() > 0) {
                if (matcher.matches(rendering.toLowerCase(), match)) {
                    res.add(getEntity(rendering, type));
                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public <T extends OWLEntity> Stream<T> entities(Class<T> type) {
        Set<OWLOntology> ontologies = manager.getActiveOntologies();
        if (type.equals(OWLDatatype.class)) {
            return (Stream<T>) Stream.concat(new OWLDataTypeUtils(manager.getOWLOntologyManager()).builtinDatatypes(),
                    ontologies.stream().flatMap(HasDatatypesInSignature::datatypesInSignature));
        }
        if (type.equals(OWLClass.class)) {
            return (Stream<T>) ontologies.stream().flatMap(HasClassesInSignature::classesInSignature);
        }
        if (type.equals(OWLObjectProperty.class)) {
            return (Stream<T>) ontologies.stream().flatMap(HasObjectPropertiesInSignature::objectPropertiesInSignature);
        }
        if (type.equals((OWLDataProperty.class))) {
            return (Stream<T>) ontologies.stream().flatMap(HasDataPropertiesInSignature::dataPropertiesInSignature);
        }
        if (type.equals(OWLIndividual.class)) {
            return (Stream<T>) ontologies.stream().flatMap(HasIndividualsInSignature::individualsInSignature);
        }
        if (type.equals(OWLAnnotationProperty.class)) {
            return (Stream<T>) ontologies.stream().flatMap(HasAnnotationPropertiesInSignature::annotationPropertiesInSignature);
        }
        throw new IllegalArgumentException("Wrong type: " + type);
    }

    private <T extends OWLEntity> T getEntity(String rendering, Class<T> type) {
        if (OWLClass.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLClass(rendering));
        } else if (OWLObjectProperty.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLObjectProperty(rendering));
        } else if (OWLDataProperty.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLDataProperty(rendering));
        } else if (OWLNamedIndividual.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLIndividual(rendering));
        } else if (OWLAnnotationProperty.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLAnnotationProperty(rendering));
        } else if (OWLDatatype.class.isAssignableFrom(type)) {
            return type.cast(renderingCache.getOWLDatatype(rendering));
        } else {
            return type.cast(renderingCache.getOWLEntity(rendering));
        }
    }

    private <T extends OWLEntity> Set<String> getRenderings(Class<T> type) {
        if (OWLClass.class.isAssignableFrom(type)) {
            return renderingCache.getOWLClassRenderings();
        } else if (OWLObjectProperty.class.isAssignableFrom(type)) {
            return renderingCache.getOWLObjectPropertyRenderings();
        } else if (OWLDataProperty.class.isAssignableFrom(type)) {
            return renderingCache.getOWLDataPropertyRenderings();
        } else if (OWLNamedIndividual.class.isAssignableFrom(type)) {
            return renderingCache.getOWLIndividualRenderings();
        } else if (OWLAnnotationProperty.class.isAssignableFrom(type)) {
            return renderingCache.getOWLAnnotationPropertyRenderings();
        } else if (OWLDatatype.class.isAssignableFrom(type)) {
            return renderingCache.getOWLDatatypeRenderings();
        } else {
            return renderingCache.getOWLEntityRenderings();
        }
    }

    private interface SimpleWildCardMatcher {

        boolean matches(String rendering, String s);
    }
}
