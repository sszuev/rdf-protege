package org.protege.editor.owl.ui.action;

import com.google.common.base.Stopwatch;
import org.protege.editor.core.log.LogBanner;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.HierarchyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by @ssz on 26.01.2020.
 *
 * @param <E> anything
 */
public abstract class ObjectHierarchyDeleter<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OWLObjectHierarchyDeleter.class);

    private static final String DELETE_PREFS_KEY = "delete.preferences";
    private static final String ALWAYS_DELETE_CONFIRM = "delete.confirm.always";
    private static final String ALWAYS_CONFIRM_WHEN_DELETE_DESCENDANTS = "delete.confirm.descendants";
    private static final String DELETE_DESCENDANTS = "delete.descendants";

    private final OWLEditorKit kit;
    private final Supplier<Stream<E>> nodesProvider;
    private final HierarchyProvider<E> hierarchyProvider;
    private final String pluralName;

    public ObjectHierarchyDeleter(OWLEditorKit kit,
                                  HierarchyProvider<E> hierarchyProvider,
                                  Supplier<Stream<E>> nodeProvider,
                                  String pluralName) {
        this.kit = Objects.requireNonNull(kit);
        this.hierarchyProvider = Objects.requireNonNull(hierarchyProvider);
        this.nodesProvider = Objects.requireNonNull(nodeProvider);
        this.pluralName = Objects.requireNonNull(pluralName);
    }

    public void dispose() {
    }

    public OWLEditorKit getOWLEditorKit() {
        return kit;
    }

    protected Preferences getPreferences() {
        return PreferencesManager.getInstance().getApplicationPreferences(DELETE_PREFS_KEY);
    }

    protected String getRendering(E node) {
        return kit.getModelManager().getRendering(node);
    }

    public void performDeletion() {
        Preferences prefs = getPreferences();
        Set<E> nodes = nodesProvider.get().collect(Collectors.toSet());
        String name;
        if (nodes.size() == 1) {
            name = getRendering(nodes.iterator().next());
        } else {
            name = "selected " + pluralName;
        }

        boolean assertedSubsExist = hasAssertedSubs(nodes);
        boolean showDialog = prefs.getBoolean(ALWAYS_DELETE_CONFIRM, true);
        if (assertedSubsExist) {
            showDialog = prefs.getBoolean(ALWAYS_CONFIRM_WHEN_DELETE_DESCENDANTS, true);
        }
        if (!showDialog) {
            delete(nodes);
            return;
        }
        JComponent panel = new Box(BoxLayout.PAGE_AXIS);
        JLabel label = new JLabel(String.format("<html><body>Delete %s?" +
                "<p>All references to %s will be removed from the active ontologies.</p></body></html>", name, name));
        panel.add(label);
        String confirmLabel = "Always show this confirmation before deleting";

        JRadioButton descendantsRadioButton = null;
        boolean deleteDescendants = false;
        if (assertedSubsExist) {
            deleteDescendants = prefs.getBoolean(DELETE_DESCENDANTS, false);
            JRadioButton onlySelectedEntityRadioButton = new JRadioButton(String.format("Delete %s only", name),
                    !deleteDescendants);
            descendantsRadioButton = new JRadioButton(String.format("Delete %s and asserted descendant %s",
                    name, pluralName), deleteDescendants);
            ButtonGroup bg = new ButtonGroup();
            bg.add(onlySelectedEntityRadioButton);
            bg.add(descendantsRadioButton);

            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(onlySelectedEntityRadioButton);
            panel.add(descendantsRadioButton);
            confirmLabel += " " + pluralName + " with asserted descendants";
        }

        JCheckBox alwaysConfirmCheckbox = new JCheckBox(confirmLabel, true);

        panel.add(Box.createRigidArea(new Dimension(0, 40)));
        panel.add(alwaysConfirmCheckbox);

        int ret = JOptionPane.showConfirmDialog(kit.getWorkspace(), panel, "Delete " + name,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        if (assertedSubsExist) {
            deleteDescendants = descendantsRadioButton.isSelected();
            prefs.putBoolean(DELETE_DESCENDANTS, deleteDescendants);
            prefs.putBoolean(ALWAYS_CONFIRM_WHEN_DELETE_DESCENDANTS, alwaysConfirmCheckbox.isSelected());
        }
        prefs.putBoolean(ALWAYS_DELETE_CONFIRM, alwaysConfirmCheckbox.isSelected());
        if (deleteDescendants) {
            deleteDescendants(nodes);
        } else {
            delete(nodes);
        }
    }

    protected abstract void delete(Set<E> nodes);

    private void deleteDescendants(Set<E> selected) {
        LOGGER.info(LogBanner.start("Deleting descendants"));
        LOGGER.info("Deleting descendants of {}", selected);
        Set<E> ents = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (E ent : selected) {
            LOGGER.info("Retrieving descendants of {}", ent);
            ents.add(ent);
            hierarchyProvider.descendants(ent).forEach(ents::add);
        }
        stopwatch.stop();
        LOGGER.info("Retrieved {} entities to delete in {} ms", ents.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        delete(ents);
        LOGGER.info(LogBanner.end());
    }

    private boolean hasAssertedSubs(Set<E> nodes) {
        for (E entity : nodes) {
            if (hierarchyProvider.hasDescendants(entity)) {
                return true;
            }
        }
        return false;
    }
}
