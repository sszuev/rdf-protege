package org.protege.editor.owl.ui.frame.individual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;

import java.util.stream.Stream;

/**
 * Created by @ssz on 23.02.2020.
 */
public abstract class AbstractPropertyAssertionFrameSectionRow<R, A extends OWLPropertyAssertionAxiom<?, ?>, E>
        extends AbstractOWLFrameSectionRow<R, A, E> {

    protected AbstractPropertyAssertionFrameSectionRow(OWLEditorKit kit,
                                                       OWLFrameSection<R, A, E> section,
                                                       OWLOntology ontology,
                                                       R root,
                                                       A axiom) {
        super(kit, section, ontology, root, axiom);
    }

    @Override
    public final Stream<OWLObject> manipulatableObjects() {
        OWLPropertyAssertionAxiom<?,?> ax = getAxiom();
        return Stream.of(ax.getProperty(), ax.getObject());
    }

    @Override
    public final String getDelimiter() {
        return "  ";
    }
}
