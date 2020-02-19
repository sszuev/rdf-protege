package org.protege.editor.owl.ui.ontology.imports;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.ontology.imports.wizard.OntologyImportWizard;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: May 28, 2009<br><br>
 */
public class OntologyImportsList extends MList<Object> {

    private final OWLEditorKit editorKit;
    private final MListSectionHeader directImportsHeader;
    private final MListSectionHeader indirectImportsHeader;
    private final OWLOntologyChangeListener ontChangeListener = this::handleOntologyChanges;

    private OWLOntology ont;

    public OntologyImportsList(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
        setFixedCellHeight(-1);
        uncheckedSetCellRenderer(new OntologyImportsItemRenderer(editorKit));

        directImportsHeader = new MListSectionHeader() {
            public String getName() {
                return "Direct Imports";
            }

            public boolean canAdd() {
                return true;
            }
        };

        indirectImportsHeader = new MListSectionHeader() {
            public String getName() {
                return "Indirect Imports";
            }

            public boolean canAdd() {
                return false;
            }
        };

        editorKit.getOWLModelManager().addOntologyChangeListener(ontChangeListener);
    }

    @Override
    protected void handleAdd() {
        // don't need to check the section as only the direct imports can be added
        OntologyImportWizard wizard = new OntologyImportWizard((Frame) SwingUtilities.getAncestorOfClass(Frame.class,
                editorKit.getWorkspace()), editorKit);
        int ret = wizard.showModalDialog();

        if (ret == Wizard.FINISH_RETURN_CODE) {
            AddImportsStrategy strategy = new AddImportsStrategy(editorKit, ont, wizard.getImports());
            strategy.addImports();
        }
    }

    public void setOntology(OWLOntology ont) {
        this.ont = ont;
        List<Object> data = new ArrayList<>();
        data.add(directImportsHeader);

        // @@TODO ordering
        ont.importsDeclarations().map(x -> new OntologyImportItem(ont, x, editorKit)).forEach(data::add);
        data.add(indirectImportsHeader);
        // @@TODO ordering
        try {
            editorKit.getOWLModelManager().getOWLOntologyManager().importsClosure(ont)
                    .filter(o -> !o.equals(ont))
                    .forEach(o -> o.importsDeclarations()
                            .filter(dec -> !data.contains(dec))
                            .forEach(dec -> data.add(new OntologyImportItem(o, dec, editorKit))));
        } catch (UnknownOWLOntologyException e) {
            throw new OWLRuntimeException(e);
        }
        setListData(data.toArray());
    }

    private void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            if (!(change instanceof AddImport) && !(change instanceof RemoveImport)) {
                continue;
            }
            if (!change.getOntology().equals(ont)) {
                continue;
            }
            refresh();
            return;
        }
    }

    private void refresh() {
        setOntology(ont);
    }

    public void dispose() {
        editorKit.getOWLModelManager().removeOntologyChangeListener(ontChangeListener);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }
}
