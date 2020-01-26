package org.protege.editor.owl.ui.view.rdf;

import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;

import javax.swing.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The base impl {@link VerifiedInputEditor}, that is also {@link JPanel}.
 * Created by @ssz on 25.01.2020.
 */
abstract class VerifiedInputEditorJPanel extends JPanel implements VerifiedInputEditor {
    protected final Set<InputVerificationStatusChangedListener> listeners;

    public VerifiedInputEditorJPanel() {
        super();
        this.listeners = new CopyOnWriteArraySet<>();
    }

    protected abstract boolean isOK();

    @Override
    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.add(Objects.requireNonNull(listener));
        listener.verifiedStatusChanged(false);
    }

    @Override
    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.remove(listener);
    }

    protected void testValid() {
        setValid(isOK());
    }

    protected void setValid(boolean status) {
        listeners.forEach(x -> x.verifiedStatusChanged(status));
    }
}
