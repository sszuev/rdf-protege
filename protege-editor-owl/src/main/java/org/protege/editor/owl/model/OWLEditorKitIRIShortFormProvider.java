package org.protege.editor.owl.model;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 7 Nov 2016
 */
public class OWLEditorKitIRIShortFormProvider implements IRIShortFormProvider {

    private final OWLEditorKit editorKit;

    private final SimpleIRIShortFormProvider delegateIRIShortFormProvider;

    public OWLEditorKitIRIShortFormProvider(@Nonnull OWLEditorKit editorKit,
                                            @Nonnull SimpleIRIShortFormProvider delegateIRIShortFormProvider) {
        this.editorKit = Objects.requireNonNull(editorKit);
        this.delegateIRIShortFormProvider = Objects.requireNonNull(delegateIRIShortFormProvider);
    }

    @Nonnull
    @Override
    public String getShortForm(@Nonnull IRI iri) {
        OWLModelManager manager = editorKit.getOWLModelManager();
        Set<OWLEntity> entities = manager.getActiveOntology().entitiesInSignature(iri).limit(2).collect(Collectors.toSet());
        return entities.isEmpty() ? delegateIRIShortFormProvider.getShortForm(iri)
                : manager.getRendering(entities.iterator().next());
    }
}
