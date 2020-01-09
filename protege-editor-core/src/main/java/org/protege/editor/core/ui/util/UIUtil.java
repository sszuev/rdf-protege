package org.protege.editor.core.ui.util;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.EditorKitManager;
import org.protege.editor.core.platform.OSUtils;
import org.protege.editor.core.platform.apple.MacUIUtil;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Apr 2, 2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class UIUtil {

    public static final String FILE_PREFERENCES_KEY = "FILE_PREFERENCES_KEY";
    public static final String CURRENT_FILE_DIRECTORY_KEY = "CURRENT_FILE_DIRECTORY_KEY";
    public static final String FILE_URI_SCHEME = "file";

    public static String getCurrentFileDirectory() {
        String dir = "~";
        Preferences p = PreferencesManager.getInstance().getApplicationPreferences(FILE_PREFERENCES_KEY);
        dir = p.getString(CURRENT_FILE_DIRECTORY_KEY, dir);
        return dir;
    }

    public static void setCurrentFileDirectory(String dir) {
        Preferences p = PreferencesManager.getInstance().getApplicationPreferences(FILE_PREFERENCES_KEY);
        p.putString(CURRENT_FILE_DIRECTORY_KEY, dir);
    }

    public static File openFile(Component parent, String title, String description, Set<String> extensions) {
        return openFile(parent, title, description, extensions, null);
    }

    public static File openFile(Component parent,
                                String title,
                                String description,
                                Set<String> extensions,
                                JPanel settings) {
        if (OSUtils.isOSX() && parent instanceof Window) {
            return MacUIUtil.openFile((Window) parent, title, extensions);
        }
        String dir = getCurrentFileDirectory();
        JFileChooser dialog = new FileChooser(dir, settings);
        if (extensions != null && !extensions.isEmpty()) {
            dialog.setFileFilter(createFileChooserFilter(description, extensions));
        }
        dialog.setDialogType(JFileChooser.OPEN_DIALOG);
        int retVal = dialog.showOpenDialog(parent);
        File res;
        if (retVal != JFileChooser.APPROVE_OPTION || (res = dialog.getSelectedFile()) == null) {
            return null;
        }
        if (res.getParent() != null) {
            setCurrentFileDirectory(res.getParent());
        }
        return res;
    }

    private static FileFilter createFileChooserFilter(String description, Set<String> extensions) {
        return new FileFilter() {
            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public boolean accept(File f) {
                if (extensions.isEmpty() || f.isDirectory()) {
                    return true;
                }
                String name = f.getName().toLowerCase();
                for (String ext : extensions) {
                    if (name.endsWith(ext.toLowerCase())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static File saveFile(Component parent, String title, String description, Set<String> extensions, String initialName) {
        Window parentWindow;
        if (parent instanceof Window) {
            parentWindow = (Window) parent;
        } else {
            parentWindow = SwingUtilities.getWindowAncestor(parent);
        }
        if (OSUtils.isOSX() && parentWindow != null) {
            return MacUIUtil.saveFile(parentWindow, title, extensions, initialName);
        }
        JFileChooser fileDialog = new JFileChooser(getCurrentFileDirectory());
        fileDialog.setDialogTitle(title);
        if (extensions != null && !extensions.isEmpty()) {
            fileDialog.setFileFilter(createFileChooserFilter(description, extensions));
        }
        fileDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        if (initialName != null) {
            fileDialog.setSelectedFile(new File(initialName));
        }
        int retVal = fileDialog.showSaveDialog(parent);

        File res;
        if (retVal != JFileChooser.APPROVE_OPTION || (res = fileDialog.getSelectedFile()) == null) {
            return null;
        }
        if (res.getParent() != null) {
            setCurrentFileDirectory(res.getParent());
        }
        return res;
    }

    public static File saveFile(Window parent, String title, String description, Set<String> extensions) {
        return saveFile(parent, title, description, extensions, null);
    }

    public static File chooseFolder(Component parent, String title) {
        if (System.getProperty("os.name").contains("OS X")) {
            return MacUIUtil.chooseOSXFolder(parent, title);
        }
        JFileChooser chooser = new JFileChooser();
        File currentDirectory = new File(getCurrentFileDirectory());
        chooser.setSelectedFile(currentDirectory);
        chooser.setDialogTitle(title);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = chooser.getSelectedFile();
            if (selectedDirectory != null) {
            	setCurrentFileDirectory(selectedDirectory.toString());
            }
            return selectedDirectory;
        }
        return null;
    }

    public static void openRequest(OpenRequestHandler handler) throws Exception {
        ProtegeManager pm = ProtegeManager.getInstance();
        EditorKitManager editorKitManager = pm.getEditorKitManager();
        if (editorKitManager.getEditorKitCount() == 1) {
            EditorKit editorKit = editorKitManager.getEditorKits().get(0);
            if (!editorKit.hasModifiedDocument()) {
                handler.openInNewWorkspace();
                return;
            }
        }
        int ret = JOptionPane.showConfirmDialog(handler.getCurrentWorkspace(),
                                      "Do you want to open the ontology in the current window?",
                                      "Open in current window",
                                      JOptionPane.YES_NO_CANCEL_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);

        if (ret == JOptionPane.YES_OPTION) {
            handler.openInCurrentWorkspace();
        } else if (ret == JOptionPane.NO_OPTION) {
            handler.openInNewWorkspace();
        }
    }

    public static <T> Collection<T> getComponentsExtending(Component component, Class<? extends T> clazz) {
        Collection<T> components = new ArrayList<>();
        addComponentsExtending(component, clazz, components);
        return components;
    }

    private static <T> void addComponentsExtending(Component component,
                                                   Class<? extends T> clazz,
                                                   Collection<T> components) {
        if (!(component instanceof Container)) {
            return;
        }
        Container container = (Container) component;
        int nSubcomponents = container.getComponentCount();
        for (int i = 0; i < nSubcomponents; ++i) {
            Component subComponent = container.getComponent(i);
            if (clazz.isAssignableFrom(subComponent.getClass())) {
                components.add(clazz.cast(subComponent));
            } else {
                addComponentsExtending(subComponent, clazz, components);
            }
        }
    }

    /**
     * Tests to see if a URI represents a local file.
     *
     * @param uri The URI.  May be <code>null</code>.
     * @return <code>true</code> if the URI represents a local file, otherwise <code>false</code>.
     */
    public static boolean isLocalFile(URI uri) {
        if (uri == null) {
            return false;
        }
        String scheme = uri.getScheme();
        return scheme != null && FILE_URI_SCHEME.equals(scheme.toLowerCase());
    }

    /**
     * Determines whether the user is on Windows and if so whether the "high contrast" setting
     * is set to on.
     * @return {@code true} if on, otherwise {@code false}
     */
    public static boolean isHighContrastOn() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Optional<Boolean> highContrast = Optional.ofNullable((Boolean) toolkit.getDesktopProperty("win.highContrast.on"));
        return highContrast.orElse(false);
    }

    /**
     * An extended file-chooser with additional panel near buttons.
     */
    static class FileChooser extends JFileChooser {

        FileChooser(String currentDirectoryPath, Component settings) {
            super(currentDirectoryPath);
            if (settings != null) {
                initCheckbox(settings);
            }
        }

        void initCheckbox(Component c0) {
            JPanel panel = (JPanel) ((JPanel) this.getComponent(3)).getComponent(3);
            Component c1 = panel.getComponent(0);
            Component c2 = panel.getComponent(1);
            panel.removeAll();

            panel.add(c0);
            panel.add(c1);
            panel.add(c2);
        }
    }

}
