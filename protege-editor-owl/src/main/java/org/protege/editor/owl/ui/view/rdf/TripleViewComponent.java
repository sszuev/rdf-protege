package org.protege.editor.owl.ui.view.rdf;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.internal.ONTObject;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.protege.editor.owl.ui.view.rdf.utils.GridBagUtils;
import org.protege.editor.owl.ui.view.rdf.utils.OWLTripleUtils;
import org.protege.editor.owl.ui.view.rdf.utils.PrintUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import java.awt.*;
import java.io.StringWriter;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final JTextArea turtle = new JTextArea();

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

        panel.add(createDisplayPanel(), BorderLayout.BEFORE_FIRST_LINE);
    }

    public JPanel createDisplayPanel() {
        JPanel res = new JPanel(new GridBagLayout());
        Stream.of(subject, predicate, object, axiom, turtle).forEach(x -> {
            x.setBackground(Color.WHITE);
            x.setEditable(false);
        });
        turtle.setVisible(false);
        GridBagUtils.addSimpleRow(res, "SUBJECT", " ", subject, 1);
        GridBagUtils.addSimpleRow(res, "PREDICATE", " ", predicate, 2);
        GridBagUtils.addSimpleRow(res, "OBJECT", " ", object, 3);
        GridBagUtils.addSimpleRow(res, "AXIOM", " ", axiom, 4);
        GridBagUtils.addSimpleRow(res, "CONTENT", " ", turtle, 5);

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
    }

    protected void refill(Triple t) {
        PrefixMapping model = getPrefixMapping();
        PrefixMapping std = OntModelFactory.STANDARD;
        Function<Object, String> bm = getBlankNodeMapper();
        header.setText(PrintUtils.printTriple(t, model, bm, true));

        subject.setText(PrintUtils.printSubject(t.getSubject(), std, bm));
        predicate.setText(PrintUtils.printPredicate(t.getPredicate(), std));
        object.setText(PrintUtils.printObject(t.getObject(), std, bm, true));
        turtle.setVisible(false);
        if (t.getPredicate().equals(RDF.type.asNode()) && t.getObject().equals(OWL.Ontology.asNode())) {
            axiom.setText("HEADER");
            return;
        }
        Collection<ONTObject<? extends OWLAxiom>> axioms = OWLTripleUtils.getAxioms(t, getActiveOntology());
        if (axioms.isEmpty()) {
            axiom.setText("");
            return;
        }
        String ax = axioms.stream().map(ONTObject::getOWLObject)
                .map(Object::toString).collect(Collectors.joining(", "));
        axiom.setText(ax);

        Model m = OntModelFactory.createDefaultModel().setNsPrefixes(model);
        Graph g = m.getGraph();
        axioms.forEach(x -> x.triples().forEach(g::add));
        if (m.size() <= 1) {
            return;
        }
        StringWriter sw = new StringWriter();
        m.write(sw, "ttl", null);
        turtle.setText(sw.toString());
        turtle.setVisible(true);
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
