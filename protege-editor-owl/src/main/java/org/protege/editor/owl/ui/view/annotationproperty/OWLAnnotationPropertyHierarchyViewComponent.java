package org.protege.editor.owl.ui.view.annotationproperty;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.ui.action.AbstractDeleteEntityAction;
import org.protege.editor.owl.ui.action.AbstractOWLTreeAction;
import org.protege.editor.owl.ui.renderer.*;
import org.protege.editor.owl.ui.view.AbstractOWLEntityHierarchyViewComponent;
import org.protege.editor.owl.ui.view.CreateNewChildTarget;
import org.protege.editor.owl.ui.view.CreateNewSiblingTarget;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Apr 23, 2009<br><br>
 */
public class OWLAnnotationPropertyHierarchyViewComponent
        extends AbstractOWLEntityHierarchyViewComponent<OWLAnnotationProperty>
        implements CreateNewChildTarget, CreateNewSiblingTarget, SelectionDriver {

    @Override
    protected void performExtraInitialisation() throws Exception {
        addAction(new AbstractOWLTreeAction<OWLAnnotationProperty>("Add sub property",
                new AddChildIcon(new OWLAnnotationPropertyIcon()), getTree().getSelectionModel()) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewChild();
            }

            @Override
            protected boolean canPerform(OWLAnnotationProperty prop) {
                return canCreateNewChild();
            }
        }, "A", "A");

        addAction(new AbstractOWLTreeAction<OWLAnnotationProperty>("Add sibling property",
                new AddSiblingIcon(new OWLAnnotationPropertyIcon()), getTree().getSelectionModel()) {
            @Override
            public void actionPerformed(ActionEvent event) {
                createNewSibling();
            }

            @Override
            protected boolean canPerform(OWLAnnotationProperty cls) {
                return canCreateNewSibling();
            }
        }, "A", "B");

        addAction(new AbstractDeleteEntityAction<OWLAnnotationProperty>("Delete selected properties",
                getDeleteIcon(),
                getOWLEditorKit(),
                getHierarchyProvider(),
                () -> getSelectedEntities().stream()) {

            @Override
            protected String getPluralDescription() {
                return "properties";
            }
        }, "B", "A");
    }

    private Icon getDeleteIcon() {
        return new DeleteEntityIcon(new OWLAnnotationPropertyIcon(OWLEntityIcon.FillType.HOLLOW));
    }

    @Override
    protected OWLHierarchyProvider<OWLAnnotationProperty> getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getOWLAnnotationPropertyHierarchyProvider();
    }

    @Override
    protected Optional<OWLHierarchyProvider<OWLAnnotationProperty>> getInferredHierarchyProvider() {
        return Optional.empty();
    }

    @Override
    protected OWLObject updateView() {
        OWLSelectionModel sm = getOWLWorkspace().getOWLSelectionModel();
        OWLAnnotationProperty res = sm.getLastSelectedAnnotationProperty();
        updateView(res);
        return res;
    }

    @Override
    public List<OWLAnnotationProperty> find(String match) {
        return new ArrayList<>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLAnnotationProperties(match));
    }

    @Override
    public boolean canCreateNewChild() {
        return true;
    }

    @Override
    public void createNewChild() {
        OWLEntityCreationSet<OWLAnnotationProperty> set = getOWLWorkspace().createOWLAnnotationProperty();
        List<OWLOntologyChange> changes = new ArrayList<>(set.getOntologyChanges());
        OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
        OWLAnnotationProperty selProp = getSelectedEntity();
        if (selProp != null) {
            OWLAxiom ax = df.getOWLSubAnnotationPropertyOfAxiom(set.getOWLEntity(), selProp);
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
        }
        getOWLModelManager().applyChanges(changes);
        setGlobalSelection(set.getOWLEntity());
    }

    @Override
    public boolean canCreateNewSibling() {
        return getSelectedEntity() != null;
    }

    @Override
    public void createNewSibling() {
        OWLAnnotationProperty property = getTree().getSelectedObject();
        if (property == null) {
            // Shouldn't really get here, because the
            // action should be disabled
            return;
        }
        // We need to apply the changes in the active ontology
        OWLEntityCreationSet<OWLAnnotationProperty> creationSet = getOWLWorkspace().createOWLAnnotationProperty();
        if (creationSet == null) {
            return;
        }
        // Combine the changes that are required to create the OWLAnnotationProperty, with the
        // changes that are required to make it a sibling property.
        List<OWLOntologyChange> changes = new ArrayList<>(creationSet.getOntologyChanges());
        OWLModelManager mngr = getOWLModelManager();
        OWLDataFactory df = mngr.getOWLDataFactory();
        OWLOntology o = mngr.getActiveOntology();
        getHierarchyProvider()
                .parents(property)
                .map(x -> new AddAxiom(o, df.getOWLSubAnnotationPropertyOfAxiom(creationSet.getOWLEntity(), x)))
                .forEach(changes::add);
        mngr.applyChanges(changes);
        setGlobalSelection(creationSet.getOWLEntity());
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public Optional<OWLObject> getSelection() {
        return Optional.ofNullable(getSelectedEntity());
    }
}
