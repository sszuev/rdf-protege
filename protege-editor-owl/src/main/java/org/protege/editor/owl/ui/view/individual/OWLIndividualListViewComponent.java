package org.protege.editor.owl.ui.view.individual;

import org.protege.editor.core.ui.RefreshableComponent;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.model.util.OWLEntityDeleter;
import org.protege.editor.owl.ui.action.DeleteIndividualAction;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.protege.editor.owl.ui.renderer.AddEntityIcon;
import org.protege.editor.owl.ui.renderer.OWLIndividualIcon;
import org.protege.editor.owl.ui.view.ChangeListenerMediator;
import org.protege.editor.owl.ui.view.CreateNewTarget;
import org.protege.editor.owl.ui.view.Deleteable;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityCollector;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br>
 * <br>
 * This definitely needs a rethink - it is a totally inefficient hack!
 */

public class OWLIndividualListViewComponent extends AbstractOWLIndividualViewComponent
        implements Findable<OWLNamedIndividual>, Deleteable, CreateNewTarget, RefreshableComponent, SelectionDriver {

    private OWLObjectList<OWLNamedIndividual> list;
    private OWLOntologyChangeListener listener;
    private ChangeListenerMediator changeListenerMediator;
    private OWLModelManagerListener modelManagerListener;
    private boolean selectionChangedByUser = true;
    protected Set<OWLNamedIndividual> individualsInList;

    private final ListSelectionListener listSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            if (list.getSelectedValue() != null && selectionChangedByUser) {
                setGlobalSelection(list.getSelectedValue());
            }
            changeListenerMediator.fireStateChanged(OWLIndividualListViewComponent.this);
        }
    };

    @Override
    public void initialiseIndividualsView() throws Exception {
        list = new OWLObjectList<>(getOWLEditorKit());
        int selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        list.setSelectionMode(selectionMode);
        setLayout(new BorderLayout());
        add(new JScrollPane(list));
        list.addListSelectionListener(listSelectionListener);
        list.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                setGlobalSelection(list.getSelectedValue());
            }
        });
        listener = this::processChanges;
        getOWLModelManager().addOntologyChangeListener(listener);

        setupActions();
        changeListenerMediator = new ChangeListenerMediator();
        individualsInList = new TreeSet<>(getOWLModelManager().getOWLObjectComparator());
        refill();
        modelManagerListener = event -> {
            if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || event.isType(EventType.ONTOLOGY_RELOADED)) {
                refill();
            }
        };
        getOWLModelManager().addListener(modelManagerListener);
    }

    protected void setupActions() {
        addAction(new AddIndividualAction(), "A", "A");
        addAction(new DeleteIndividualAction(getOWLEditorKit(), () -> getSelectedIndividuals().stream()), "B", "A");
    }

    @Override
    public void refreshComponent() {
        refill();
    }

    protected void refill() {
        // Initial fill
        individualsInList.clear();
        for (OWLOntology ont : getOntologies()) {
            ont.individualsInSignature().forEach(x -> individualsInList.add(x));
        }
        reset();
    }

    protected Set<OWLOntology> getOntologies() {
        return getOWLModelManager().getActiveOntologies();
    }

    public void setSelectedIndividual(OWLIndividual individual) {
        list.setSelectedValue(individual, true);
    }

    protected void reset() {
        OWLNamedIndividual[] objects = individualsInList.toArray(new OWLNamedIndividual[0]);
        list.setListData(objects);
        OWLNamedIndividual individual = getSelectedOWLIndividual();
        selectionChangedByUser = false;
        try {
            list.setSelectedValue(individual, true);
        } finally {
            selectionChangedByUser = true;
        }
    }

    @Override
    public OWLNamedIndividual updateView(OWLNamedIndividual selected) {
        if (!isPinned()) {
            list.setSelectedValue(selected, true);
        }
        return list.getSelectedValue();
    }

    @Override
    public void disposeView() {
        getOWLModelManager().removeOntologyChangeListener(listener);
        getOWLModelManager().removeListener(modelManagerListener);
    }

    public OWLNamedIndividual getSelectedIndividual() {
        return list.getSelectedValue();
    }

    public Set<OWLNamedIndividual> getSelectedIndividuals() {
        return new LinkedHashSet<>(list.getSelectedValuesList());
    }

    protected void processChanges(List<? extends OWLOntologyChange> changes) {
        Set<OWLEntity> possiblyAddedObjects = new HashSet<>();
        Set<OWLEntity> possiblyRemovedObjects = new HashSet<>();
        OWLEntityCollector addedCollector = new OWLEntityCollector(possiblyAddedObjects);
        OWLEntityCollector removedCollector = new OWLEntityCollector(possiblyRemovedObjects);
        for (OWLOntologyChange chg : changes) {
            if (!chg.isAxiomChange()) {
                continue;
            }
            OWLAxiomChange axChg = (OWLAxiomChange) chg;
            if (axChg instanceof AddAxiom) {
                axChg.getAxiom().accept(addedCollector);
            } else {
                axChg.getAxiom().accept(removedCollector);
            }
        }
        boolean mod = false;
        for (OWLEntity ent : possiblyAddedObjects) {
            if (ent instanceof OWLIndividual) {
                if (individualsInList.add((OWLNamedIndividual) ent)) {
                    mod = true;
                }
            }
        }
        for (OWLEntity ent : possiblyRemovedObjects) {
            if (!(ent instanceof OWLIndividual)) {
                continue;
            }
            boolean stillReferenced = false;
            for (OWLOntology ont : getOntologies()) {
                if (ont.containsIndividualInSignature(ent.getIRI())) {
                    stillReferenced = true;
                    break;
                }
            }
            if (stillReferenced) {
                continue;
            }
            //noinspection SuspiciousMethodCalls
            if (individualsInList.remove(ent)) {
                mod = true;
            }
        }
        if (mod) {
            reset();
        }
    }

    protected void addIndividual() {
        OWLEntityCreationSet<OWLNamedIndividual> set = getOWLWorkspace().createOWLIndividual();
        if (set == null) {
            return;
        }
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.addAll(set.getOntologyChanges());
        changes.addAll(dofurtherCreateSteps(set.getOWLEntity()));
        getOWLModelManager().applyChanges(changes);
        OWLNamedIndividual ind = set.getOWLEntity();
        if (ind != null) {
            list.setSelectedValue(ind, true);
        }
    }

    protected List<OWLOntologyChange> dofurtherCreateSteps(OWLIndividual newIndividual) {
        return Collections.emptyList();
    }

    @Override
    public List<OWLNamedIndividual> find(String match) {
        return new ArrayList<>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLIndividuals(match));
    }

    @Override
    public void show(OWLNamedIndividual owlEntity) {
        list.setSelectedValue(owlEntity, true);
    }

    public void setSelectedIndividuals(Set<OWLNamedIndividual> individuals) {
        list.setSelectedValues(individuals, true);
    }

    private class AddIndividualAction extends DisposableAction {

        public AddIndividualAction() {
            super("Add individual", new AddEntityIcon(new OWLIndividualIcon()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addIndividual();
        }

        @Override
        public void dispose() {
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeListenerMediator.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeListenerMediator.removeChangeListener(listener);
    }

    @Override
    public void handleDelete() {
        OWLEntityDeleter.deleteEntities(getSelectedIndividuals(), getOWLModelManager());
    }

    @Override
    public boolean canDelete() {
        return !getSelectedIndividuals().isEmpty();
    }

    @Override
    public boolean canCreateNew() {
        return true;
    }

    @Override
    public void createNewObject() {
        addIndividual();
    }

    public void setSelectionMode(int selectionMode) {
        if (list != null) {
            list.setSelectionMode(selectionMode);
        }
    }

    public void setIndividualListColor(Color c) {
        list.setBackground(c);
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public Optional<OWLObject> getSelection() {
        return Optional.ofNullable(getSelectedIndividual());
    }
}
