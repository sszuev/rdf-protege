package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Dec-2007<br><br>
 */
public class AxiomListFrame extends AbstractOWLFrame<Set<OWLAxiom>> {

    public AxiomListFrame(OWLEditorKit owlEditorKit) {
        addSection(new AxiomListFrameSection(owlEditorKit, this));
    }
}
