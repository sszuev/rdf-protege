package org.protege.editor.owl.ui.view.objectproperty;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationPreferences;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationStrategy;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationStrategyFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.FilteringOWLOntologyChangeListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 29-Jan-2007<br><br>
 */
public class OWLObjectPropertyCharacteristicsViewComponent extends AbstractOWLObjectPropertyViewComponent {

    private static final long serialVersionUID = -1299595056337566960L;

    private JCheckBox functionalCB;
    private JCheckBox inverseFunctionalCB;
    private JCheckBox transitiveCB;
    private JCheckBox symmetricCB;
    private JCheckBox aSymmetricCB;
    private JCheckBox reflexiveCB;
    private JCheckBox irreflexiveCB;
    private List<JCheckBox> checkBoxes;
    private OWLOntologyChangeListener listener;
    private OWLObjectProperty prop;

    @Override
    @SuppressWarnings("NullableProblems")
    public void initialiseView() throws Exception {
        functionalCB = new JCheckBox("Functional");
        inverseFunctionalCB = new JCheckBox("Inverse functional");
        transitiveCB = new JCheckBox("Transitive");
        symmetricCB = new JCheckBox("Symmetric");
        aSymmetricCB = new JCheckBox("Asymmetric");
        reflexiveCB = new JCheckBox("Reflexive");
        irreflexiveCB = new JCheckBox("Irreflexive");

        checkBoxes = new ArrayList<>();
        checkBoxes.add(functionalCB);
        checkBoxes.add(inverseFunctionalCB);
        checkBoxes.add(transitiveCB);
        checkBoxes.add(symmetricCB);
        checkBoxes.add(aSymmetricCB);
        checkBoxes.add(reflexiveCB);
        checkBoxes.add(irreflexiveCB);


        setLayout(new BorderLayout());
        Box box = new Box(BoxLayout.Y_AXIS);
        box.setOpaque(false);
        box.add(functionalCB);
        box.add(Box.createVerticalStrut(7));
        box.add(inverseFunctionalCB);
        box.add(Box.createVerticalStrut(7));
        box.add(transitiveCB);
        box.add(Box.createVerticalStrut(7));
        box.add(symmetricCB);
        box.add(Box.createVerticalStrut(7));
        box.add(aSymmetricCB);
        box.add(Box.createVerticalStrut(7));
        box.add(reflexiveCB);
        box.add(Box.createVerticalStrut(7));
        box.add(irreflexiveCB);
        add(new JScrollPane(box));

        setupSetters();

        listener = new FilteringOWLOntologyChangeListener() {
            @Override
            public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
                updateView(prop);
            }

            @Override
            public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
                updateView(prop);
            }

            @Override
            public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
                updateView(prop);
            }

            @Override
            public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
                updateView(prop);
            }

            @Override
            public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
                updateView(prop);
            }

            @Override
            public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
                updateView(prop);
            }

            @Override
            public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
                updateView(prop);
            }
        };
        getOWLModelManager().addOntologyChangeListener(listener);
    }

    private void setupSetters() {
        addSetter(functionalCB, () -> getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(getProperty()));

        addSetter(inverseFunctionalCB, () -> getOWLDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(getProperty()));

        addSetter(transitiveCB, () -> getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(getProperty()));

        addSetter(symmetricCB, () -> getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(getProperty()));

        addSetter(aSymmetricCB, () -> getOWLDataFactory().getOWLAsymmetricObjectPropertyAxiom(getProperty()));

        addSetter(reflexiveCB, () -> getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(getProperty()));

        addSetter(irreflexiveCB, () -> getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(getProperty()));
    }

    private void addSetter(final JCheckBox checkBox, final PropertyCharacteristicSetter setter) {
        checkBox.addActionListener(e -> {
            if (getProperty() == null) {
                return;
            }
            if (checkBox.isSelected()) {
                FreshAxiomLocationPreferences preferences = FreshAxiomLocationPreferences.getPreferences();
                FreshAxiomLocationStrategyFactory strategyFactory = preferences.getFreshAxiomLocation().getStrategyFactory();
                FreshAxiomLocationStrategy strategy = strategyFactory.getStrategy(getOWLEditorKit());
                OWLAxiom ax = setter.getAxiom();
                OWLOntology ont = strategy.getFreshAxiomLocation(ax, getOWLModelManager());
                getOWLModelManager().applyChange(new AddAxiom(ont, ax));
            }
            else {
                List<OWLOntologyChange> changes = new ArrayList<>();
                OWLAxiom ax = setter.getAxiom();
                for (OWLOntology ont : getOWLModelManager().getActiveOntologies()) {
                    changes.add(new RemoveAxiom(ont, ax));
                }
                getOWLModelManager().applyChanges(changes);
            }
        });
    }

    private OWLObjectProperty getProperty() {
        return prop;
    }

    @Override
    public void disposeView() {
        getOWLModelManager().removeOntologyChangeListener(listener);
    }

    private void setCheckBoxesEnabled(boolean enable) {
        for (JCheckBox cb : checkBoxes) {
            cb.setEnabled(enable);
        }
    }

    private void clearAll() {
        for (JCheckBox cb : checkBoxes) {
            cb.setSelected(false);
        }
    }

    @Override
    protected OWLObjectProperty updateView(OWLObjectProperty property) {
        prop = property;
        clearAll();
        setCheckBoxesEnabled(property != null);
        if (property == null) {
            return null;
        }
        OWLModelManager m = getOWLModelManager();
        // We only require one axiom to specify that a property has a specific characteristic
        for (OWLOntology ont : m.getActiveOntologies()) {
            if (ont.functionalObjectPropertyAxioms(property).findFirst().isPresent()) {
                functionalCB.setSelected(true);
                if (!m.isMutable(ont)) {
                    functionalCB.setEnabled(false);
                }
            }
            if (ont.inverseFunctionalObjectPropertyAxioms(property).findFirst().isPresent()) {
                inverseFunctionalCB.setSelected(true);
                if (!m.isMutable(ont)) {
                    inverseFunctionalCB.setEnabled(false);
                }
            }
            if (ont.transitiveObjectPropertyAxioms(property).findFirst().isPresent()) {
                transitiveCB.setSelected(true);
                if (!m.isMutable(ont)) {
                    transitiveCB.setEnabled(false);
                }
            }
            if (ont.symmetricObjectPropertyAxioms(property).findFirst().isPresent()) {
                symmetricCB.setSelected(true);
                if (!m.isMutable(ont)) {
                    symmetricCB.setEnabled(false);
                }
            }
            if (ont.asymmetricObjectPropertyAxioms(property).findFirst().isPresent()) {
                aSymmetricCB.setSelected(true);
                if (!m.isMutable(ont)) {
                    aSymmetricCB.setEnabled(false);
                }
            }
            if (ont.reflexiveObjectPropertyAxioms(property).findFirst().isPresent()) {
                reflexiveCB.setSelected(true);
                if (!m.isMutable(ont)) {
                    reflexiveCB.setEnabled(false);
                }
            }
            if (ont.irreflexiveObjectPropertyAxioms(property).findFirst().isPresent()) {
                irreflexiveCB.setSelected(true);
                if (!m.isMutable(ont)) {
                    irreflexiveCB.setEnabled(false);
                }
            }
        }

        return property;
    }

    private interface PropertyCharacteristicSetter {

        OWLAxiom getAxiom();
    }
}
