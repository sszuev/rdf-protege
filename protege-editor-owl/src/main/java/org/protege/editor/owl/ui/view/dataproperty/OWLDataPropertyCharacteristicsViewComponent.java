package org.protege.editor.owl.ui.view.dataproperty;

import org.protege.editor.owl.model.axiom.FreshAxiomLocationPreferences;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationStrategy;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationStrategyFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.FilteringOWLOntologyChangeListener;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 21-Feb-2007<br><br>
 */
public class OWLDataPropertyCharacteristicsViewComponent extends AbstractOWLDataPropertyViewComponent {

    private JCheckBox checkBox;
    private OWLDataProperty prop;
    private OWLOntologyChangeListener listener;

    @Override
    protected OWLDataProperty updateView(OWLDataProperty property) {
        prop = property;
        checkBox.setEnabled(property != null);
        checkBox.setSelected(false);
        if (property == null) {
            return null;
        }
        for (OWLOntology ont : getOWLModelManager().getActiveOntologies()) {
            if (ont.functionalDataPropertyAxioms(prop).findFirst().isPresent()) {
                checkBox.setSelected(true);
                break;
            }
        }
        return property;
    }

    @Override
    public void disposeView() {
        super.disposeView();
        getOWLModelManager().removeOntologyChangeListener(listener);
    }

    @Override
    public void initialiseView() throws Exception {
        setLayout(new BorderLayout());
        checkBox = new JCheckBox("Functional");
        Box box = new Box(BoxLayout.PAGE_AXIS);
        box.setOpaque(false);
        box.add(checkBox);
        add(new JScrollPane(box));
        listener = new FilteringOWLOntologyChangeListener() {
            @Override
            public void visit(@Nonnull OWLFunctionalDataPropertyAxiom axiom) {
                updateView(prop);
            }
        };
        getOWLModelManager().addOntologyChangeListener(listener);
        checkBox.addActionListener(e -> updateOntology());
    }

    private void updateOntology() {
        if (prop == null) {
            return;
        }
        OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
        OWLAxiom ax = df.getOWLFunctionalDataPropertyAxiom(prop);
        if (checkBox.isSelected()) {
            FreshAxiomLocationPreferences preferences = FreshAxiomLocationPreferences.getPreferences();
            FreshAxiomLocationStrategyFactory strategyFactory = preferences.getFreshAxiomLocation().getStrategyFactory();
            FreshAxiomLocationStrategy strategy = strategyFactory.getStrategy(getOWLEditorKit());
            OWLOntology ont = strategy.getFreshAxiomLocation(ax, getOWLModelManager());
            getOWLModelManager().applyChange(new AddAxiom(ont, ax));
            return;
        }
        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLOntology ont : getOWLModelManager().getActiveOntologies()) {
            changes.add(new RemoveAxiom(ont, ax));
        }
        getOWLModelManager().applyChanges(changes);
    }
}
