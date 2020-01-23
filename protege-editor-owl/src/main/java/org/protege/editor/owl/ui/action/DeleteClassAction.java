package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.DeleteEntityIcon;
import org.protege.editor.owl.ui.renderer.OWLClassIcon;
import org.protege.editor.owl.ui.renderer.OWLEntityIcon;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.util.OWLEntitySetProvider;

import javax.swing.*;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 29-May-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class DeleteClassAction extends AbstractDeleteEntityAction<OWLClass> {

    private static final Icon DELETE_ICON = new DeleteEntityIcon(new OWLClassIcon(OWLClassIcon.Type.PRIMITIVE, OWLEntityIcon.FillType.HOLLOW));

    private final OWLClass thing;

    public DeleteClassAction(OWLEditorKit kit, OWLEntitySetProvider<OWLClass> clsSetProvider) {
        super("Delete selected classes", DELETE_ICON, kit, kit.getModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider(), clsSetProvider);
        thing = kit.getModelManager().getOWLDataFactory().getOWLThing();
    }

    @Override
    public void updateState() {
        Set<OWLClass> res = entitySetProvider.entities().collect(Collectors.toSet());
        setEnabled(!res.isEmpty() && !res.contains(thing));
    }

    @Override
    protected String getPluralDescription() {
        return "classes";
    }

    protected String getResultsViewId() {
        return "OWLClassUsageView";
    }
}
