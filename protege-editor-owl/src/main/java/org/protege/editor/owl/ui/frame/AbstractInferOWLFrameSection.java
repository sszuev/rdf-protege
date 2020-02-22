package org.protege.editor.owl.ui.frame;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Created by @ssz on 22.02.2020.
 */
public abstract class AbstractInferOWLFrameSection<R, A extends OWLAxiom, E> extends AbstractOWLFrameSection<R, A, E> {

    protected AbstractInferOWLFrameSection(OWLEditorKit kit,
                                           String label,
                                           String rowLabel,
                                           OWLFrame<? extends R> frame) {
        super(kit, label, rowLabel, frame);
    }

    protected abstract void infer();

    protected abstract ReasonerPreferences.OptionalInferenceTask getOptionalInferenceTask();

    @Override
    protected void refillInferred() {
        getReasonerPreferences().executeTask(getOptionalInferenceTask(), this::infer);
    }

    protected ReasonerPreferences getReasonerPreferences() {
        return getOWLModelManager().getReasonerPreferences();
    }
}
