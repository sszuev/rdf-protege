package org.protege.editor.core.ui.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 28-Feb-2007<br><br>
 */
public class ErrorLogPanel extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorLogPanel.class);

    /**
     * Shows a local error dialog for displaying one exception
     *
     * @param throwable The exception to be displayed.
     */
    public static void showErrorDialog(Throwable throwable) {
        LOGGER.error("An error was thrown: {}", throwable.getMessage(), throwable);
        JTextArea textPane = new JTextArea(15, 80);
        textPane.setLineWrap(false);
        textPane.setEditable(false);
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        textPane.setText(sw.toString());
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane sp = new JScrollPane(textPane);
        sp.setPreferredSize(new Dimension(800, 300));
        JOptionPane op = new JOptionPane(sp, JOptionPane.ERROR_MESSAGE);
        JDialog dlg = op.createDialog(null, "An error has occurred");
        dlg.setResizable(true);
        dlg.setVisible(true);
    }

}
