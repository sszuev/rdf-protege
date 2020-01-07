package org.protege.editor.owl.ui.metrics;

import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.OWLOntology;

import java.awt.*;
/*
 * Copyright (C) 2007, University of Manchester
 *
 *
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 30-Jul-2007<br><br>
 */
public class AxiomMetricsViewComponent extends AbstractActiveOntologyViewComponent {

    private MetricsPanel metricsPanel;

    @Override
    protected void initialiseOntologyView() {
        metricsPanel = new MetricsPanel(getOWLEditorKit());
        setLayout(new BorderLayout());
        add(metricsPanel);
    }

    @Override
    protected void disposeOntologyView() {
        // do nothing
    }

    @Override
    protected void updateView(OWLOntology activeOntology) {
        metricsPanel.updateView(activeOntology);
    }
}
