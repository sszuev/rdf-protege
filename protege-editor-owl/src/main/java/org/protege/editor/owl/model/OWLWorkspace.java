package org.protege.editor.owl.model;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.protege.editor.core.Fonts;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.plugin.AbstractPluginLoader;
import org.protege.editor.core.plugin.PluginUtilities;
import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.core.ui.action.ProtegeActionPluginJPFImpl;
import org.protege.editor.core.ui.error.ErrorLog;
import org.protege.editor.core.ui.error.ErrorLogListener;
import org.protege.editor.core.ui.error.ErrorNotificationLabel;
import org.protege.editor.core.ui.error.SendErrorReportHandler;
import org.protege.editor.core.ui.progress.BackgroundTaskLabel;
import org.protege.editor.core.ui.workspace.*;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ProtegeOWL;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.*;
import org.protege.editor.owl.model.selection.OWLSelectionHistoryManager;
import org.protege.editor.owl.model.selection.OWLSelectionHistoryManagerImpl;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelImpl;
import org.protege.editor.owl.ui.OWLEntityCreationPanel;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.protege.editor.owl.ui.inference.*;
import org.protege.editor.owl.ui.navigation.OWLEntityNavPanel;
import org.protege.editor.owl.ui.ontology.OntologySourcesChangedHandlerUI;
import org.protege.editor.owl.ui.preferences.AnnotationPreferences;
import org.protege.editor.owl.ui.renderer.*;
import org.protege.editor.owl.ui.search.SearchDialogPanel;
import org.protege.editor.owl.ui.util.OWLComponentFactory;
import org.protege.editor.owl.ui.util.OWLComponentFactoryImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.OWLEntityCollectingOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Mar 17, 2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLWorkspace extends TabbedWorkspace implements SendErrorReportHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OWLWorkspace.class);

    public static final String REASONER_INITIALIZE = "Start reasoner";
    public static final String REASONER_RESYNC = "Synchronize reasoner";
    public static final String REASONER_STOP = "Stop reasoner";
    public static final String REASONER_EXPLAIN = "Explain inconsistent ontology";
    private static final String WINDOW_MODIFIED = "Window.documentModified";

    private final JComboBox<OWLOntology> ontologiesList = new JComboBox<>();
    private final ArrayList<OWLEntityDisplayProvider> entityDisplayProviders = new ArrayList<>();
    private final Map<String, Color> keyWordColorMap = new KeywordColourMap();
    private final OWLSelectionModel owlSelectionModel = new OWLSelectionModelImpl();
    private final OWLSelectionHistoryManager owlSelectionHistoryManager = new OWLSelectionHistoryManagerImpl(owlSelectionModel);
    private final Set<EventType> reselectionEventTypes = new HashSet<>();
    private final ErrorNotificationLabel errorNotificationLabel = new ErrorNotificationLabel();
    private final BackgroundTaskLabel backgroundTaskLabel = new BackgroundTaskLabel(ProtegeApplication.getBackgroundTaskManager());
    private final Set<URI> hiddenAnnotationURIs = new HashSet<>();
    private final PrecomputeAction startReasonerAction = new PrecomputeAction();
    private final PrecomputeAction synchronizeReasonerAction = new PrecomputeAction();
    private final ProtegeOWLAction stopReasonerAction = new StopReasonerAction();
    private final ExplainInconsistentOntologyAction explainInconsistentOntologyAction = new ExplainInconsistentOntologyAction();
    private final JLabel reasonerStatus = new JLabel();
    private final JCheckBox displayReasonerResults = new JCheckBox("Show Inferences");
    private final JPanel statusArea = new JPanel();
    private final OWLIconProvider iconProvider = new OWLIconProviderImpl(c -> getOWLModelManager()
            .getActiveOntologies().stream()
            .anyMatch(o -> EntitySearcher.isDefined(c, o) || o.disjointUnionAxioms(c).findFirst().isPresent()));
    private final ErrorLogListener errorLogListener = new ErrorLogListener() {
        @Override
        public void errorLogged() {
            errorNotificationLabel.setVisible(true);
        }

        @Override
        public void errorLogCleared() {
            errorNotificationLabel.setVisible(false);
        }
    };
    private String altTitle;
    private boolean reasonerManagerStarted = false;
    private final OWLModelManagerListener owlModelManagerListener = event -> handleModelManagerEvent(event.getType());
    private final OWLEntityCollectingOntologyChangeListener listener = new OWLEntityCollectingOntologyChangeListener() {
        @Override
        public void ontologiesChanged() {
            verifySelection(getEntities());
        }

        @Override
        public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
            super.ontologiesChanged(changes);
            handleOntologiesChanged(changes);
        }
    };
    private OWLComponentFactory owlComponentFactory;

    private JDialog searchDialog;
    private List<ProtegeAction> extraReasonerMenuActions;

    public OWLWorkspace() {
        statusArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 1));
        statusArea.setLayout(new BoxLayout(statusArea, BoxLayout.X_AXIS));
        statusArea.add(Box.createHorizontalGlue());
        statusArea.add(reasonerStatus);
        statusArea.add(Box.createHorizontalStrut(10));
        statusArea.add(displayReasonerResults);
        statusArea.add(Box.createHorizontalStrut(10));

        reselectionEventTypes.add(EventType.ACTIVE_ONTOLOGY_CHANGED);
        reselectionEventTypes.add(EventType.ONTOLOGY_RELOADED);
        reselectionEventTypes.add(EventType.ENTITY_RENDERER_CHANGED);
        reselectionEventTypes.add(EventType.ONTOLOGY_VISIBILITY_CHANGED);
        reselectionEventTypes.add(EventType.REASONER_CHANGED);
        reselectionEventTypes.add(EventType.ONTOLOGY_CLASSIFIED);


        hiddenAnnotationURIs.addAll(AnnotationPreferences.getHiddenAnnotationURIs());
    }

    private static Optional<JMenu> getReasonerMenu(JMenuBar menuBar) {
        return getMenu(menuBar, "Reasoner");
    }

    private static Optional<JMenu> getWindowMenu(JMenuBar menuBar) {
        return getMenu(menuBar, "Window");
    }

    private static Optional<JMenu> getMenu(JMenuBar menuBar, String name) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu == null) {
                continue;
            }
            if (menu.getText() == null) {
                continue;
            }
            if (menu.getText().equals(name)) {
                return Optional.of(menu);
            }
        }
        return Optional.empty();
    }

    private static void bringComponentToFront(Component component) {
        if (component.isShowing()) {
            return;
        }
        Component parent = component.getParent();
        if (parent == null) {
            return;
        }
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedComponent(component);
        }
        bringComponentToFront(parent);
    }

    public OWLEditorKit getOWLEditorKit() {
        return (OWLEditorKit) getEditorKit();
    }

    public OWLModelManager getOWLModelManager() {
        return getOWLEditorKit().getModelManager();
    }

    @Override
    public void initialise() {
        super.initialise();

        ProtegeApplication.getLogManager().addErrorLogListener(errorLogListener);

        createActiveOntologyPanel();

        owlComponentFactory = new OWLComponentFactoryImpl(getOWLEditorKit());

        final OWLModelManager mngr = getOWLModelManager();
        mngr.addListener(owlModelManagerListener);
        mngr.addOntologyChangeListener(listener);

        OWLReasonerManager reasonerManager = mngr.getOWLReasonerManager();
        reasonerManager.setReasonerProgressMonitor(new ReasonerProgressUI(getOWLEditorKit()));
        reasonerManager.setReasonerExceptionHandler(new UIReasonerExceptionHandler(this));
        reasonerManagerStarted = true;
        updateReasonerStatus(false);
        displayReasonerResults.setSelected(reasonerManager.getReasonerPreferences().isShowInferences());
        displayReasonerResults.addActionListener(e -> {
            ReasonerPreferences prefs = reasonerManager.getReasonerPreferences();
            prefs.setShowInferences(displayReasonerResults.isSelected());
        });

        reasonerStatus.setFont(Fonts.getSmallDialogFont());
        displayReasonerResults.putClientProperty("JComponent.sizeVariant", "small");
        displayReasonerResults.setFont(Fonts.getSmallDialogFont());

        new OntologySourcesChangedHandlerUI(this);
    }

    private void handleOntologiesChanged(List<? extends OWLOntologyChange> changes) {
        boolean reasonerDirty = false;
        boolean ontologyIdsDirty = false;
        for (OWLOntologyChange chg : changes) {
            if (chg instanceof SetOntologyID) {
                ontologyIdsDirty = true;
            } else if (chg instanceof ImportChange) {
                reasonerDirty = true;
            } else if (chg instanceof OWLAxiomChange) {
                reasonerDirty = true;
            }
        }

        if (reasonerDirty) {
            updateReasonerStatus(true);
        }
        if (ontologyIdsDirty) {
            updateTitleBar();
        }
        updateDirtyFlag();
    }

    private void updateDirtyFlag() {
        WorkspaceManager workspaceManager = ProtegeManager.getInstance().getEditorKitManager().getWorkspaceManager();
        JFrame frame = workspaceManager.getFrame(this);
        Set<OWLOntology> dirtyOntologies = getOWLModelManager().getDirtyOntologies();
        boolean dirty = false;
        for (OWLOntology ont : getOWLModelManager().getOntologies()) {
            if (dirtyOntologies.contains(ont)) {
                dirty = true;
                break;
            }
        }
        frame.getRootPane().putClientProperty(WINDOW_MODIFIED, dirty);
    }

    private void handleModelManagerEvent(EventType type) {
        try {
            if (reselectionEventTypes.contains(type)) {
                verifySelection();
            }
            switch (type) {
                case ACTIVE_ONTOLOGY_CHANGED:
                    updateTitleBar();
                    updateReasonerStatus(false);
                    rebuildOntologyDropDown();
                    ontologiesList.repaint();
                    break;
                case ONTOLOGY_CLASSIFIED:
                case ABOUT_TO_CLASSIFY:
                case REASONER_CHANGED:
                    updateReasonerStatus(false);
                    break;
                case ONTOLOGY_LOADED:
                case ONTOLOGY_CREATED:
                    if (getTabCount() > 0) {
                        setSelectedTab(0);
                    }
                    break;
                case ENTITY_RENDERER_CHANGED:
                case ONTOLOGY_RELOADED:
                    rebuildOntologyDropDown();
                    refreshComponents();
                    break;
                case ONTOLOGY_SAVED:
                    updateDirtyFlag();
                    updateTitleBar();
                    break;
                case ENTITY_RENDERING_CHANGED:
                case ONTOLOGY_VISIBILITY_CHANGED:
                    break;
            }
        } catch (Exception e) {
            LOGGER.warn("An error occurred whilst handling a Model Manager Event: {}", e.getMessage(), e);
        }
    }

    public void refreshComponents() {
        refreshComponents(SwingUtilities.getAncestorOfClass(Frame.class, this));
    }

    public void refreshComponents(Component component) {
        if (component == null) {
            return;
        }
        if (component instanceof Container) {
            Container cont = (Container) component;
            for (Component childComp : cont.getComponents()) {
                refreshComponents(childComp);
            }
        }
        if (component instanceof RefreshableComponent) {
            ((RefreshableComponent) component).refreshComponent();
        }
    }

    private void verifySelection(Iterable<? extends OWLEntity> entities) {
        Set<OWLEntity> unreferencedEntities = new HashSet<>();
        for (OWLEntity entity : entities) {
            if (entity == null || entity.isBuiltIn()) {
                continue;
            }
            boolean unreferenced = true;
            for (OWLOntology ont : getOWLModelManager().getActiveOntologies()) {
                if (ont.containsEntityInSignature(entity)) {
                    unreferenced = false;
                    break;
                }
            }
            if (unreferenced) {
                unreferencedEntities.add(entity);
            }
        }
        for (OWLEntity entity : unreferencedEntities) {
            getOWLSelectionModel().clearLastSelectedEntity(entity);
        }
    }

    private void verifySelection() {
        LOGGER.debug("--- Verifying selection ---");
        OWLSelectionModel selectionModel = getOWLSelectionModel();

        OWLClass lastSelectedClass = selectionModel.getLastSelectedClass();
        LOGGER.debug("Last selected class: {}", lastSelectedClass);

        OWLObjectProperty lastSelectedObjectProperty = selectionModel.getLastSelectedObjectProperty();
        LOGGER.debug("Last selected object property: {}", lastSelectedObjectProperty);

        OWLDataProperty lastSelectedDataProperty = selectionModel.getLastSelectedDataProperty();
        LOGGER.debug("Last selected data property: {}", lastSelectedDataProperty);

        OWLAnnotationProperty lastSelectedAnnotationProperty = selectionModel.getLastSelectedAnnotationProperty();
        LOGGER.debug("Last selected annotation property: " + lastSelectedAnnotationProperty);

        OWLNamedIndividual lastSelectedIndividual = selectionModel.getLastSelectedIndividual();
        LOGGER.debug("Last selected individual: {}", lastSelectedIndividual);

        OWLDatatype lastSelectedDatatype = selectionModel.getLastSelectedDatatype();
        LOGGER.debug("Last selected datatype: {}", lastSelectedDatatype);

        OWLEntity selectedEntity = selectionModel.getSelectedEntity();
        LOGGER.debug("Last selected entity: {}", selectedEntity);

        verifySelection(CollectionFactory.createSet(lastSelectedClass, lastSelectedDataProperty, lastSelectedObjectProperty, lastSelectedAnnotationProperty, lastSelectedIndividual, lastSelectedDatatype, selectedEntity));
        LOGGER.debug("---------------------------");
    }

    public boolean isHiddenAnnotationURI(URI annotationURI) {
        return hiddenAnnotationURIs.contains(annotationURI);
    }

    public void setHiddenAnnotationURIs(Set<URI> hiddenURIs) {
        if (!hiddenURIs.equals(hiddenAnnotationURIs)) {
            hiddenAnnotationURIs.clear();
            hiddenAnnotationURIs.addAll(hiddenURIs);
            AnnotationPreferences.setHiddenAnnotationURIs(hiddenAnnotationURIs);
            getOWLEditorKit().getModelManager().fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
        }
    }

    @Override
    protected void initialiseExtraMenuItems(JMenuBar menuBar) {
        super.initialiseExtraMenuItems(menuBar);

        //getOntologiesMenu(menuBar);
        this.extraReasonerMenuActions = initialiseExtraReasonerMenuActions();

        rebuildReasonerMenu(menuBar);
        addReasonerListener(menuBar);
        updateTitleBar();

        Optional<JMenu> menu = getWindowMenu(menuBar);
        if (!menu.isPresent()) {
            return;
        }
        JMenu windowMenu = menu.get();
        windowMenu.addSeparator();
        windowMenu.add(new AbstractAction("Refresh user interface") {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshComponents();
            }
        });

    }

    private List<ProtegeAction> initialiseExtraReasonerMenuActions() {
        List<ProtegeAction> result = new ArrayList<>();
        ExtraReasonerMenuActionPluginLoader loader = new ExtraReasonerMenuActionPluginLoader();
        for (ExtraReasonerMenuActionPlugin extraReasonerMenuActionPlugin : loader.getPlugins()) {
            try {
                ProtegeAction action = extraReasonerMenuActionPlugin.newInstance();
                action.setEditorKit(getOWLEditorKit());
                String name = extraReasonerMenuActionPlugin.getName();
                if (name == null) {
                    LOGGER.warn("Extra reasoner menu action plugin has no name: {}", extraReasonerMenuActionPlugin);
                }
                action.putValue(Action.NAME, name);
                String toolTipText = extraReasonerMenuActionPlugin.getToolTipText();
                if (toolTipText != null) {
                    action.putValue(Action.SHORT_DESCRIPTION, toolTipText);
                }
                String accelerator = extraReasonerMenuActionPlugin.getAccelerator();
                if (accelerator != null) {
                    action.putValue(Action.ACCELERATOR_KEY, accelerator);
                }
                result.add(action);
                LOGGER.debug("action = {}", action);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                LOGGER.error("Error whilst loading menu plugin", e);
            }
        }
        return result;
    }

    private void rebuildReasonerMenu(JMenuBar menuBar) {
        OWLModelManager manager = getOWLModelManager();
        Optional<JMenu> menu = getReasonerMenu(menuBar);
        if (!menu.isPresent()) {
            return;
        }
        JMenu reasonerMenu = menu.get();
        reasonerMenu.removeAll();

        startReasonerAction.setEditorKit(getOWLEditorKit());
        startReasonerAction.putValue(Action.NAME, REASONER_INITIALIZE);
        reasonerMenu.add(startReasonerAction);

        synchronizeReasonerAction.setEditorKit(getOWLEditorKit());
        synchronizeReasonerAction.putValue(Action.NAME, REASONER_RESYNC);
        reasonerMenu.add(synchronizeReasonerAction);

        stopReasonerAction.setEditorKit(getOWLEditorKit());
        stopReasonerAction.putValue(Action.NAME, REASONER_STOP);
        reasonerMenu.add(stopReasonerAction);

        explainInconsistentOntologyAction.setEditorKit(getOWLEditorKit());
        explainInconsistentOntologyAction.putValue(Action.NAME, REASONER_EXPLAIN);
        explainInconsistentOntologyAction.setEnabled(false);
        reasonerMenu.add(explainInconsistentOntologyAction);

        ConfigureReasonerAction configureAction = new ConfigureReasonerAction();
        configureAction.setEditorKit(getOWLEditorKit());
        configureAction.putValue(Action.NAME, "Configure...");
        reasonerMenu.add(configureAction);

        if (extraReasonerMenuActions != null && extraReasonerMenuActions.size() > 0) {
            reasonerMenu.addSeparator();
            for (ProtegeAction action : extraReasonerMenuActions) {
                reasonerMenu.add(action);
            }
        }

        reasonerMenu.addSeparator();

        ButtonGroup bg = new ButtonGroup();
        Set<ProtegeOWLReasonerInfo> factories = manager.getOWLReasonerManager().getInstalledReasonerFactories();
        List<ProtegeOWLReasonerInfo> factoriesList = new ArrayList<>(factories);
        factoriesList.sort(new ReasonerInfoComparator());
        for (final ProtegeOWLReasonerInfo plugin : factoriesList) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(plugin.getReasonerName());
            item.setSelected(manager.getOWLReasonerManager().getCurrentReasonerFactoryId().equals(plugin.getReasonerId()));
            reasonerMenu.add(item);
            bg.add(item);
            item.addActionListener(e -> manager.getOWLReasonerManager().setCurrentReasonerFactoryId(plugin.getReasonerId()));
        }
    }

    private void addReasonerListener(JMenuBar menuBar) {
        IExtensionRegistry registry = PluginUtilities.getInstance().getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(ProtegeOWL.ID, ProtegeOWLReasonerPlugin.REASONER_PLUGIN_TYPE_ID);

        registry.addListener(new IRegistryEventListener() {

            @Override
            public void added(IExtension[] extensions) {
                OWLReasonerManagerImpl reasonerManager = (OWLReasonerManagerImpl) getOWLModelManager().getOWLReasonerManager();
                Set<ProtegeOWLReasonerPlugin> plugins = new HashSet<>();
                for (IExtension extension : extensions) {
                    plugins.add(new ProtegeOWLReasonerPluginJPFImpl(getOWLModelManager(), extension));
                }
                reasonerManager.addReasonerFactories(plugins);
                rebuildReasonerMenu(menuBar);
                menuBar.repaint();
            }

            @Override
            public void added(IExtensionPoint[] extensionPoints) {
            }

            @Override
            public void removed(IExtension[] extensions) {
            }

            @Override
            public void removed(IExtensionPoint[] extensionPoints) {
            }

        }, point.getUniqueIdentifier());

    }

    private void createActiveOntologyPanel() {
        JPanel topBarPanel = new JPanel(new GridBagLayout());
        topBarPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 3, 10));
        topBarPanel.add(new OWLEntityNavPanel(getOWLEditorKit()), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.BASELINE, GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));

        OWLModelManager manager = getOWLModelManager();

        // Install the active ontology combo box
        ontologiesList.setToolTipText("Active ontology");
        ontologiesList.setRenderer(new OWLOntologyCellRenderer(getOWLEditorKit()));
        rebuildOntologyDropDown();

        topBarPanel.add(ontologiesList, new GridBagConstraints(1, 0, 1, 1, 100, 0,
                GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        ontologiesList.addActionListener(e -> {
            OWLOntology ont = (OWLOntology) ontologiesList.getSelectedItem();
            if (ont != null) {
                manager.setActiveOntology(ont);
            }
        });

        JButton searchButton = new JButton("Search...");
        searchButton.addActionListener(e -> showSearchDialog());

        topBarPanel.add(searchButton, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                GridBagConstraints.BASELINE, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));

        topBarPanel.add(backgroundTaskLabel, new GridBagConstraints(3, 0, 1, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        topBarPanel.add(errorNotificationLabel, new GridBagConstraints(4, 0, 1, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        add(topBarPanel, BorderLayout.NORTH);

        updateTitleBar();
    }

    public void showSearchDialog() {
        if (getOWLEditorKit() == null) {
            LOGGER.info("The OWLWorkspace has not been initialised.  Not displaying the search dialog.");
            return;
        }
        if (searchDialog == null) {
            searchDialog = SearchDialogPanel.createDialog(this, getOWLEditorKit());
            Point workspaceLocation = getLocation();
            Dimension workspaceSize = getSize();
            SwingUtilities.convertPointToScreen(workspaceLocation, this);
            workspaceLocation.translate(workspaceSize.width - searchDialog.getWidth() - 10, 0);
            searchDialog.setLocation(workspaceLocation);
        }
        searchDialog.setVisible(true);
    }

    @Override
    protected String getTitle() {
        if (altTitle != null) {
            return altTitle;
        }
        final OWLModelManager mngr = getOWLModelManager();
        OWLOntology activeOntology = mngr.getActiveOntology();
        if (activeOntology == null) {
            return null;
        }
        URI locURI = mngr.getOntologyPhysicalURI(activeOntology);
        String location = "*";
        if (locURI != null) {
            if (!locURI.toString().isEmpty()) {
                location = locURI.toString();
                if ("file".equals(locURI.getScheme())) {
                    location = new File(locURI).getPath();
                }
            }
        }

        String ontShortName = mngr.getRendering(activeOntology);
        Optional<IRI> defaultDocumentIRI = activeOntology.getOntologyID().getDefaultDocumentIRI();
        String documentIRIPart = "";
        if (defaultDocumentIRI.isPresent()) {
            documentIRIPart = " (" + defaultDocumentIRI.get() + ") ";
        }
        return ontShortName + documentIRIPart + " : [" + location + "]";
    }

    public void setTitle(String title) {
        altTitle = title;
        updateTitleBar();
    }

    private void updateTitleBar() {
        Frame f = ProtegeManager.getInstance().getFrame(this);
        if (f != null) {
            f.setTitle(getTitle());
        }
        ontologiesList.repaint();
    }

    private void updateReasonerStatus(boolean changesInProgress) {
        if (!reasonerManagerStarted) {
            return;
        }
        OWLReasonerManager reasonerManager = getOWLEditorKit().getOWLModelManager().getOWLReasonerManager();
        ReasonerStatus newStatus;
        try {
            newStatus = reasonerManager.getReasonerStatus();
        } catch (ReasonerDiedException reasonerDied) {
            newStatus = ReasonerStatus.REASONER_NOT_INITIALIZED;
            ReasonerUtilities.warnThatReasonerDied(null, reasonerDied);
        }
        if (changesInProgress && (newStatus == ReasonerStatus.INITIALIZED
                || newStatus == ReasonerStatus.INCONSISTENT)
                && reasonerManager.getCurrentReasoner().getBufferingMode() == BufferingMode.BUFFERING) {
            newStatus = ReasonerStatus.OUT_OF_SYNC;
        }
        updateReasonerStatus(newStatus);
    }

    private void updateReasonerStatus(ReasonerStatus status) {
        reasonerStatus.setText(status.getDescription());

        startReasonerAction.setEnabled(status.isEnableInitialization());
        startReasonerAction.putValue(Action.SHORT_DESCRIPTION, status.getInitializationTooltip());

        synchronizeReasonerAction.setEnabled(status.isEnableSynchronization());
        synchronizeReasonerAction.putValue(Action.SHORT_DESCRIPTION, status.getSynchronizationTooltip());

        stopReasonerAction.setEnabled(status.isEnableStop());

        explainInconsistentOntologyAction.setEnabled(status == ReasonerStatus.INCONSISTENT);

        KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        startReasonerAction.putValue(Action.ACCELERATOR_KEY, status.isEnableInitialization() ? shortcut : null);
        synchronizeReasonerAction.putValue(Action.ACCELERATOR_KEY, status.isEnableSynchronization() ? shortcut : null);
    }

    public void displayOWLEntity(OWLEntity owlEntity) {
        OWLEntityDisplayProvider candidate = null;
        for (OWLEntityDisplayProvider provider : entityDisplayProviders) {
            if (!provider.canDisplay(owlEntity)) {
                continue;
            }
            if (candidate == null) {
                candidate = provider;
            }
            if (provider.getDisplayComponent().isShowing()) {
                candidate = provider;
                break;
            }
        }
        if (candidate == null) {
            return;
        }
        JComponent component = candidate.getDisplayComponent();
        if (component != null) {
            bringComponentToFront(component);
        }
    }

    @Override
    public CustomWorkspaceTabsManager getCustomTabsManager() {
        return new OWLCustomWorkspaceTabsManager();
    }

    public void registerOWLEntityDisplayProvider(OWLEntityDisplayProvider provider) {
        entityDisplayProviders.add(provider);
    }

    public void unregisterOWLEntityDisplayProvider(OWLEntityDisplayProvider provider) {
        entityDisplayProviders.remove(provider);
    }

    @Override
    public void dispose() {
        // Save our workspace!
        super.dispose();

        owlComponentFactory.dispose();

        ProtegeApplication.getLogManager().removeErrorLogListener(errorLogListener);

        getOWLModelManager().removeListener(owlModelManagerListener);
        getOWLModelManager().removeOntologyChangeListener(listener);
    }

    private void rebuildOntologyDropDown() {
        try {
            TreeSet<OWLOntology> ts = new TreeSet<>(getOWLModelManager().getOWLObjectComparator());
            ts.addAll(getOWLModelManager().getOntologies());
            ontologiesList.setModel(new DefaultComboBoxModel<>(ts.toArray(new OWLOntology[0])));
            ontologiesList.setSelectedItem(getOWLModelManager().getActiveOntology());
        } catch (Exception e) {
            LOGGER.error("An error occurred whilst building the ontology list: '{}'", e.getMessage(), e);
        }
    }

    public OWLIconProvider getOWLIconProvider() {
        return iconProvider;
    }

    public OWLCellRenderer createOWLCellRenderer() {
        return createOWLCellRenderer(true, true);
    }

    public OWLCellRenderer createOWLCellRenderer(boolean renderExpression, boolean renderIcon) {
        return new OWLCellRenderer(getOWLEditorKit(), renderExpression, renderIcon);
    }

    public Map<String, Color> getKeyWordColorMap() {
        return keyWordColorMap;
    }

    public OWLSelectionModel getOWLSelectionModel() {
        return owlSelectionModel;
    }

    public OWLSelectionHistoryManager getOWLSelectionHistoryManager() {
        return owlSelectionHistoryManager;
    }

    public OWLEntityCreationSet<OWLClass> createOWLClass() {
        return OWLEntityCreationPanel.showDialog(getOWLEditorKit(), OWLClass.class);
    }

    public OWLEntityCreationSet<OWLObjectProperty> createOWLObjectProperty() {
        return OWLEntityCreationPanel.showDialog(getOWLEditorKit(), OWLObjectProperty.class);
    }

    public OWLEntityCreationSet<OWLDataProperty> createOWLDataProperty() {
        return OWLEntityCreationPanel.showDialog(getOWLEditorKit(), OWLDataProperty.class);
    }

    public OWLEntityCreationSet<OWLAnnotationProperty> createOWLAnnotationProperty() {
        return OWLEntityCreationPanel.showDialog(getOWLEditorKit(), OWLAnnotationProperty.class);
    }

    public OWLEntityCreationSet<OWLNamedIndividual> createOWLIndividual() {
        return OWLEntityCreationPanel.showDialog(getOWLEditorKit(), OWLNamedIndividual.class);
    }

    public OWLEntityCreationSet<OWLDatatype> createOWLDatatype() {
        return OWLEntityCreationPanel.showDialog(getOWLEditorKit(), OWLDatatype.class);
    }

    public OWLComponentFactory getOWLComponentFactory() {
        if (owlComponentFactory == null) {
            throw new IllegalStateException("The OWL Workspace has not been initialised. " +
                    "OWLWorkspace.getOWLComponentFactory must not be called until the workspace has been initialised.");
        }
        return owlComponentFactory;
    }

    @Override
    public boolean sendErrorReport(ErrorLog errorLog) {
        return true;
    }

    @Override
    public Optional<JComponent> getStatusArea() {
        return Optional.of(statusArea);
    }

    @Override
    public WorkspaceTab createWorkspaceTab(final String name) {
        OWLWorkspaceViewsTab tab = new OWLWorkspaceViewsTab();
        tab.setup(new WorkspaceTabPlugin() {
            @Override
            public TabbedWorkspace getWorkspace() {
                return OWLWorkspace.this;
            }

            @Override
            public String getLabel() {
                return name;
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public String getIndex() {
                return "Z";
            }

            @Override
            public URL getDefaultViewConfigFile() {
                try {
                    return new File(getId() + "-config.xml").toURI().toURL();
                } catch (MalformedURLException uriex) {
                    LOGGER.warn("The default view configuration file is malformed: '{}'", uriex.getMessage());
                }
                return null;
            }

            @Override
            public String getId() {
                return "WorkspaceTab" + System.nanoTime();
            }

            @Override
            public String getDocumentation() {
                return null;
            }

            @Override
            public WorkspaceTab newInstance() {
                return tab;
            }
        });

        return tab;
    }

    private static class ExtraReasonerMenuActionPlugin extends ProtegeActionPluginJPFImpl {

        protected ExtraReasonerMenuActionPlugin(EditorKit editorKit, IExtension extension) {
            super(editorKit, extension);
        }

        public String getAccelerator() {
            return PluginUtilities.getAttribute(getIExtension(), "accelerator");
        }
    }

    private class ExtraReasonerMenuActionPluginLoader extends AbstractPluginLoader<ExtraReasonerMenuActionPlugin> {

        /**
         * Creates a loader that will load (a subset) of the plugins
         * that extend the specified plugin at the specified plugin extension point.
         */
        public ExtraReasonerMenuActionPluginLoader() {
            super(ProtegeOWL.ID, "ExtraReasonerMenuAction");
        }

        /**
         * This method needs to be overridden to create an instance of the desired plugin,
         * based on the plugin <code>Extension</code>
         *
         * @param extension the <code>Extension</code> that describes the Java Plugin Framework extension
         * @return a plugin object (typically some sort of wrapper around the extension)
         */
        @Override
        protected ExtraReasonerMenuActionPlugin createInstance(IExtension extension) {
            return new ExtraReasonerMenuActionPlugin(getOWLEditorKit(), extension);
        }
    }
}
