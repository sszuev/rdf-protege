package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14-Oct-2007<br><br>
 */
public class InferredAxiomsFrame extends AbstractOWLFrame<OWLOntology> {

    public InferredAxiomsFrame(OWLEditorKit owlEditorKit) {
        addSection(new InferredAxiomsFrameSection(owlEditorKit, this));
    }
}
