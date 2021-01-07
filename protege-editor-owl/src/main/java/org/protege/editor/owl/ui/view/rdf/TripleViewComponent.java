package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.Ontology;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

/**
 * TODO: under developing
 * Created by @ssz on 07.01.2021.
 */
public class TripleViewComponent extends AbstractOWLSelectionViewComponent {

    private final JLabel tmpLabel = new JLabel();

    private final OWLSelectionModelListener listener = () -> {
        Object t = getSelectionModel().getSelectedObject();
        if (t instanceof Triple) {
            refill((Triple) t);
        }
    };

    @Override
    public void initialiseView() throws Exception {
        initialiseIndividualsView();
    }

    public void initialiseIndividualsView() throws Exception {
        getSelectionModel().addListener(listener);

        setLayout(new BorderLayout());
        JComponent typePanel = new Box(BoxLayout.X_AXIS);
        typePanel.add(new JLabel("Triple: "));
        typePanel.add(tmpLabel);
        typePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
        add(typePanel, BorderLayout.NORTH);

        refill();
    }

    @Override
    public void disposeView() {
    }

    @Override
    protected OWLObject updateView() {
        return null;
    }

    @Override
    public void refreshComponent() {
        refill();
    }

    protected void refill() {
    }

    protected void refill(Triple t) {
        PrefixMapping pm = getPrefixMapping();
        Function<Object, String> bm = getBlankNodeMapper();
        tmpLabel.setText(PrintUtils.printTriple(t, pm, bm, true));
    }

    public Ontology getActiveOntology() {
        return OWLAdapter.get().asONT(getOWLModelManager().getActiveOntology());
    }

    public Function<Object, String> getBlankNodeMapper() {
        return getOWLModelManager().getBlankNodeMapper();
    }

    public PrefixMapping getPrefixMapping() {
        return getActiveOntology().asGraphModel();
    }

    protected OWLSelectionModel getSelectionModel() {
        return getOWLWorkspace().getOWLSelectionModel();
    }

}
