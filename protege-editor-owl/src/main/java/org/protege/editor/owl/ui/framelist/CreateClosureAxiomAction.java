package org.protege.editor.owl.ui.framelist;

import org.protege.editor.owl.model.util.ClosureAxiomFactory;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 24, 2008<br><br>
 */
public class CreateClosureAxiomAction extends OWLFrameListPopupMenuAction<OWLClass> {

    @Override
    protected String getName() {
        return "Create closure axiom";
    }

    @Override
    protected void initialise() throws Exception {
    }

    @Override
    protected void dispose() throws Exception {
    }

    private Set<OWLObjectProperty> getPropertiesFromSelection() {
        ClosureSourceIdentifier closureSourceIdentifier = new ClosureSourceIdentifier();
        for (Object val : getFrameList().getSelectedValuesList()) {
            if (val instanceof OWLFrameSectionRow) {
                OWLAxiom ax = ((OWLFrameSectionRow<?, ?, ?>) val).getAxiom();
                ax.accept(closureSourceIdentifier);
            }
        }
        return closureSourceIdentifier.getPropertiesToClose();
    }

    @Override
    protected void updateState() {
        setEnabled(!getPropertiesFromSelection().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<OWLOntologyChange> changes = new ArrayList<>();

        final OWLOntology activeOnt = getOWLModelManager().getActiveOntology();
        final Set<OWLOntology> activeOnts = getOWLModelManager().getActiveOntologies();
        final OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
        final OWLClass root = getRootObject();

        for (OWLObjectProperty prop : getPropertiesFromSelection()) {
            OWLAxiom ax = ClosureAxiomFactory.getClosureAxiom(root, prop, df, activeOnts);
            if (ax != null && !activeOnt.containsAxiom(ax)) {
                changes.add(new AddAxiom(activeOnt, ax));
            }
        }
        if (!changes.isEmpty()) {
            getOWLModelManager().applyChanges(changes);
        }
    }


    /**
     * Gets the properties of some, min and exact restrictions from super or equivalent class axioms
     */
    @SuppressWarnings("NullableProblems")
    static class ClosureSourceIdentifier implements OWLObjectVisitor {

        private final Set<OWLObjectProperty> propertiesToClose = new HashSet<>();
        private final Set<OWLObject> visited = new HashSet<>();

        public Set<OWLObjectProperty> getPropertiesToClose() {
            return propertiesToClose;
        }

        @Override
        public void visit(OWLSubClassOfAxiom owlSubClassAxiom) {
            if (visited.contains(owlSubClassAxiom)) {
                return;
            }
            visited.add(owlSubClassAxiom);
            owlSubClassAxiom.getSuperClass().accept(this);
        }

        @Override
        public void visit(OWLEquivalentClassesAxiom axiom) {
            if (visited.contains(axiom)) {
                return;
            }
            visited.add(axiom);
            axiom.classExpressions().forEach(op -> op.accept(this));
        }

        @Override
        public void visit(OWLObjectIntersectionOf owlObjectIntersectionOf) {
            owlObjectIntersectionOf.operands().forEach(op -> op.accept(this));
        }

        @Override
        public void visit(OWLObjectSomeValuesFrom restr) {
            restr.getProperty().accept(this);
        }

        @Override
        public void visit(OWLObjectMinCardinality restr) {
            restr.getProperty().accept(this);
        }

        @Override
        public void visit(OWLObjectExactCardinality restr) {
            restr.getProperty().accept(this);
        }

        @Override
        public void visit(OWLObjectHasValue restr) {
            restr.getProperty().accept(this);
        }

        @Override
        public void visit(OWLObjectProperty owlObjectProperty) {
            propertiesToClose.add(owlObjectProperty);
        }
    }
}
