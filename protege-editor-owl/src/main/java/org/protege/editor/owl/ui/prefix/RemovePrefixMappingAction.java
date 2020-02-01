package org.protege.editor.owl.ui.prefix;

import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.prefix.PrefixMapperTables.SelectedOntologyListener;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 22-Sep-2006<br><br>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class RemovePrefixMappingAction extends AbstractAction {
    private static final long serialVersionUID = 5507854297099664212L;

    private final PrefixMapperTables tables;
    private final ListSelectionListener tableSelectionListener = e -> updateEnabled();
    private PrefixMapperTable table;

    public RemovePrefixMappingAction(PrefixMapperTables tables) {
        super("Remove prefix", OWLIcons.getIcon("prefix.remove.png"));
        this.tables = tables;
        table = tables.getPrefixMapperTable();
        table.getSelectionModel().addListSelectionListener(tableSelectionListener);
        updateEnabled();
        SelectedOntologyListener ontologySelectionListener = () -> {
            updateEnabled();
            if (table != null) {
                table.getSelectionModel().removeListSelectionListener(tableSelectionListener);
            }
            table = tables.getPrefixMapperTable();
            if (table != null) {
                table.getSelectionModel().addListSelectionListener(tableSelectionListener);
            }
        };
        tables.addListener(ontologySelectionListener);
    }

    private void updateEnabled() {
    	PrefixMapperTable table = tables.getPrefixMapperTable();
    	if (table == null) {
    		setEnabled(false);
    		return;
        }
        int row = table.getSelectedRow();
        if (row == -1) {
            setEnabled(false);
            return;
        }
        String prefix = (String) table.getModel().getValueAt(row, 0);
        setEnabled(!PrefixUtilities.isStandardPrefix(prefix));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PrefixMapperTable table = tables.getPrefixMapperTable();
        int[] selIndexes = table.getSelectedRows();
        List<String> prefixesToRemove = new ArrayList<>();
        for (int selIndex : selIndexes) {
            prefixesToRemove.add(table.getModel().getValueAt(selIndex, 0).toString());
        }
        for (String prefix : prefixesToRemove) {
            table.getModel().removeMapping(prefix);
        }
    }
}
