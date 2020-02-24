package org.protege.editor.core.ui.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
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
     * Shows a local error dialog for displaying one exception.
     *
     * @param throwable the exception to be displayed
     */
    public static void showErrorDialog(Throwable throwable) {
        showMessageDialog(Level.ERROR, throwable);
    }

    /**
     * Shows a local warn dialog for displaying one exception,
     * which is not critical and may mean incorrect user input, for example.
     *
     * @param throwable the exception to be displayed
     */
    public static void showWarnDialog(Throwable throwable) {
        showMessageDialog(Level.WARN, throwable);
    }

    public static void showMessageDialog(Level level, Object message) {
        level.log(message);
        JTextArea area = new JTextArea(15, 80);
        area.setLineWrap(false);
        area.setEditable(false);
        level.addText(area, message);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(800, 300));
        JOptionPane pane = level.createPane(sp);
        JDialog res = pane.createDialog(null, level.getTitle());
        res.setResizable(true);
        res.setVisible(true);
    }

    /**
     * Describes the text-box's message levels.
     */
    public enum Level {
        ERROR {
            @Override
            public JOptionPane createPane(Object m) {
                return new JOptionPane(m, JOptionPane.ERROR_MESSAGE);
            }

            @Override
            void doLog(Object... args) {
                LOGGER.error(txt, args);
            }
        },
        WARN {
            @Override
            public JOptionPane createPane(Object m) {
                return new JOptionPane(m, JOptionPane.WARNING_MESSAGE);
            }

            @Override
            void doLog(Object... args) {
                LOGGER.warn(txt, args);
            }
        },
        ;
        static final String txt = "An error was thrown: {}";

        public void log(Object message) {
            if (message instanceof Throwable) {
                Throwable t = (Throwable) message;
                doLog(t.getMessage(), t);
            } else {
                doLog(message);
            }
        }

        public void addText(JTextComponent area, Object message) {
            area.setText(toString(message));
        }

        private static String toString(Object message) {
            if (message instanceof Throwable) {
                Throwable t = (Throwable) message;
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                return sw.toString();
            }
            return String.valueOf(message);
        }

        public String getTitle() {
            return "An error has occurred";
        }

        public abstract JOptionPane createPane(Object m);

        abstract void doLog(Object... args);
    }
}
