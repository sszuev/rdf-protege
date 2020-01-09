package org.protege.editor.owl.ui.action;

import org.protege.editor.core.OWLSource;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKitFactoryPlugin;
import org.protege.editor.core.ui.OpenFromURLPanel;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.core.ui.util.OpenRequestHandler;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.core.ui.workspace.Workspace;
import org.protege.editor.owl.OWLEditorKit;

import java.awt.event.ActionEvent;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 20-Dec-2006<br><br>
 */
public class OpenFromURLAction extends ProtegeOWLAction implements OpenRequestHandler {
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            UIUtil.openRequest(this);
        } catch (Exception e1) {
            ErrorLogPanel.showErrorDialog(e1);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void initialise() throws Exception {
    }

    @Override
    public Workspace getCurrentWorkspace() {
        return getWorkspace();
    }

    @Override
    public void openInNewWorkspace() throws Exception {
        OWLSource src = OpenFromURLPanel.showDialog();
        if (src.isEmpty()) {
            return;
        }
        String id = getEditorKit().getEditorKitFactory().getId();
        for (EditorKitFactoryPlugin plugin : ProtegeManager.getInstance().getEditorKitFactoryPlugins()) {
            if (id.equals(plugin.getId())) {
                ProtegeManager.getInstance().loadAndSetupEditorKitFromURI(plugin, src);
                break;
            }
        }
    }

    @Override
    public void openInCurrentWorkspace() {
        OWLSource src = OpenFromURLPanel.showDialog();
        if (src.isEmpty()) return;
        OWLEditorKit kit = getOWLEditorKit();
        kit.handleLoadFrom(src.toURI(), src.configure(kit.getLoadConfig()));
    }
}
