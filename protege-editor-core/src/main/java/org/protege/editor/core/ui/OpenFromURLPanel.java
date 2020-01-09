package org.protege.editor.core.ui;

import org.protege.editor.core.BookMarkedURIManager;
import org.protege.editor.core.OWLSource;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.core.ui.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 12-May-2007<br><br>
 */
public class OpenFromURLPanel extends JPanel implements VerifiedInputEditor {

    private JTextField uriField;
    private MList bookmarksList;
    private LoadSettingsPanel settings;
    private List<InputVerificationStatusChangedListener> listeners = new ArrayList<>();

    public OpenFromURLPanel() {
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        uriField = new JTextField(45);
        uriField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                handleValueChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                handleValueChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                handleValueChanged();
            }
        });
        JPanel uriFieldHolder = new JPanel(new BorderLayout());
        uriFieldHolder.setBorder(ComponentFactory.createTitledBorder("URI"));
        uriFieldHolder.add(uriField, BorderLayout.NORTH);
        this.add(uriFieldHolder, BorderLayout.NORTH);
        JPanel bookmarksHolder = new JPanel(new BorderLayout());
        bookmarksHolder.setBorder(ComponentFactory.createTitledBorder("Bookmarks"));
        this.add(bookmarksHolder);
        bookmarksList = new MList() {
            @Override
            protected void handleAdd() {
                addURI();
            }

            @Override
            protected void handleDelete() {
                deleteSelectedBookmark();
            }
        };

        bookmarksList.setCellRenderer(new BookmarkedItemListRenderer());
        bookmarksHolder.add(new JScrollPane(bookmarksList));
        fillList();
        bookmarksList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateTextField();
            }
        });

        this.add(this.settings = new LoadSettingsPanel(false), BorderLayout.SOUTH);
    }

    private void handleValueChanged() {
        final boolean validURI = isValidURI();
        for (InputVerificationStatusChangedListener l : listeners){
            l.verifiedStatusChanged(validURI);
        }
    }

    protected boolean isValidURI(){
        URI uri = getURI(false);
        return uri != null && uri.isAbsolute() && uri.getScheme() != null;        
    }

    public URI getURI() {
        return getURI(true);
    }

    private URI getURI(boolean showMessage) {
        try {
            return new URI(uriField.getText().trim());
        } catch (URISyntaxException e) {
            if (showMessage) {
                showURIErrorMessage(e);
            }
        }
        return null;
    }

    private void updateTextField() {
        URIListItem item = getSelUriListItem();
        if (item != null) {
            uriField.setText(item.uri.toString());
        }
    }

    private void addURI() {
        String uriString = JOptionPane.showInputDialog(this, "Please enter a URI", "URI", JOptionPane.PLAIN_MESSAGE);
        if (uriString == null) {
            return;
        }
        try {
            URI uri = new URI(uriString);
            BookMarkedURIManager.getInstance().add(uri);
        } catch (URISyntaxException e) {
            showURIErrorMessage(e);
        }
        fillList();
    }

    private void showURIErrorMessage(URISyntaxException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid URI", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    private void fillList() {
        BookMarkedURIManager man = BookMarkedURIManager.getInstance();
        Set<URI> ts = new TreeSet<>(man.getBookMarkedURIs());
        ArrayList<Object> data = new ArrayList<>();

        data.add(new AddURIItem());
        for (URI uri : ts) {
            data.add(new URIListItem(uri));
        }
        bookmarksList.setListData(data.toArray());
    }

    private void deleteSelectedBookmark() {
        Object selObj = bookmarksList.getSelectedValue();
        if (!(selObj instanceof URIListItem)) {
            return;
        }
        URIListItem item = (URIListItem) selObj;
        BookMarkedURIManager.getInstance().remove(item.uri);
        fillList();
    }

    private URIListItem getSelUriListItem() {
        if (bookmarksList.getSelectedValue() instanceof URIListItem) {
            return (URIListItem) bookmarksList.getSelectedValue();
        }
        return null;
    }

    @Override
    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.add(listener);
        listener.verifiedStatusChanged(isValidURI());
    }

    @Override
    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.remove(listener);
    }

    private static class BookmarkedItemListRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = -833970269120392171L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof URIListItem) {
                URIListItem item = (URIListItem) value;
                label.setText(item.uri.toString());
            }
            return label;
        }
    }


    private static class AddURIItem implements MListSectionHeader {

        @Override
        public String getName() {
            return "Bookmarked URIs";
        }

        @Override
        public boolean canAdd() {
            return true;
        }
    }


    private static class URIListItem implements MListItem {

        private URI uri;

        public URIListItem(URI uri) {
            this.uri = uri;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public void handleEdit() {
        }

        @Override
        public boolean isDeleteable() {
            return true;
        }

        @Override
        public boolean handleDelete() {
            return true;
        }

        @Override
        public String getTooltip() {
            return uri.toString();
        }
    }

    public static OWLSource showDialog() {
        OpenFromURLPanel panel = new OpenFromURLPanel();
        int res = JOptionPaneEx.showValidatingConfirmDialog(null, "Enter or select a URI", panel,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.uriField);
        Map<String, Object> props = panel.settings.getProperties();
        URI uri = res == JOptionPane.OK_OPTION ? panel.getURI() : null;
        return OWLSource.create(uri, props);
    }
}
