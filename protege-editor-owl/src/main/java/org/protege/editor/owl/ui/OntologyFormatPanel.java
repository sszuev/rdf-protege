package org.protege.editor.owl.ui;

import com.github.owlcs.ontapi.OntFormat;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormat;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 13-Sep-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OntologyFormatPanel extends JPanel {

    private JComboBox<OWLDocumentFormat> formatComboBox;
    private JLabel messageLabel;

    public OntologyFormatPanel() {
        OWLDocumentFormat[] formats = outputFormats()
                .map(OntFormat::createOwlFormat).toArray(OWLDocumentFormat[]::new);
        formatComboBox = new JComboBox<>(formats);
        setLayout(new BorderLayout(12, 12));
        add(formatComboBox, BorderLayout.SOUTH);
        formatComboBox.setSelectedItem(formats[0]);
    }

    /**
     * Lists all formats that support serialization.
     * Some formats could only be used for reading, some - for writing, some are suitable for both operations.
     * This method applies only about the second category - it returns writable formats only.
     *
     * @return a {@code Stream} of {@link OntFormat}s
     */
    public static Stream<OntFormat> outputFormats() {
        return OntFormat.formats().filter(OntFormat::isWriteSupported);
    }

    public static Optional<OWLDocumentFormat> showDialog(OWLEditorKit editorKit,
                                                         OWLDocumentFormat defaultFormat,
                                                         String message) {
        OntologyFormatPanel panel = new OntologyFormatPanel();
        if (message != null) {
            panel.setMessage(message);
        }
        panel.setSelectedFormat(defaultFormat);
        OWLDocumentFormat selectedFormat;
        do {
            int res = JOptionPane.showConfirmDialog(editorKit.getWorkspace(), panel, "Select an ontology format",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) {
                return Optional.empty();
            }
            selectedFormat = panel.getSelectedFormat();
        } while (!isFormatOk(editorKit, selectedFormat));
        return Optional.of(selectedFormat);
    }

    private static boolean isFormatOk(OWLEditorKit editorKit, OWLDocumentFormat format) {
        if (!(format instanceof ManchesterSyntaxDocumentFormat)) {
            return true;
        }
        int userSaysOk = JOptionPane.showConfirmDialog(editorKit.getOWLWorkspace(),
                "The Manchester OWL Syntax can lose information such as GCI's and annotations of undeclared entities. " +
                        "Continue?", "Warning", JOptionPane.YES_NO_OPTION);
        return userSaysOk == JOptionPane.YES_OPTION;
    }

    public void setMessage(String message) {
        if (messageLabel == null) {
            messageLabel = new JLabel(message);
            add(messageLabel, BorderLayout.NORTH);
        } else {
            messageLabel.setText(message);
        }
        revalidate();
    }

    public OWLDocumentFormat getSelectedFormat() {
        return (OWLDocumentFormat) formatComboBox.getSelectedItem();
    }

    public void setSelectedFormat(OWLDocumentFormat format) {
        if (format == null) {
            formatComboBox.setSelectedIndex(0);
        }
        for (int i = 0; i < formatComboBox.getModel().getSize(); i++) {
            if (!formatComboBox.getModel().getElementAt(i).equals(format)) {
                continue;
            }
            formatComboBox.setSelectedIndex(i);
            return;
        }
    }
}
