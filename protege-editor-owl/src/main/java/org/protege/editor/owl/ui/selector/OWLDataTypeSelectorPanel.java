package org.protege.editor.owl.ui.selector;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.core.ui.view.ViewComponentPlugin;
import org.protege.editor.core.ui.view.ViewComponentPluginAdapter;
import org.protege.editor.core.ui.workspace.Workspace;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 27-Sep-2006<br><br>
 */
public class OWLDataTypeSelectorPanel extends AbstractSelectorPanel<OWLDatatype> {

    private AbstractOWLViewComponent vc;

    private OWLObjectList<OWLDatatype> list;

    private Map<ChangeListener, ListSelectionListener> selListenerWrappers = new HashMap<>();

    public OWLDataTypeSelectorPanel(OWLEditorKit editorKit) {
        super(editorKit);
    }


    public OWLDataTypeSelectorPanel(OWLEditorKit editorKit, boolean editable) {
        super(editorKit, editable);
    }

    @Override
    public void setSelection(OWLDatatype dt) {
        list.setSelectedValue(dt, true);
    }

    @Override
    public void setSelection(Set<OWLDatatype> ranges) {
        list.setSelectedValues(ranges, true);
    }

    @Override
    public OWLDatatype getSelectedObject() {
        return list.getSelectedValue();
    }

    @Override
    public Set<OWLDatatype> getSelectedObjects() {
        return new HashSet<>(list.getSelectedOWLObjects());
    }

    @Override
    protected ViewComponentPlugin getViewComponentPlugin() {
        return new ViewComponentPluginAdapter() {
            @Override
            public String getLabel() {
                return "Datatypes";
            }

            @Override
            public Workspace getWorkspace() {
                return getOWLEditorKit().getWorkspace();
            }

            @Override
            public ViewComponent newInstance() {
                vc = new OWLDatatypeListView();
                vc.setup(this);
                return vc;
            }
        };
    }

    public void dispose() {
        vc.dispose();
    }

    public void addSelectionListener(ChangeListener listener) {
        list.addListSelectionListener(wrapListener(listener));
    }

    public void removeSelectionListener(ChangeListener listener) {
        list.removeListSelectionListener(wrapListener(listener));
    }

    private ListSelectionListener wrapListener(final ChangeListener listener) {
        return selListenerWrappers.computeIfAbsent(listener, l1 -> event -> l1.stateChanged(new ChangeEvent(list)));
    }

    private Stream<OWLDatatype> datatypes() {
        OWLModelManager m = getOWLModelManager();
        OWLOntologyManager manager = m.getOWLOntologyManager();
        return new OWLDataTypeUtils(manager)
                .knownDatatypes(m.getActiveOntologies())
                .sorted(m.getOWLObjectComparator());
    }

    private void rebuildDatatypeList() {
        OWLDatatype selected = list.getSelectedValue();
        List<OWLDatatype> datatypes = datatypes().collect(Collectors.toList());
        list.setListData(datatypes.toArray(new OWLDatatype[]{}));
        if (datatypes.contains(selected)) {
            list.setSelectedValue(selected, true);
        }
    }

    @SuppressWarnings("NullableProblems")
    private class UpdateDatatypeListListener implements OWLOntologyChangeListener {
        @Override
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            if (datatypesMightHaveChanged(changes)) {
                rebuildDatatypeList();
            }
        }

        private boolean datatypesMightHaveChanged(List<? extends OWLOntologyChange> changes) {
            return changes.stream().filter(c -> c instanceof OWLAxiomChange)
                    .flatMap(c -> c.getAxiom().signature())
                    .anyMatch(e -> e instanceof OWLDatatype && !e.isBuiltIn());
        }
    }

    private class ActiveOntologyChangedListener implements OWLModelManagerListener {
        @Override
        public void handleChange(OWLModelManagerChangeEvent event) {
            if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
                rebuildDatatypeList();
            }
        }
    }

    private class OWLDatatypeListView extends AbstractOWLViewComponent {

        private final OWLOntologyChangeListener ontologyChangeListener = new UpdateDatatypeListListener();
        private final OWLModelManagerListener p4Listener = new ActiveOntologyChangedListener();

        @Override
        protected void initialiseOWLView() {
            setLayout(new BorderLayout());

            list = new OWLObjectList<>(getOWLEditorKit());
            list.setListData(datatypes().toArray(OWLDatatype[]::new));
            list.setSelectedIndex(0);
            add(ComponentFactory.createScrollPane(list));
            getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);
            getOWLModelManager().addListener(p4Listener);
        }

        @Override
        protected void disposeOWLView() {
            getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
            getOWLModelManager().removeListener(p4Listener);
        }
    }
}
