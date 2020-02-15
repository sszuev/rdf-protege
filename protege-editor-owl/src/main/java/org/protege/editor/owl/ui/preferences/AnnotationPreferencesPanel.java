package org.protege.editor.owl.ui.preferences;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.HasAnnotationPropertiesInSignature;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14-Aug-2007<br><br>
 */
public class AnnotationPreferencesPanel extends OWLPreferencesPanel {

    private Map<JCheckBox, URI> checkBoxURIMap;

    @Override
    public void initialise() throws Exception {
        setLayout(new BorderLayout());
        Box box = new Box(BoxLayout.Y_AXIS);
        OWLModelManager manager = getOWLModelManager();
        Set<OWLAnnotationProperty> annotationProperties = manager.getOntologies().stream()
                .flatMap(HasAnnotationPropertiesInSignature::annotationPropertiesInSignature)
                .collect(Collectors.toCollection(TreeSet::new));
        checkBoxURIMap = new HashMap<>();
        OWLWorkspace workspace = getOWLEditorKit().getWorkspace();
        for (OWLAnnotationProperty property : annotationProperties) {
            JCheckBox cb = new JCheckBox(manager.getRendering(property),
                    workspace.isHiddenAnnotationURI(property.getIRI().toURI()));
            checkBoxURIMap.put(cb, property.getIRI().toURI());
            box.add(cb);
            box.add(Box.createVerticalStrut(4));
            cb.setOpaque(false);
        }
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBorder(ComponentFactory.createTitledBorder("Hidden annotation URIs"));
        holder.add(new JScrollPane(box));
        add(holder);
    }

    @Override
    public void applyChanges() {
        Set<URI> hiddenURIs = new HashSet<>();
        for (JCheckBox cb : checkBoxURIMap.keySet()) {
            if (cb.isSelected()) {
                hiddenURIs.add(checkBoxURIMap.get(cb));
            }
        }
        getOWLEditorKit().getWorkspace().setHiddenAnnotationURIs(hiddenURIs);
    }

    @Override
    public void dispose() throws Exception {
    }
}
