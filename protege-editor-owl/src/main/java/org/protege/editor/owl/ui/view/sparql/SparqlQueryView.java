package org.protege.editor.owl.ui.view.sparql;

import com.github.owlcs.ontapi.Ontology;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.table.BasicOWLTable;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * A {@code ViewComponent} for executing SPARQL.
 * Created by @ssz on 07.01.2020.
 *
 * @see <a href='https://github.com/protegeproject/sparql-query-plugin/blob/master/src/main/java/org/protege/editor/owl/rdf/SparqlQueryView.java'>org.protege.editor.owl.rdf.SparqlQueryView</a>
 */
public class SparqlQueryView extends AbstractOWLViewComponent {

    private final SPARQLEngine.Factory factory = new SPARQLEngine.Factory();
    private final JTextPane queryPane = new JTextPane();
    private final ResultModel resultModel = new ResultModel();

    private SPARQLEngine.Type type = SPARQLEngine.Type.SELECT;

    @Override
    protected void initialiseOWLView() {
        setLayout(new BorderLayout());
        add(createNorthComponent(), BorderLayout.NORTH);
        add(createCenterComponent(), BorderLayout.CENTER);
        add(createBottomComponent(), BorderLayout.SOUTH);
    }

    private JComponent createNorthComponent() {
        JComboBox<SPARQLEngine.Type> s = new JComboBox<>(SPARQLEngine.Type.values());
        s.setSelectedIndex(0);
        s.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                type = (SPARQLEngine.Type) Objects.requireNonNull(s.getSelectedItem());
                queryPane.setText(type.getSampleQuery());
            }
        });
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(s, BorderLayout.WEST);
        return panel;
    }

    private JComponent createCenterComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        queryPane.setText(type.getSampleQuery());
        panel.add(new JScrollPane(queryPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        BasicOWLTable results = new BasicOWLTable(resultModel) {
            @Override
            protected boolean isHeaderVisible() {
                return true;
            }
        };
        OWLCellRenderer renderer = new OWLCellRenderer(getOWLEditorKit());
        renderer.setWrap(false);
        results.setDefaultRenderer(Object.class, renderer);
        JScrollPane scrollableResults = new JScrollPane(results);
        panel.add(scrollableResults);
        return panel;
    }

    private JComponent createBottomComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton executeQuery = new JButton("Execute");
        executeQuery.addActionListener(e -> {
            try {
                Ontology o = (Ontology) getOWLModelManager().getActiveOntology();
                String query = queryPane.getText();
                SPARQLEngine.Res result = factory.create(type).executeQuery(o.asGraphModel(), query);
                resultModel.setResults(result);
            } catch (SPARQLEngine.Error ex) {
                ErrorLogPanel.showErrorDialog(ex);
                JOptionPane.showMessageDialog(getOWLWorkspace(),
                        ex.getMessage() + "\nSee the logs for more information.");
            }
        });
        panel.add(executeQuery);
        return panel;
    }

    @Override
    protected void disposeOWLView() {
    }

    /**
     * Created by @ssz on 07.01.2020.
     */
    public static class ResultModel extends AbstractTableModel {
        private static final long serialVersionUID = -1094080880127911408L;
        private SPARQLEngine.Res results = SPARQLEngine.Factory.EMPTY;

        @Override
        public int getRowCount() {
            return results.getRowCount();
        }

        @Override
        public int getColumnCount() {
            return results.getColumnCount();
        }

        @Override
        public String getColumnName(int column) {
            return results.getColumnName(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return results.getResult(rowIndex, columnIndex);
        }

        public void setResults(SPARQLEngine.Res results) {
            this.results = results;
            fireTableStructureChanged();
        }
    }
}
