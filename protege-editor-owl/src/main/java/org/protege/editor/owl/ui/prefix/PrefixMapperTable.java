package org.protege.editor.owl.ui.prefix;

import org.protege.editor.owl.ui.table.BasicOWLTable;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;

import javax.swing.event.ChangeEvent;

import static org.protege.editor.owl.ui.prefix.PrefixMapperTableModel.Column;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 22-Sep-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class PrefixMapperTable extends BasicOWLTable {
    private static final long serialVersionUID = 7960180034430124925L;

    public PrefixMapperTable(PrefixDocumentFormat prefixManager) {
        super(new PrefixMapperTableModel(prefixManager));
        setShowGrid(true);
        setRowHeight(getRowHeight() + 3);
        getColumnModel().getColumn(0).setPreferredWidth(150);
        getColumnModel().getColumn(1).setPreferredWidth(600);
        getColumnModel().getColumn(0).setCellEditor(new PrefixTableCellEditor());
        getColumnModel().getColumn(1).setCellEditor(new PrefixTableCellEditor());
    }

    public void createAndEditRow() {
        PrefixMapperTableModel model = getModel();
        int index;
        for (int i = 0; true; i++) {
            String candidatePrefix = "p" + i;
            if (model.getIndexOfPrefix(candidatePrefix) < 0) {
                index = model.addMapping(candidatePrefix, "");
                break;
            }
	    }
    	setRowSelectionInterval(index, index);
        editCellAt(index, Column.PREFIX.ordinal());
    }

    @Override
    protected boolean isHeaderVisible() {
        return true;
    }

    @Override
    public PrefixMapperTableModel getModel() {
        return (PrefixMapperTableModel) super.getModel();
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        int editingColumn = getEditingColumn();
        String cellValue = (String) getCellEditor().getCellEditorValue();
        super.editingStopped(e);
        if (editingColumn != Column.PREFIX_NAME.ordinal()) {
            return;
        }
        int newRow = getModel().getIndexOfPrefix(cellValue);
        if (newRow < 0) {
            return;
        }
        setRowSelectionInterval(newRow, newRow);
        editCellAt(newRow, Column.PREFIX.ordinal());
        requestFocus();
    }
}
