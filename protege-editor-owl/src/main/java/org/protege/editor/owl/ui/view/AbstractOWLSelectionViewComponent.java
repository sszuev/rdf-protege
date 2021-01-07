package org.protege.editor.owl.ui.view;

import org.protege.editor.core.ProtegeProperties;
import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewComponentPlugin;
import org.protege.editor.core.util.HandlerRegistration;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.model.selection.SelectionPlane;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.event.HierarchyListener;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


/**
 * A view that has a notion of a selected <code>OWLObject</code>
 * <p>
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Apr 8, 2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 * <p>
 */
public abstract class AbstractOWLSelectionViewComponent extends AbstractOWLViewComponent implements RefreshableComponent {

    private OWLSelectionModelListener listener;
    private Set<OWLSelectionViewAction> registeredActions;
    private boolean initialUpdatePerformed;
    private OWLModelManagerListener modelManagerListener;
    private Object lastDisplayedObject;
    private OWLEntityRendererListener entityRendererListener;
    private HierarchyListener hierarchyListener;
    private boolean needsRefresh;

    /**
     * The initialise method is called at the start of a plugin instance life cycle.
     * This method is called to give the plugin a chance to initialise itself.
     * All plugin initialisation should be done in this method rather than the plugin constructor,
     * since the initialisation might need to occur at a point after plugin instance creation,
     * and a each plugin must have a zero argument constructor.
     */
    @Override
    public final void initialiseOWLView() throws Exception {
        registeredActions = new HashSet<>();
        listener = () -> {
            final OWLObject owlObject = getOWLWorkspace().getOWLSelectionModel().getSelectedOWLObject();
            if (owlObject instanceof OWLEntity) {
                if (canShowEntity((OWLEntity) owlObject)) {
                    updateViewContentAndHeader();
                }
            }
        };

        entityRendererListener = (entity, renderer) -> {
            if (lastDisplayedObject != null) {
                if (lastDisplayedObject.equals(entity)) {
                    updateHeader(lastDisplayedObject);
                }
            }
        };

        hierarchyListener = e -> {
            if (needsRefresh && isShowing()) {
                updateViewContentAndHeader();
            }
        };

        modelManagerListener = event -> {
            if (event.isType(EventType.ENTITY_RENDERER_CHANGED)) {
                getOWLModelManager().getOWLEntityRenderer().addListener(entityRendererListener);
            }
        };

        addHierarchyListener(hierarchyListener);

        getOWLModelManager().addListener(modelManagerListener);
        getOWLModelManager().getOWLEntityRenderer().addListener(entityRendererListener);
        getOWLWorkspace().getOWLSelectionModel().addListener(listener);

        initialiseView();
        updateViewContentAndHeader();
        if (this instanceof SelectionDriver) {
            getSelectionPlane().ifPresent(plane -> {
                HandlerRegistration registration = plane.registerSelectionDriver((SelectionDriver) this);
                addHandlerRegistration(registration);
            });
        }
    }

    private Optional<SelectionPlane> getSelectionPlane() {
        return Optional.ofNullable((SelectionPlane) SwingUtilities.getAncestorOfClass(SelectionPlane.class, this));
    }

    @Override
    public void refreshComponent() {
        updateHeader(lastDisplayedObject);
    }

    /**
     * A convenience method that sets the specified entity to be
     * the selected entity in the <code>OWLSelectionModel</code>.
     */
    protected void setGlobalSelection(OWLEntity owlEntity) {
        View view = getView();
        if (view == null) {
            return;
        }
        if (!view.isSyncronizing()) {
            return;
        }
        if (this instanceof SelectionDriver) {
            getSelectionPlane().ifPresent(d -> d.transmitSelection((SelectionDriver) this, owlEntity));
        } else {
            getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(owlEntity);
        }
    }

    protected void registerSelectionAction(OWLSelectionViewAction action) {
        registeredActions.add(action);
    }

    protected void addAction(OWLSelectionViewAction action, String group, String groupIndex) {
        registerSelectionAction(action);
        super.addAction(action, group, groupIndex);
    }

    public abstract void initialiseView() throws Exception;

