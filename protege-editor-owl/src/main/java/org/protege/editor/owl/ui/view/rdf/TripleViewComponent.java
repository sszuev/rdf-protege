package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.protege.editor.owl.ui.view.rdf.utils.PrintUtils;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * TODO: under developing
 * Created by @ssz on 07.01.2021.
 */
public class TripleViewComponent extends AbstractOWLSelectionViewComponent {

    private final JLabel header = new JLabel();
    private final JTextField subject = new JTextField();
    private final JTextField predicate = new JTextField();
    private final JTextField object = new JTextField();
    private final JTextField axiom = new JTextField();

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
        typePanel.add(new JLabel("Details "));
        typePanel.add(header);
        typePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
        add(typePanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));

        add(panel);

        JPanel p = createPanel();

        panel.add(p, BorderLayout.BEFORE_FIRST_LINE);
        refill();
    }

    public JPanel createPanel() {
        JPanel res = new JPanel(new GridBagLayout());
        Stream.of(subject, predicate, object, axiom).forEach(t -> {
            t.setBackground(Color.WHITE);
            t.setEditable(false);
        });
        AddTriplePanel.addSimpleRow(res, "SUBJECT", subject, 1);
        AddTriplePanel.addSimpleRow(res, "PREDICATE", predicate, 2);
        AddTriplePanel.addSimpleRow(res, "OBJECT", object, 3);
        AddTriplePanel.addSimpleRow(res, "AXIOM", axiom, 4);

        return res;
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
        // todo: no need
    }

    protected void refill(Triple t) {
        PrefixMapping model = getPrefixMapping();
        PrefixMapping std = OntModelFactory.STANDARD;
        Function<Object, String> bm = getBlankNodeMapper();
        header.setText(PrintUtils.printTriple(t, model, bm, true));

        subject.setText(PrintUtils.printSubject(t.getSubject(), std, bm));
        predicate.setText(PrintUtils.printPredicate(t.getPredicate(), std));
        object.setText(PrintUtils.printObject(t.getObject(), std, bm, true));
        axiom.setText(PrintUtils.printOWLInfo(t, getActiveOntology()));
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
