package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.view.OWLSelectionViewAction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.OWLEntitySetProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 20-Apr-2007<br><br>
 */
public abstract class AbstractDeleteEntityAction<E extends OWLEntity> extends OWLSelectionViewAction {

    private final OWLObjectHierarchyDeleter<E> deleter;
    protected final OWLEntitySetProvider<E> entitySetProvider;

    protected AbstractDeleteEntityAction(String name,
                                         Icon icon,
                                         OWLEditorKit kit,
                                         OWLHierarchyProvider<E> hierarchyProvider,
                                         OWLEntitySetProvider<E> entitiesProvider) {
        super(name, icon);
        this.entitySetProvider = Objects.requireNonNull(entitiesProvider);
        this.deleter = new OWLObjectHierarchyDeleter<>(kit, hierarchyProvider, entitiesProvider, getPluralDescription());
    }

    @Override
    public void updateState() {
        setEnabled(entitySetProvider.entities().findFirst().isPresent());
    }

    @Override
    public void dispose() {
        deleter.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        deleter.performDeletion();
    }

    /**
     * Returns the plural name of a set of entities e.g. classes, properties, individuals.
     * This is used in the UI e.g. "Delete selected classes"
     *
     * @return {@code String}
     */
    protected abstract String getPluralDescription();
}
