package org.protege.editor.owl.ui.renderer.menu;

import org.github.owlcs.ontapi.OWLManager;
import org.protege.editor.core.ui.action.ProtegeDynamicAction;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.renderer.OWLEntityAnnotationValueRenderer;
import org.protege.editor.owl.ui.renderer.OWLEntityRendererImpl;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.protege.editor.owl.ui.renderer.plugin.RendererPlugin;
import org.semanticweb.owlapi.model.HasAnnotationPropertiesInSignature;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RenderByProperty extends ProtegeDynamicAction {

    private static final long serialVersionUID = 8119262495644333132L;
    private static final OWLAnnotationProperty LABEL = OWLManager.getOWLDataFactory().getRDFSLabel();

    private JMenu menu;
    private OWLModelManagerListener listener;

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void rebuildChildMenuItems(JMenu thisMenuItem) {
        annotationProperties().collect(Collectors.toCollection(() -> new TreeSet<>(getComparator())))
                .forEach(prop -> thisMenuItem.add(new PropertyMenuItem(prop)));
    }

    private Stream<OWLAnnotationProperty> annotationProperties() {
        return getOWLModelManager().getActiveOntologies().stream()
                .flatMap(HasAnnotationPropertiesInSignature::annotationPropertiesInSignature);
    }

    public OWLModelManager getOWLModelManager() {
        return ((OWLModelManager) getEditorKit().getModelManager());
    }

    private Comparator<OWLAnnotationProperty> getComparator() {
        return Comparator.comparing(p -> getOWLModelManager().getRendering(p));
    }

    public void setMenu(JMenu menu) {
        this.menu = menu;
        updateCheckedStatus();
    }

    /*
     * Code below is copied from AbstractByRendererMenu
     */

    @Override
    public void initialise() throws Exception {
        listener = event -> {
            if (event.isType(EventType.ENTITY_RENDERER_CHANGED)) {
                updateCheckedStatus();
            }
        };
        getOWLModelManager().addListener(listener);
    }

    private void updateCheckedStatus() {
        if (menu != null) {
            RendererPlugin plugin = OWLRendererPreferences.getInstance().getRendererPlugin();
            markCheckedMenu(isMyRendererPlugin(plugin) && isConfigured());
        }
    }

    private void markCheckedMenu(boolean marked) {
        if (marked) {
            JCheckBoxMenuItem it = new JCheckBoxMenuItem();
            it.setSelected(true);
            /*
             * This is unfortunate. We cannot easily get the checkbox
             * icon from the L&F. Actually we get it, and then it
             * throws a class cast exception when it tries to paint it,
             * because it expects the component to be a JCheckBox.
             */
            menu.setIcon(Icons.getIcon("hierarchy.collapsed.gif"));
        } else {
            menu.setIcon(null);
        }
    }

    @Override
    public void dispose() throws Exception {
        getOWLModelManager().removeListener(listener);
    }

    OWLRendererPreferences getOwlRendererPreferences() {
        return OWLRendererPreferences.getInstance();
    }

    /*
     * End copied code
     */

    protected boolean isMyRendererPlugin(RendererPlugin plugin) {
        return plugin.getRendererClassName().equals(OWLEntityAnnotationValueRenderer.class.getName());
    }

    protected boolean isConfigured() {
        List<IRI> annotations = getOwlRendererPreferences().getAnnotationIRIs();
        return annotations.size() == 1 && !annotations.iterator().next().equals(LABEL.getIRI());
    }

    protected void configure(OWLAnnotationProperty prop) {
        getOwlRendererPreferences().setAnnotations(Collections.singletonList(prop.getIRI()));
    }

    private void setRendering(OWLAnnotationProperty prop) {
        OWLRendererPreferences preferences = getOwlRendererPreferences();
        for (RendererPlugin plugin : preferences.getRendererPlugins()) {
            if (isMyRendererPlugin(plugin)) {
                preferences.setRendererPlugin(plugin);
                configure(prop);
                getOWLModelManager().refreshRenderer();
                break;
            }
        }
    }

    private void resetRendering() {
        OWLRendererPreferences preferences = getOwlRendererPreferences();
        preferences.setRendererPlugin(preferences.getRendererPluginByClassName(OWLEntityRendererImpl.class.getName()));
        getOWLModelManager().refreshRenderer();
    }

    class PropertyMenuItem extends JCheckBoxMenuItem {
        private final OWLAnnotationProperty property;

        public PropertyMenuItem(OWLAnnotationProperty prop) {
            super(getOWLModelManager().getRendering(prop));
            property = prop;
            setToolTipText(property.getIRI().toString());
            addActionListener(x -> onStateChanged());
            setSelected(isRenderingProperty());
        }

        private boolean isRenderingProperty() {
            OWLRendererPreferences pref = getOwlRendererPreferences();
            if (!isMyRendererPlugin(pref.getRendererPlugin())) {
                return false;
            }
            for (IRI iri : pref.getAnnotationIRIs()) {
                if (iri.equals(property.getIRI())) {
                    return true;
                }
            }
            return false;
        }

        private void onStateChanged() {
            if (isSelected()) {
                setRendering(property);
            } else {
                resetRendering();
            }
        }
    }

}
