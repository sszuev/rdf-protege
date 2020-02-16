package org.protege.editor.owl.ui.ontology.imports.wizard.page;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.protege.editor.owl.ui.ontology.imports.wizard.ImportInfo;
import org.protege.editor.owl.ui.ontology.imports.wizard.OntologyImportWizard;
import org.protege.editor.owl.ui.renderer.OWLOntologyCellRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Sep-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class LoadedOntologyPage extends OntologyImportPage {
    private static final long serialVersionUID = 7719702648603699776L;
    public static final String ID = LoadedOntologyPage.class.getName();

    private JList<OWLOntology> ontologyList;

    public LoadedOntologyPage(OWLEditorKit owlEditorKit) {
        super(ID, "Import pre-loaded ontology", owlEditorKit);
    }

    private List<OWLOntology> getOntologies() {
        final OWLModelManager mngr = getOWLModelManager();

        Set<OWLOntology> set = mngr.getOWLOntologyManager()
                .importsClosure(mngr.getActiveOntology()).collect(Collectors.toSet());
        List<OWLOntology> res = mngr.getOntologies().stream()
                .filter(x -> !set.contains(x)).collect(Collectors.toList());

        // you cannot import an ontology from the same series
        res.removeAll(getOntologiesInSeries(mngr.getActiveOntology(), res));

        res.sort(mngr.getOWLObjectComparator());
        return res;
    }

    private Set<OWLOntology> getOntologiesInSeries(OWLOntology ontology, Collection<OWLOntology> ontologies) {
        return getOntologiesInSeries(ontology.getOntologyID(), ontologies);
    }

    private Set<OWLOntology> getOntologiesInSeries(OWLOntologyID other, Collection<OWLOntology> ontologies) {
        Set<OWLOntology> res = new HashSet<>();
        if (other.isAnonymous()) {
            return res;
        }
        for (OWLOntology ont : ontologies) {
            OWLOntologyID id = ont.getOntologyID();
            if (id.isAnonymous() || !id.getOntologyIRI().equals(other.getOntologyIRI())) {
                continue;
            }
            res.add(ont);
        }
        return res;
    }

    @Override
    public void aboutToHidePanel() {
        OntologyImportWizard wizard = getWizard();
        wizard.setImportsAreFinal(false);
        wizard.clearImports();
        for (OWLOntology ontology : ontologyList.getSelectedValuesList()) {
            OWLOntologyID id = ontology.getOntologyID();
            IRI physicalLocation = getOWLModelManager().getOWLOntologyManager().getOntologyDocumentIRI(ontology);
            IRI iri = id.isAnonymous() ? physicalLocation :
                    id.getDefaultDocumentIRI().orElseThrow(IllegalStateException::new);

            ImportInfo parameter = new ImportInfo();
            parameter.setOntologyID(ontology.getOntologyID());
            parameter.setPhysicalLocation(physicalLocation.toURI());
            parameter.setImportLocation(iri);
            wizard.addImport(parameter);
        }
        ((SelectImportLocationPage) getWizardModel().getPanel(SelectImportLocationPage.ID)).setBackPanelDescriptor(ID);
        ((ImportConfirmationPage) getWizardModel().getPanel(ImportConfirmationPage.ID)).setBackPanelDescriptor(ID);
        super.aboutToHidePanel();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void createUI(JComponent parent) {
        setInstructions("Please select an existing (pre-loaded) ontology that you want to import.");
        ontologyList = new OWLObjectList<>(getOWLEditorKit());
        ontologyList.setCellRenderer(new OWLOntologyCellRenderer(getOWLEditorKit()));
        ontologyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateState();
            }
        });
        parent.setLayout(new BorderLayout());
        parent.add(ComponentFactory.createScrollPane(ontologyList), BorderLayout.CENTER);
        parent.add(createCustomizedImportsComponent(), BorderLayout.SOUTH);
    }

    private void fillList() {
        ontologyList.setListData(getOntologies().toArray(new OWLOntology[0]));
    }

    @Override
    public Object getNextPanelDescriptor() {
        return getWizard().isCustomizeImports() ? SelectImportLocationPage.ID : ImportConfirmationPage.ID;
    }

    @Override
    public Object getBackPanelDescriptor() {
        return ImportTypePage.ID;
    }

    @Override
    public void displayingPanel() {
        fillList();
        updateState();
        ontologyList.requestFocus();
    }

    private void updateState() {
        getWizard().setNextFinishButtonEnabled(ontologyList.getSelectedValue() != null);
    }
}

