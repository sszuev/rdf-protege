package org.protege.editor.owl.ui.view;

import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.ui.action.AbstractDeleteEntityAction;
import org.protege.editor.owl.ui.action.AbstractOWLTreeAction;
import org.protege.editor.owl.ui.tree.OWLTreePreferences;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * User: nickdrummond
 * Date: May 23, 2008
 */
public abstract class AbstractOWLPropertyHierarchyViewComponent<O extends OWLProperty>
        extends AbstractOWLEntityHierarchyViewComponent<O>
        implements Findable<O>, Deleteable, CreateNewChildTarget, CreateNewSiblingTarget {

    private static final String ADD_GROUP = "A";
    private static final String DELETE_GROUP = "B";
    private static final String FIRST_SLOT = "A";
    private static final String SECOND_SLOT = "B";

    protected abstract OWLSubPropertyAxiom<? extends OWLPropertyExpression> getSubPropertyAxiom(O child, O parent);

    protected abstract boolean canAcceptDrop(Object child, Object parent);

    protected abstract OWLEntityCreationSet<O> createProperty();

    protected abstract Icon getSubIcon();

    protected abstract Icon getSibIcon();

    protected abstract Icon getDeleteIcon();

    @Override
    protected void performExtraInitialisation() throws Exception {
        addAction(new AbstractOWLTreeAction<O>("Add sub property",
                getSubIcon(),
                getTree().getSelectionModel()) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewChild();
            }

            @Override
            protected boolean canPerform(O prop) {
                return canCreateNewChild();
            }
        }, ADD_GROUP, FIRST_SLOT);

        addAction(new AbstractOWLTreeAction<O>("Add sibling property",
                getSibIcon(),
                getTree().getSelectionModel()) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewSibling();
            }

            @Override
            protected boolean canPerform(O cls) {
                return canCreateNewSibling();
            }
        }, ADD_GROUP, SECOND_SLOT);

        addAction(new AbstractDeleteEntityAction<O>("Delete selected properties",
                getDeleteIcon(),
                getOWLEditorKit(),
                getHierarchyProvider(),
                () -> getSelectedEntities().stream()) {

            @Override
            protected String getPluralDescription() {
                return "properties";
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!getTopProperty().equals(getSelectedEntity())) {
                    super.actionPerformed(e);
                }
            }

            @Override
            public void updateState() {
                super.updateState();
                if (isEnabled()) {
                    setEnabled(isInAssertedMode());
                }
            }
        }, DELETE_GROUP, FIRST_SLOT);

        getTree().setDragAndDropHandler(new OWLPropertyTreeDropHandler<O>(getOWLModelManager()){
            @Override
            protected OWLAxiom getAxiom(OWLDataFactory df, O child, O parent) {
                return getSubPropertyAxiom(child, parent);
            }

            @Override
            public boolean canDrop(Object child, Object parent) {
                return OWLTreePreferences.getInstance().isTreeDragAndDropEnabled() && canAcceptDrop(child, parent);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final OWLEntity updateView() {
        OWLSelectionModel sm = getOWLWorkspace().getOWLSelectionModel();
        OWLProperty res = null;
        if (isOWLDataPropertyView()) {
            res = updateView((O) sm.getLastSelectedDataProperty());
        } else if (isOWLObjectPropertyView()) {
            res = updateView((O) sm.getLastSelectedObjectProperty());
        }
        if (res != null) {
            updateRegisteredActions();
        } else {
            disableRegisteredActions();
        }
        return res;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Findable
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    public List<O> find(String match) {
        OWLEntityFinder ef = getOWLModelManager().getOWLEntityFinder();
        if (isOWLDataPropertyView()) {
            return new ArrayList<>((Set<O>) ef.getMatchingOWLDataProperties(match));
        } else if (isOWLObjectPropertyView()) {
            return new ArrayList<>((Set<O>) ef.getMatchingOWLObjectProperties(match));
        }
        return Collections.emptyList();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CreateNewChildTarget
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canCreateNewChild() {
        return isInAssertedMode() && getSelectedEntity() != null;
    }

    @Override
    public void createNewChild() {
        O selectedProperty = getSelectedEntity();
        if (selectedProperty == null) {
            return;
        }
        OWLEntityCreationSet<O> set = createProperty();
        if (set == null) {
            return;
        }
        List<OWLOntologyChange> changes = new ArrayList<>(set.getOntologyChanges());
        if (shouldAddAsParentOfNewlyCreatedProperty(selectedProperty)) {
            OWLAxiom ax = getSubPropertyAxiom(set.getOWLEntity(), selectedProperty);
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        }
        getOWLModelManager().applyChanges(changes);
        getTree().setSelectedOWLObject(set.getOWLEntity());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CreateNewSiblingTarget
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canCreateNewSibling() {
        O e = getSelectedEntity();
        return isInAssertedMode() && e != null && !e.equals(getTopProperty());
    }

    @Override
    public void createNewSibling() {
        O property = getTree().getSelectedOWLObject();
        if (property == null) {
            // Shouldn't really get here, because the
            // action should be disabled
            return;
        }
        // We need to apply the changes in the active ontology
        OWLEntityCreationSet<O> creationSet = createProperty();
        if (creationSet == null) {
            return;
        }
        // Combine the changes that are required to create the OWLAnnotationProperty, with the
        // changes that are required to make it a sibling property.
        List<OWLOntologyChange> changes = new ArrayList<>(creationSet.getOntologyChanges());
        OWLOntology ont = getOWLModelManager().getActiveOntology();
        for (O parentProperty : getHierarchyProvider().getParents(property)) {
            if (shouldAddAsParentOfNewlyCreatedProperty(parentProperty)) {
                OWLAxiom ax = getSubPropertyAxiom(creationSet.getOWLEntity(), parentProperty);
                changes.add(new AddAxiom(ont, ax));
            }
        }
        getOWLModelManager().applyChanges(changes);
        getTree().setSelectedOWLObject(creationSet.getOWLEntity());
    }

    /*
     * This code will get me into trouble if and when a hierarchy does not have owl:topObject/DataProperty
     * as the root of the hierarchy.  I don't know if this is possible yet but it can be imagined. By adding
     * a protected method we allow for the possibility that this behavior can be overridden.
     */
    protected boolean shouldAddAsParentOfNewlyCreatedProperty(O parent) {
        return !getHierarchyProvider().hasRoot(parent);
    }

    private O getTopProperty() throws NoSuchElementException {
        return getHierarchyProvider().roots().findFirst().orElseThrow(NoSuchElementException::new);
    }
}
