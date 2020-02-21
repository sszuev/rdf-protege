package org.protege.editor.owl.ui.rename;

import org.protege.editor.core.ui.util.CheckTable;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 1, 2008<br><br>
 */
public class RenameEntitiesPanel extends JPanel implements VerifiedInputEditor {
    private static final long serialVersionUID = 259808389697631045L;
    // editor pause
    private static final int SEARCH_PAUSE_MILLIS = 1000;

    private final OWLEditorKit eKit;
    private final JComboBox<Object> replaceWithCombo;
    private final JComboBox<Object> findCombo;
    private final CheckTable<OWLEntity> list;

    private final Map<String, Set<OWLEntity>> nsMap = new HashMap<>();
    private final Map<OWLEntity, IRI> entity2IRIMap = new HashMap<>();
    private final Map<OWLEntity, String> errorMap = new HashMap<>();
    private final List<InputVerificationStatusChangedListener> statusListeners = new ArrayList<>();

    private boolean currentStatus = false;
    private Thread reloadThread;
    private final Runnable reloadProcess = new Runnable() {
        @Override
        public void run() {
            reloadEntityList();
            reloadThread = null;
        }
    };

    public RenameEntitiesPanel(OWLEditorKit eKit) {
        setLayout(new BorderLayout(6, 6));
        this.eKit = eKit;

        buildEntityNamespaceMap();

        JComponent subPanel = new JPanel();
        subPanel.setBorder(new TitledBorder("Find & Replace"));
        subPanel.setLayout(new BorderLayout());

        ItemListener findListener = event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                reloadEntityListThreaded();
            }
        };
        findCombo = createCombo("Find", findListener, subPanel, BorderLayout.NORTH);

        ItemListener replaceListener = event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                updateEntityMap();
                handleStateChanged();
            }
        };
        replaceWithCombo = createCombo("Replace with", replaceListener, subPanel, BorderLayout.SOUTH);

        add(subPanel, BorderLayout.NORTH);

        list = new CheckTable<>("Matching entities");
        list.checkAll(true);
        list.setDefaultRenderer(new ResultCellRenderer(eKit));
        ListSelectionListener listSelListener = event -> handleStateChanged();
        list.addCheckSelectionListener(listSelListener);

        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    private JComboBox<Object> createCombo(String title,
                                          ItemListener listener,
                                          JComponent parent,
                                          String constraints) {
        final JComboBox<Object> combo = new JComboBox<>(nsMap.keySet().toArray());
        combo.addItem("");
        combo.setSelectedItem("");
        combo.setEditable(true);
        combo.addItemListener(listener);

        final JTextComponent editor = (JTextComponent) combo.getEditor().getEditorComponent();

        final ActionListener actionListener = actionEvent -> combo.setSelectedItem(editor.getText());

        final Timer timer = new Timer(SEARCH_PAUSE_MILLIS, actionListener);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                timer.restart();
            }

            public void removeUpdate(DocumentEvent event) {
                timer.restart();
            }

            public void changedUpdate(DocumentEvent event) {
            }
        });
        JComponent panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(new JLabel(title));
        panel.add(combo);
        parent.add(panel, constraints);
        return combo;
    }

    private void buildEntityNamespaceMap() {
        getOntologies().stream().flatMap(HasSignature::signature).forEach(this::extractNSFromEntity);
    }

    private void extractNSFromEntity(OWLEntity entity) {
        String ns = getBase(entity.getIRI());
        Set<OWLEntity> matchingEntities = nsMap.get(ns);
        if (matchingEntities == null) {
            matchingEntities = new HashSet<>();
        }
        matchingEntities.add(entity);
        nsMap.put(ns, matchingEntities);
    }

    public String getFindValue() {
        return (String) findCombo.getSelectedItem();
    }

    public String getReplaceWithValue() {
        return (String) replaceWithCombo.getSelectedItem();
    }

    public List<OWLEntity> getSelectedEntities() {
        return list.getFilteredValues();
    }

    public List<OWLOntologyChange> getChanges() {
        OWLOntologyManager mngr = eKit.getModelManager().getOWLOntologyManager();
        OWLEntityRenamer renamer = new OWLEntityRenamer(mngr, getOntologies());
        Map<OWLEntity, IRI> filteredIRIMap = new HashMap<>();
        for (OWLEntity e : list.getFilteredValues()) {
            filteredIRIMap.put(e, entity2IRIMap.get(e));
        }
        return renamer.changeIRI(filteredIRIMap);
    }

    private void reloadEntityList() {
        List<OWLEntity> entities = entities().distinct()
                .sorted(eKit.getModelManager().getOWLObjectComparator())
                .collect(Collectors.toList());
        list.getModel().setData(entities, true);
        updateEntityMap();
        handleStateChanged();
    }

    /**
     * The getEntities() and the updateEntityMap should be consistent with one another.
     * Currently both assume that the user is changing a namespace prefix
     * if the findValue is found in the namespace map but is using a regular expression otherwise.
     * <p>
     * The other assumption that we will make is that the entity2IRIMap is unfiltered by the changes list.
     *
     * @return {@code Stream}
     */
    private Stream<OWLEntity> entities() {
        Set<OWLEntity> res = nsMap.get(getFindValue());
        if (res != null) {
            return res.stream();
        }
        String matching = ".*" + getFindValue() + ".*";
        Pattern p = Pattern.compile(matching);
        return getOntologies().stream().flatMap(HasSignature::signature)
                .filter(e -> p.matcher(e.getIRI().toString()).matches());
    }

    private void updateEntityMap() {
        entity2IRIMap.clear();
        errorMap.clear();
        Set<OWLEntity> matches = nsMap.get(getFindValue());
        if (matches != null) {
            updateEntityMapUsingPrefixes(getFindValue(), matches);
        } else {
            updateEntityMapUsingRegexp();
        }
        list.repaint();
    }

    private void updateEntityMapUsingPrefixes(String prefix, Set<OWLEntity> matches) {
        int prefixLength = prefix.length();
        String replacementText = getReplaceWithValue();
        for (OWLEntity entity : matches) {
            String iriString = entity.getIRI().toString();
            addToEntityMap(entity, replacementText + iriString.substring(prefixLength));
        }
    }

    private void updateEntityMapUsingRegexp() {
        String find = getFindValue();
        String replace = getReplaceWithValue();
        getOntologies().stream()
                .flatMap(HasSignature::signature)
                .distinct().forEach(entity -> {
            String newURIStr = entity.getIRI().toString().replaceAll(find, replace);
            addToEntityMap(entity, newURIStr);
        });
    }

    private void addToEntityMap(OWLEntity entity, String newURIStr) {
        try {
            URI newURI = new URI(newURIStr);
            if (!newURI.isAbsolute()) {
                throw new URISyntaxException(newURIStr, "IRI must be absolute");
            }
            entity2IRIMap.put(entity, IRI.create(newURI));
        } catch (URISyntaxException e) {
            errorMap.put(entity, newURIStr);
        }
    }

    private Set<OWLOntology> getOntologies() {
        return eKit.getModelManager().getOntologies();
    }

    private String getBase(IRI uri) {
        String frag = getShortForm(uri);
        final String uriStr = uri.toString();
        return uriStr.substring(0, uriStr.lastIndexOf(frag));
    }

    private String getShortForm(IRI uri) {
        try {
            Optional<String> rendering = uri.getRemainder();
            if (!rendering.isPresent()) {
                // Get last bit of path
                String path = uri.toURI().getPath();
                if (path == null) {
                    return uri.toString();
                }
                return uri.toURI().getPath().substring(path.lastIndexOf("/") + 1);
            }
            return rendering.get();
        } catch (Exception e) {
            return "<Error! " + e.getMessage() + ">";
        }
    }

    @Override
    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        statusListeners.add(listener);
        listener.verifiedStatusChanged(currentStatus);
    }

    @Override
    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        statusListeners.remove(listener);
    }

    private void handleStateChanged() {
        boolean valid = getStatus();
        if (currentStatus == valid) {
            return;
        }
        currentStatus = valid;
        for (InputVerificationStatusChangedListener l : statusListeners) {
            l.verifiedStatusChanged(currentStatus);
        }
    }

    private boolean getStatus() {
        return findCombo.getSelectedItem() != null && !findCombo.getSelectedItem().equals("") &&
                replaceWithCombo.getSelectedItem() != null && !replaceWithCombo.getSelectedItem().equals("") &&
                !list.getFilteredValues().isEmpty() && errorMap.isEmpty();
    }

    @SuppressWarnings("CallToThreadRun")
    private void reloadEntityListThreaded() {
        if (reloadThread != null && reloadThread.isAlive()) {
            reloadThread.interrupt();
        }
        reloadThread = new Thread(reloadProcess);
        reloadThread.run();
    }

    public JComponent getFocusComponent() {
        return (JComponent) findCombo.getEditor().getEditorComponent();
    }

    /**
     * Should highlight the matching text in the URI
     */
    class ResultCellRenderer extends OWLCellRenderer {

        public ResultCellRenderer(OWLEditorKit owlEditorKit) {
            super(owlEditorKit);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            setStrikeThrough(value instanceof OWLEntity && errorMap.containsKey(value));
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        @Override
        protected String getRendering(Object object) {
            if (object instanceof OWLEntity) {
                return super.getRendering(object) + " (" + ((OWLEntity) object).getIRI() + ")";
            }
            return super.getRendering(object);
        }
    }
}
