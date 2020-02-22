package org.protege.editor.owl.model.search.importer;

import org.protege.editor.owl.model.search.*;
import org.protege.editor.owl.ui.renderer.styledstring.StyledString;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 03/10/2012
 */
public class OntologyAnnotationSearchMetadataImporter extends OntologyBasedSearchMDImporter {

    @Override
    public boolean isImporterFor(Set<SearchCategory> categories) {
        return categories.contains(SearchCategory.ANNOTATION_VALUE);
    }

    @Override
    public void generateSearchMetadata(OWLOntology ontology, SearchMetadataImportContext context, SearchMetadataDB db) {
        ontology.annotations().forEach(a -> generateSearchMetadataForAnnotation(a, ontology, context, db));
    }

    private void generateSearchMetadataForAnnotation(OWLAnnotation annotation,
                                                     OWLOntology ontology,
                                                     SearchMetadataImportContext context,
                                                     SearchMetadataDB db) {
        String gd = context.getRendering(annotation.getProperty());
        StyledString rendering = context.getStyledStringRendering(annotation);
        SearchMetadata md = new SearchMetadata(SearchCategory.ANNOTATION_VALUE, gd, ontology,
                context.getRendering(ontology), rendering.getString()) {
            @Override
            public StyledString getStyledSearchSearchString() {
                return context.getStyledStringRendering(annotation);
            }
        };
        db.addResult(md);
        annotation.annotations().forEach(a -> generateSearchMetadataForAnnotation(a, ontology, context, db));
    }
}
