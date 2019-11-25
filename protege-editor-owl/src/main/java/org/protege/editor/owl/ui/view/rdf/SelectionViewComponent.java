package org.protege.editor.owl.ui.view.rdf;

import org.protege.editor.core.ProtegeProperties;
import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.core.ui.view.ViewComponentPlugin;
import org.protege.editor.core.util.HandlerRegistration;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.model.selection.SelectionPlane;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
import org.protege.editor.owl.ui.view.OWLSelectionViewAction;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by @ssz on 23.11.2019.
 *
 * @see org.protege.editor.owl.ui.view.AbstractOWLViewComponent
 */
@SuppressWarnings("WeakerAccess")
public abstract class SelectionViewComponent extends ViewComponent implements RefreshableComponent {

    private OWLSelectionModelListener listener;
    private Set<OWLSelectionViewAction> registeredActions;
    private boolean initialUpdatePerformed;
    private OWLModelManagerListener modelManagerListener;
    private OWLObject lastDisplayedObject;
    private OWLEntityRendererListener entityRendererListener;
    private HierarchyListener hierarchyListener;
    private boolean needsRefresh;

    private static void removeFromInputMap(KeyStroke ks, JComponent c) {
        // Most likely stored in the ancestor of focused component map,
        // but...
        removeKeyBinding(c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), ks);
        removeKeyBinding(c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), ks);
        removeKeyBinding(c.getInputMap(JComponent.WHEN_FOCUSED), ks);
        // Process children recursively
        for (Component child : c.getComponents()) {
            if (child instanceof JComponent) {
                removeFromInputMap(ks, (JComponent) child);
            }
        }
    }

    private static void removeKeyBinding(InputMap im, KeyStroke ks) {
        // Remove the key binding from the second "tier" input map
        // User bindings are stored in the first tier, where as UI
        // bindings (installed by the LAF) are stored in the second tier;
        if (im.getParent() != null) {
            im.getParent().remove(ks);
        }
    }

    /**
     * The initialise method is called at the start of a
     * plugin instance life cycle.
     * This method is called to give the plugin a chance
     * to intitialise itself.  All plugin initialisation
     * should be done in this method rather than the plugin
     * constructor, since the initialisation might need to
     * occur at a point after plugin instance creation, and
     * a each plugin must have a zero argument constructor.
     */
    final public void initialiseOWLView() throws Exception {
        registeredActions = new HashSet<>();
        listener = () -> {
            final OWLObject owlObject = getOWLWorkspace().getOWLSelectionModel().getSelectedObject();
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
     * A convenience method that sets the specified entity to be the
     * selected entity in the <code>OWLSelectionModel</code>.
     */
    @SuppressWarnings("SameParameterValue")
    protected void setGlobalSelection(OWLEntity owlEntity) {
        if (getView() != null) {
            if (getView().isSyncronizing()) {
                if (this instanceof SelectionDriver) {
                    getSelectionPlane().ifPresent(d -> d.transmitSelection((SelectionDriver) this, owlEntity));
                } else {
                    getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(owlEntity);
                }
            }
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
     * This method is called at the end of a plugin
     * life cycle, when the plugin needs to be removed
     * from the system.  Plugins should remove any listeners
     * that they setup and perform other cleanup, so that
     * the plugin can be garbage collected.
     */
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

    protected void updateHeader(OWLObject object) {
        // Set the label in the header to reflect the entity that the view
        // is displaying
        if (object != null) {
            updateRegisteredActions();
            getView().setHeaderText(getOWLModelManager().getRendering(object));
        } else {
            // Not displaying an entity, so disable all actions
            disableRegisteredActions();
            getView().setHeaderText("");
        }
    }

    /**
     * Request that the view is updated to display the current selection.
     *
     * @return The OWLEntity that the view is displaying.  This
     * list is typically used to generate the view header text to give the
     * user an indication of what the view is displaying.
     */
    protected abstract OWLObject updateView();

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

    public final void initialise() throws Exception {
        initialiseOWLView();

        prepareCopyable();
        preparePasteable();
        prepareCuttable();
        // add refresh components here (perhaps) when the class trees don't do their crazy stuff
    }

    private void prepareCopyable() {
        // The "global" copy action should take precedence over anything, so remove any
        // copy key bindings from children
        // Remove copy from the input map of any children
        removeFromInputMap(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                this);
    }

    private void preparePasteable() {
        // The "global" paste action should take precedence over anything, so remove any
        // paste action key bindings from children
        removeFromInputMap(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                this);
    }

    private void prepareCuttable() {
        // The "global" cut action should take precedence over anything, so remove any
        // cut action key bindings from children
        removeFromInputMap(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                this);
    }

    protected OWLDataFactory getOWLDataFactory() {
        return getOWLModelManager().getOWLDataFactory();
    }

    final public void dispose() {
        disposeOWLView();
        super.dispose();
    }

    public OWLModelManager getOWLModelManager() {
        return getOWLWorkspace().getOWLEditorKit().getModelManager();
    }

    public OWLEditorKit getOWLEditorKit() {
        return getOWLWorkspace().getOWLEditorKit();
    }

    public OWLWorkspace getOWLWorkspace() {
        return (OWLWorkspace) getWorkspace();
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

