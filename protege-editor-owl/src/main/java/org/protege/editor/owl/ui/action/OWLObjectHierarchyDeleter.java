package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.HierarchyProvider;
import org.protege.editor.owl.model.util.OWLEntityDeleter;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.OWLEntitySetProvider;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 02-May-2007<br><br>
 *
 * @param <E> - subtype of {@link OWLEntity}
 */
public class OWLObjectHierarchyDeleter<E extends OWLEntity> extends ObjectHierarchyDeleter<E> {

    public OWLObjectHierarchyDeleter(OWLEditorKit editorKit,
                                     HierarchyProvider<E> hierarchyProvider,
                                     OWLEntitySetProvider<E> entitiesProvider,
                                     String pluralName) {
        super(editorKit, hierarchyProvider, () -> entitiesProvider.entities().collect(Collectors.toSet()), pluralName);
    }

    @Override
    protected void delete(Collection<E> entities) {
        OWLEntityDeleter.deleteEntities(entities, getOWLEditorKit().getOWLModelManager());
    }
}
