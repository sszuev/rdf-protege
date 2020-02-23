package org.protege.editor.owl.ui.editor;

import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.selector.OWLClassSelectorPanel;
import org.semanticweb.owlapi.model.AsOWLClass;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 26-Feb-2007<br><br>
 */
public class OWLClassExpressionSetEditor
        extends AbstractOWLObjectEditor<Set<OWLClassExpression>> implements VerifiedInputEditor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OWLClassExpressionSetEditor.class);

    private final OWLEditorKit kit;

    private OWLClassSelectorPanel classSelectorPanel;
    private JComponent editorComponent;
    private ExpressionEditor<Set<OWLClassExpression>> expressionEditor;
    private JTabbedPane tabbedPane;
    private Collection<OWLClassExpression> initialSelection;
    private Collection<InputVerificationStatusChangedListener> listeners = new ArrayList<>();

    private final ChangeListener changeListener = x -> listeners.forEach(l -> l.verifiedStatusChanged(isValid()));

    public OWLClassExpressionSetEditor(OWLEditorKit kit) {
        this.kit = kit;
    }

    public OWLClassExpressionSetEditor(OWLEditorKit kit, Collection<OWLClassExpression> selected) {
        this.kit = Objects.requireNonNull(kit);
        this.initialSelection = selected;
    }

    private void createEditor() {
        editorComponent = new JPanel(new BorderLayout());
        OWLExpressionChecker<Set<OWLClassExpression>> checker = kit.getModelManager()
                .getOWLExpressionCheckerFactory().getOWLClassExpressionSetChecker();
        expressionEditor = new ExpressionEditor<>(kit, checker);
        JPanel holderPanel = new JPanel(new BorderLayout());
        holderPanel.add(expressionEditor);
        holderPanel.setPreferredSize(new Dimension(500, 400));
        holderPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        if (initialSelection == null) {
            classSelectorPanel = new OWLClassSelectorPanel(kit);
        } else {
            Set<OWLClass> clses = getNamedClassesFromInitialSelection();
            if (clses.size() == initialSelection.size()) { // only show and initialise the tree if all are named
                classSelectorPanel = new OWLClassSelectorPanel(kit);
                classSelectorPanel.setSelection(clses);
            }
            expressionEditor.setText(generateListText());
        }

        if (classSelectorPanel != null) {
            classSelectorPanel.addSelectionListener(changeListener);

            tabbedPane = new JTabbedPane();
            tabbedPane.add("Class hierarchy", classSelectorPanel);
            tabbedPane.add("Expression editor", holderPanel);
            tabbedPane.addChangeListener(changeListener);
            editorComponent.add(tabbedPane, BorderLayout.CENTER);
        } else {
            editorComponent.add(holderPanel, BorderLayout.CENTER);
        }
    }

    private String generateListText() {
        return initialSelection.stream()
                .map(c -> kit.getModelManager().getRendering(c))
                .collect(Collectors.joining(", "));
    }

    private Set<OWLClass> getNamedClassesFromInitialSelection() {
        if (initialSelection == null) {
            return new HashSet<>();
        }
        return initialSelection.stream()
                .filter(c -> !c.isAnonymous())
                .map(AsOWLClass::asOWLClass)
                .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public String getEditorTypeName() {
        return "Set of OWL Class Expressions";
    }

    @Override
    public boolean canEdit(Object object) {
        return checkSet(object, OWLClassExpression.class);
    }

    @Nonnull
    @Override
    public JComponent getEditorComponent() {
        ensureEditorExists();
//        classSelectorPanel.setSelection(owlEditorKit.getWorkspace().getOWLSelectionModel().getLastSelectedClass());
        return editorComponent;
    }

    private void ensureEditorExists() {
        if (editorComponent == null) {
            createEditor();
        }
    }

    @Nullable
    @Override
    public Set<OWLClassExpression> getEditedObject() {
        ensureEditorExists();
        if (tabbedPane != null && tabbedPane.getSelectedComponent().equals(classSelectorPanel)) {
            return new HashSet<>(classSelectorPanel.getSelectedObjects());
        }
        try {
            return expressionEditor.createObject();
        } catch (OWLException e) {
            LOGGER.error("An error occurred when trying to create the OWL object corresponding to the " +
                    "entered expression.", e);
        }
        return null;
    }

    @Override
    public boolean setEditedObject(Set<OWLClassExpression> expressions) {
        if (expressions == null) {
            expressions = Collections.emptySet();
        }

        ensureEditorExists();
        expressionEditor.setExpressionObject(expressions);
        if (containsOnlyNamedClasses(expressions)) {
            Set<OWLClass> clses = new HashSet<>(expressions.size());
            for (OWLClassExpression expr : expressions) {
                clses.add(expr.asOWLClass());
            }
            classSelectorPanel.setSelection(clses);
        }
        // @@TODO should remove the class selector if any of the expressions are anonymous

        return true;
    }

    private boolean containsOnlyNamedClasses(Set<OWLClassExpression> expressions) {
        if (expressions == null) {
            return true;
        }
        for (OWLClassExpression expr : expressions) {
            if (expr.isAnonymous()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        if (classSelectorPanel != null) {
            classSelectorPanel.dispose();
        }
    }

    private boolean isValid() {
        return tabbedPane != null && tabbedPane.getSelectedComponent().equals(classSelectorPanel)
                ? classSelectorPanel.getSelectedObject() != null
                : expressionEditor.isWellFormed();
    }

    @Override
    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.add(listener);
        expressionEditor.addStatusChangedListener(listener);
        listener.verifiedStatusChanged(isValid());
    }

    @Override
    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.remove(listener);
        expressionEditor.removeStatusChangedListener(listener);
    }
}
