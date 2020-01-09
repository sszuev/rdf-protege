package org.protege.editor.core.platform.apple;

import org.protege.editor.core.ui.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Set;

public class MacUIUtil {

    public static File openFile(Window parent, String title, final Set<String> extensions) {
        FileDialog dialog = createDialog(parent, title, FileDialog.LOAD);
        dialog.setFilenameFilter(createFilenameFilter(extensions));
        dialog.setDirectory(UIUtil.getCurrentFileDirectory());
        dialog.setVisible(true);
        String fileName = dialog.getFile();
        if (fileName == null) {
            return null;
        }
        UIUtil.setCurrentFileDirectory(dialog.getDirectory());
        return new File(dialog.getDirectory() + fileName);
    }

    public static File saveFile(Window parent, String title, Set<String> extensions, String initialName) {
        FileDialog dialog = createDialog(parent, title, FileDialog.SAVE);
        dialog.setFilenameFilter(createFilenameFilter(extensions));
        dialog.setDirectory(UIUtil.getCurrentFileDirectory());
        if (initialName != null) {
            dialog.setFile(initialName);
        }
        dialog.setVisible(true);

        String fileName = dialog.getFile();
        if (fileName == null) {
            return null;
        }
        UIUtil.setCurrentFileDirectory(dialog.getDirectory());
        return new File(dialog.getDirectory() + fileName);
    }

    private static FilenameFilter createFilenameFilter(Set<String> extensions) {
        return (dir, name) -> {
            if (extensions == null || extensions.isEmpty()) {
                return true;
            }
            for (String ext : extensions) {
                if (name.toLowerCase().endsWith(ext.toLowerCase())) {
                    return true;
                }
            }
            return false;
        };
    }

    private static FileDialog createDialog(Window parent, String title, int constant) {
        return parent instanceof Frame ?
                new FileDialog((Frame) parent, title, constant) :
                new FileDialog((Dialog) parent, title, constant);
    }

    public static File chooseOSXFolder(Component parent, String title) {
        String prop = null;
        File file;
        try {
            prop = "apple.awt.fileDialogForDirectories";
            System.setProperty(prop, "true");
            file = UIUtil.openFile(SwingUtilities.getAncestorOfClass(Frame.class, parent),
                    title, "Folder", Collections.singleton(""));
        } finally {
            System.setProperty(prop, "false");
        }
        return file;
    }
}