    /**
     * This method is called at the end of a plugin life cycle, when the plugin needs to be removed from the system.
     * Plugins should remove any listeners that they setup and perform other cleanup, so that the plugin can be garbage collected.
     */
    @Override
    public final void disposeOWLView() {
        registeredActions.clear();
        if (listener != null) {
            getOWLWorkspace().getOWLSelectionModel().removeListener(listener);
        }
        removeHierarchyListener(hierarchyListener);
        getOWLModelManager().removeListener(modelManagerListener);
        getOWLModelManager().getOWLEntityRenderer().removeListener(entityRendererListener);
        disposeView();
    }

    public abstract void disposeView();

    protected void disableRegisteredActions() {
        for (OWLSelectionViewAction action : registeredActions) {
            action.setEnabled(false);
        }
    }

    protected void updateRegisteredActions() {
        for (OWLSelectionViewAction action : registeredActions) {
            action.updateState();
        }
    }

    protected void updateViewContentAndHeader() {
        if (!isShowing()) {
            needsRefresh = true;
            return;
        }
        needsRefresh = false;
        if (isPinned() && initialUpdatePerformed) {
            return;
        }
        initialUpdatePerformed = true;
        if (isSynchronizing()) {
            lastDisplayedObject = updateView();
            updateHeader(lastDisplayedObject);
        }
    }

    protected void updateHeader(Object object) {
        // Set the label in the header to reflect the entity that the view
        // is displaying
        if (object != null) {
            updateRegisteredActions();
            getView().setHeaderText(getRendering(object));
        } else {
            // Not displaying an entity, so disable all actions
            disableRegisteredActions();
            getView().setHeaderText("");
        }
    }

    /**
     * Requests that the view is updated to display the current selection.
     *
     * @return The OWLEntity that the view is displaying.
     * This list is typically used to generate the view header text
     * to give the user an indication of what the view is displaying.
     */
    protected abstract Object updateView();

    protected boolean isOWLClassView() {
        return canNavigate(ProtegeProperties.CLASS_VIEW_CATEGORY);
    }

    protected boolean isOWLObjectPropertyView() {
        return canNavigate(ProtegeProperties.OBJECT_PROPERTY_VIEW_CATEGORY);
    }

    protected boolean isOWLDataPropertyView() {
        return canNavigate(ProtegeProperties.DATA_PROPERTY_VIEW_CATEGORY);
    }

    protected boolean isOWLIndividualView() {
        return canNavigate(ProtegeProperties.INDIVIDUAL_VIEW_CATEGORY);
    }

    protected boolean isOWLAnnotationPropertyView() {
        return canNavigate(ProtegeProperties.ANNOTATION_PROPERTY_VIEW_CATEGORY);
    }

    protected boolean isOWLDatatypeView() {
        return canNavigate(ProtegeProperties.DATATYPE_VIEW_CATEGORY);
    }

    // by default, asks the plugin whether the entity can be displayed
    private boolean canNavigate(String type) {
        ViewComponentPlugin plugin = getWorkspace().getViewManager().getViewComponentPlugin(getView().getId());
        return plugin != null && plugin.getNavigates().contains(ProtegeProperties.getInstance().getProperty(type));
    }

    public final boolean canShowEntity(OWLEntity owlEntity) {
        return owlEntity != null && new AcceptableEntityVisitor().canShowEntity(owlEntity);
    }

    @SuppressWarnings("NullableProblems")
    class AcceptableEntityVisitor implements OWLEntityVisitor {
        boolean result;

        public boolean canShowEntity(OWLEntity owlEntity) {
            result = false;
            owlEntity.accept(this);
            return result;
        }

        @Override
        public void visit(OWLClass owlClass) {
            result = isOWLClassView();
        }

        @Override
        public void visit(OWLObjectProperty owlObjectProperty) {
            result = isOWLObjectPropertyView();
        }

        @Override
        public void visit(OWLDataProperty owlDataProperty) {
            result = isOWLDataPropertyView();
        }

        @Override
        public void visit(OWLNamedIndividual owlIndividual) {
            result = isOWLIndividualView();
        }

        @Override
        public void visit(OWLDatatype owlDatatype) {
            result = isOWLDatatypeView();
        }

        @Override
        public void visit(OWLAnnotationProperty owlAnnotationProperty) {
            result = isOWLAnnotationPropertyView();
        }
    }
}
