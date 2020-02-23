package org.protege.editor.owl.ui.view.datatype;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.protege.editor.owl.model.util.OWLEntityDeleter;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.protege.editor.owl.ui.renderer.AddEntityIcon;
import org.protege.editor.owl.ui.renderer.DeleteEntityIcon;
import org.protege.editor.owl.ui.renderer.OWLDatatypeIcon;
import org.protege.editor.owl.ui.renderer.OWLEntityIcon;
import org.protege.editor.owl.ui.view.ChangeListenerMediator;
import org.protege.editor.owl.ui.view.Findable;
import org.protege.editor.owl.ui.view.OWLSelectionViewAction;
import org.semanticweb.owlapi.model.*;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 5, 2009<br><br>
 */
public class OWLDataTypeViewComponent extends AbstractOWLDataTypeViewComponent
        implements Findable<OWLDatatype>, SelectionDriver {

    private OWLObjectList<OWLDatatype> list;
    private ChangeListenerMediator changeListenerMediator;

    private final ListSelectionListener selListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            if (list.getSelectedValue() != null) {
                setGlobalSelection(list.getSelectedValue());
            }
            changeListenerMediator.fireStateChanged(OWLDataTypeViewComponent.this);
        }
    };

    private final OWLOntologyChangeListener ontChangeListener = this::handleChanges;

    private final OWLModelManagerListener modelManagerListener = event -> {
        if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
            reload();
        }
    };

    @Override
    public void initialiseView() throws Exception {
        setLayout(new BorderLayout());

        changeListenerMediator = new ChangeListenerMediator();

        list = new OWLObjectList<>(getOWLEditorKit());
        list.addListSelectionListener(selListener);

        reload();

        setupActions();

        getOWLModelManager().addOntologyChangeListener(ontChangeListener);
        getOWLModelManager().addListener(modelManagerListener);

        add(ComponentFactory.createScrollPane(list));
    }

    private void setupActions() {
        DisposableAction addDatatypeAction = new DisposableAction("Add datatype", new AddEntityIcon(new OWLDatatypeIcon())) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewDatatype();
            }

            @Override
            public void dispose() {
                // do nothing
            }
        };

        OWLSelectionViewAction deleteDatatypeAction = new OWLSelectionViewAction("Delete datatype",
                new DeleteEntityIcon(new OWLDatatypeIcon(OWLEntityIcon.FillType.HOLLOW))) {


            @Override
            public void actionPerformed(ActionEvent event) {
                deleteDatatype();
            }

            @Override
            public void updateState() {
                // @@TODO should check if this is a built in datatype
                setEnabled(list.getSelectedIndex() != -1);
            }

            @Override
            public void dispose() {
                // do nothing
            }
        };

        addAction(addDatatypeAction, "A", "A");
        addAction(deleteDatatypeAction, "B", "A");
    }

    private void deleteDatatype() {
        OWLEntityDeleter.deleteEntities(list.getSelectedOWLObjects(), getOWLModelManager());
    }

    private void createNewDatatype() {
        OWLEntityCreationSet<OWLDatatype> set = getOWLWorkspace().createOWLDatatype();
        if (set == null) {
            return;
        }
        getOWLModelManager().applyChanges(set.getOntologyChanges());
        OWLDatatype datatype = set.getOWLEntity();
        if (datatype != null) {
            list.setSelectedValue(datatype, true);
        }
    }

    private void handleChanges(List<? extends OWLOntologyChange> changes) {
        if (changes.stream().filter(OWLOntologyChange::isAxiomChange)
                .flatMap(c -> c.getAxiom().signature()).anyMatch(AsOWLDatatype::isOWLDatatype)) {
            reload();
        }
    }

    @Override
    public void disposeView() {
        getOWLModelManager().removeOntologyChangeListener(ontChangeListener);
        getOWLModelManager().removeListener(modelManagerListener);
    }

    @Override
    protected OWLDatatype updateView(OWLDatatype dt) {
        if (dt != null) {
            list.setSelectedValue(dt, true);
        } else {
            list.clearSelection();
        }
        return dt;
    }

    private void reload() {
        // Add all known datatypes including built in ones
        OWLModelManager m = getOWLModelManager();
        List<OWLDatatype> datatypeList = new OWLDataTypeUtils(m.getOWLOntologyManager())
                .knownDatatypes(getOWLModelManager().getActiveOntologies())
                .sorted(m.getOWLObjectComparator())
                .collect(Collectors.toList());
        list.setListData(datatypeList.toArray(new OWLDatatype[0]));
        OWLDatatype sel = getOWLWorkspace().getOWLSelectionModel().getLastSelectedDatatype();
        if (datatypeList.contains(sel)) {
            list.setSelectedValue(sel, true);
        }
    }

    @Override
    public List<OWLDatatype> find(String match) {
        return new ArrayList<>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLDatatypes(match));
    }

    @Override
    public void show(OWLDatatype dt) {
        updateView(dt);
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public Optional<OWLObject> getSelection() {
        return Optional.ofNullable(list.getSelectedValue());
    }
}
