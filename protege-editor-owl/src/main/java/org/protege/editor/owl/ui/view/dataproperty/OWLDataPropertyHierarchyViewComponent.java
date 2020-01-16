package org.protege.editor.owl.ui.view.dataproperty;

import org.protege.editor.core.ui.menu.PopupMenuId;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.ui.renderer.*;
import org.protege.editor.owl.ui.view.AbstractOWLPropertyHierarchyViewComponent;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Jan-2007<br><br>
 */
public class OWLDataPropertyHierarchyViewComponent
        extends AbstractOWLPropertyHierarchyViewComponent<OWLDataProperty>
        implements SelectionDriver {

    @Override
    protected boolean isOWLDataPropertyView() {
        return true;
    }

    @Override
    protected void performExtraInitialisation() throws Exception {
        super.performExtraInitialisation();
        getAssertedTree().setPopupMenuId(new PopupMenuId("[AssertedDataPropertyHierarchy]"));
    }

    @Override
    protected OWLHierarchyProvider<OWLDataProperty> getHierarchyProvider() {
        return getOWLModelManager().getOWLHierarchyManager().getOWLDataPropertyHierarchyProvider();
    }

    @Override
    protected Optional<OWLHierarchyProvider<OWLDataProperty>> getInferredHierarchyProvider() {
        return Optional.of(getOWLModelManager().getOWLHierarchyManager().getOWLDataPropertyHierarchyProvider());
    }

    @Override
    protected OWLSubDataPropertyOfAxiom getSubPropertyAxiom(OWLDataProperty child, OWLDataProperty parent) {
        return getOWLDataFactory().getOWLSubDataPropertyOfAxiom(child, parent);
    }

    @Override
    protected boolean canAcceptDrop(Object child, Object parent) {
        return child instanceof OWLDataProperty;
    }

    @Override
    protected OWLEntityCreationSet<OWLDataProperty> createProperty() {
        return getOWLWorkspace().createOWLDataProperty();
    }

    @Override
    protected Icon getSubIcon() {
        return new AddChildIcon(new OWLDataPropertyIcon());
    }

    @Override
    protected Icon getSibIcon() {
        return new AddSiblingIcon(new OWLDataPropertyIcon());
    }

    @Override
    protected Icon getDeleteIcon() {
        return new DeleteEntityIcon(new OWLDataPropertyIcon(OWLEntityIcon.FillType.HOLLOW));
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
