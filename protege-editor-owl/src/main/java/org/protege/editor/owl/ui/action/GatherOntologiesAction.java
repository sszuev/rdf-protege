package org.protege.editor.owl.ui.action;

import com.github.owlcs.ontapi.OntFormat;
import org.protege.editor.owl.model.io.OntologySaver;
import org.protege.editor.owl.ui.GatherOntologiesPanel;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.UUID;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 22-May-2007<br><br>
 */
public class GatherOntologiesAction extends ProtegeOWLAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatherOntologiesAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        // Need to pop a dialog asking where to save
        GatherOntologiesPanel panel = GatherOntologiesPanel.showDialog(getOWLEditorKit());
        if (panel == null) {
            return;
        }
        OWLDocumentFormat saveAsFormat = panel.getOntologyFormat();
        File saveAsLocation = panel.getSaveLocation();
        OntologySaver.Builder ontologySaverBuilder = OntologySaver.builder();
        for (OWLOntology ont : panel.getOntologiesToSave()) {
            final OWLDocumentFormat format;
            OWLOntologyManager man = getOWLModelManager().getOWLOntologyManager();
            if (saveAsFormat != null) {
                format = saveAsFormat;
            } else {
                OWLDocumentFormat documentFormat = man.getOntologyFormat(ont);
                if (documentFormat != null) {
                    format = documentFormat;
                } else {
                    format = OntFormat.RDF_XML.createOwlFormat();
                }
            }

            URI originalPhysicalURI = man.getOntologyDocumentIRI(ont).toURI();
            String originalPath = originalPhysicalURI.getPath();
            if (originalPath == null) {
                originalPath = UUID.randomUUID().toString() + ".owl";
            }
            File originalFile = new File(originalPath);
            String originalFileName = originalFile.getName();
            File saveAsFile = new File(saveAsLocation, originalFileName);

            ontologySaverBuilder.addOntology(ont, format, IRI.create(saveAsFile));
        }
        try {
            ontologySaverBuilder.build().saveOntologies();
        } catch (OWLOntologyStorageException ex) {
            LOGGER.error("An error occurred whilst saving a gathered ontology: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(getWorkspace(), "There were errors when saving the ontologies. " +
                    "Please check the log for details.", "Error during save", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void initialise() throws Exception {
    }

    @Override
    public void dispose() throws Exception {
    }
}
