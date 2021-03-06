package org.protege.editor.owl.ui.view.rdf;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.protege.editor.core.ui.util.AugmentedJTextField;
import org.protege.editor.owl.ui.view.rdf.utils.GridBagUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Created by @ssz on 25.01.2020.
 */
public class AddTriplePanel extends VerifiedInputEditorJPanel {
    public static final int FIELD_WIDTH = 60;
    public static final String SUBJECT = "Subject";
    public static final String PREDICATE = "Predicate";
    public static final String OBJECT = "Object";
    public static final String DELIMITER = "=";
    public static final String IRI_GHOST_TEXT = "Short name or full IRI or Prefix-Name";
    public static final String BLANK_NODE_PLACEHOLDER = "<anonymous>";
    public static final String IRI_CONTROL_OPTION = "IRI";
    public static final String BLANK_NODE_CONTROL_OPTION = "Blank Node";
    public static final String FRESH_BLANK_NODE_CONTROL_OPTION = "Fresh Blank Node";
    public static final String MODEL_BLANK_NODE_CONTROL_OPTION = "Model Blank Node";
    public static final String PLAIN_LITERAL_CONTROL_OPTION = "Plain Literal";
    public static final String TYPED_LITERAL_CONTROL_OPTION = "Typed Literal";
    public static final String LANG_LITERAL_CONTROL_OPTION = "Lang Literal";

    public static final Insets CELL_INSETS = new Insets(0, 0, 2, 2);
    public static final Insets LINE_INSETS = new Insets(10, 2, 10, 2);

    protected final JTextField subjectIRI = new AugmentedJTextField(FIELD_WIDTH, IRI_GHOST_TEXT);
    protected final JTextField predicateIRI = new AugmentedJTextField(FIELD_WIDTH, IRI_GHOST_TEXT);
    protected final JTextField objectIRI = new AugmentedJTextField(FIELD_WIDTH, IRI_GHOST_TEXT);
    protected final JTextField subjectField = new AugmentedJTextField(FIELD_WIDTH, "IRI or Blank Node");
    protected final JTextField predicateField = new AugmentedJTextField(FIELD_WIDTH, "IRI");
    protected final JTextField objectField = new AugmentedJTextField(FIELD_WIDTH, "IRI, Blank Node or Literal");
    protected final JComboBox<String> subjectController = new JComboBox<>(new String[]{IRI_CONTROL_OPTION, BLANK_NODE_CONTROL_OPTION});
    protected final JComboBox<String> predicateController = new JComboBox<>(new String[]{IRI_CONTROL_OPTION, "Built-in IRI"});
    protected final ObjectController objectController;
    protected final JPanel literalForm = new JPanel();
    protected final JTextComponent literalText = new JTextArea(1, FIELD_WIDTH - 18);
    protected final JComboBox<String> literalLangs;
    protected final JComboBox<String> literalDatatypes;
    protected final JComboBox<TripleModel.BNode> objectBlanks;
    protected final JComboBox<String> predicateSystemPropertySelector;

    protected final PrefixMapping pm;
    protected final TypeMapper types = TypeMapper.getInstance();
    protected final UnaryOperator<String> toIRI;
    protected final Supplier<String> toLiteral;

    public AddTriplePanel(TripleModel m) {
        super();
        String base = Objects.requireNonNull(m).getBaseURI();
        this.pm = m.getPrefixMapping();
        this.predicateSystemPropertySelector = new JComboBox<>(TripleModel.toArray(m.getProperties(), pm));
        this.literalLangs = new JComboBox<>(m.getLanguageTags());
        this.literalDatatypes = new JComboBox<>(TripleModel.toArray(m.getDatatypes(), pm));
        TripleModel.BNode[] bnodes = m.getBlankNodes().toArray(new TripleModel.BNode[0]);
        this.objectBlanks = new JComboBox<>(bnodes);
        this.objectController = ObjectController.createObjectController(bnodes.length != 0);
        this.toIRI = x -> toIRI(base, x);
        this.toLiteral = this::getLiteralString;

        initSubjectConfiguration();
        initPredicateConfiguration();
        initObjectConfiguration();
        setLayout(new BorderLayout());
        add(createContentPanel());
    }

    public static boolean isEmpty(String x) {
        return x == null || x.isEmpty();
    }

    @SafeVarargs
    public static <X> boolean test(X x, BiPredicate<X, X> test, X... parts) {
        for (X p : parts) {
            if (test.test(x, p)) return true;
        }
        return false;
    }

    public static Optional<String> findSelectedItem(JComboBox<String> selector) {
        return selector.isVisible() ? Optional.ofNullable((String) selector.getSelectedItem()) : Optional.empty();
    }

    public static void changeState(JComponent selector, boolean on) {
        selector.setVisible(on);
        selector.setEnabled(on);
    }

    public static DocumentListener createTextFieldListener(Runnable operation) {
        return createTextFieldListener(e -> operation.run());
    }

    public static DocumentListener createTextFieldListener(Consumer<DocumentEvent> operation) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                operation.accept(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                operation.accept(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        };
    }

    public static boolean hasText(JTextField field) {
        return !isEmpty(field.getText());
    }

    public static ActionListener createActionListener(Runnable operation) {
        return createActionListener(e -> operation.run());
    }

    public static ActionListener createActionListener(Consumer<ActionEvent> operation) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                operation.accept(e);
            }
        };
    }

    protected JPanel createContentPanel() {
        JPanel res = new JPanel(new GridBagLayout());
        // input form:
        addSubjectInputRow(res);
        addPredicateInputRow(res);
        addObjectInputRow(res);

        GridBagUtils.addLine(res, 3);

        // result form:
        Stream.of(subjectField, predicateField, objectField).forEach(t -> {
            t.setForeground(Color.GRAY);
            t.setEditable(false);
        });
        GridBagUtils.addSimpleRow(res, SUBJECT, DELIMITER, subjectField, 4);
        GridBagUtils.addSimpleRow(res, PREDICATE, DELIMITER, predicateField, 5);
        GridBagUtils.addSimpleRow(res, OBJECT, DELIMITER, objectField, 6);
        GridBagUtils.addLine(res, 7);
        return res;
    }

    protected void addSubjectInputRow(JPanel res) {
        GridBagUtils.addLabelCell(res, SUBJECT, 0);
        GridBagUtils.addControlCell(res, subjectController, 0);
        GridBagUtils.addIRICell(res, subjectIRI, 0);
    }

    protected void addPredicateInputRow(JPanel res) {
        GridBagUtils.addLabelCell(res, PREDICATE, 1);
        GridBagUtils.addControlCell(res, predicateController, 1);
        GridBagUtils.addIRICell(res, predicateIRI, 1);
        GridBagUtils.addIRICell(res, predicateSystemPropertySelector, 1);
    }

    protected void addObjectInputRow(JPanel res) {
        GridBagUtils.addLabelCell(res, OBJECT, 2);
        GridBagUtils.addControlCell(res, objectController, 2);
        GridBagUtils.addIRICell(res, objectIRI, 2);
        GridBagUtils.addIRICell(res, objectBlanks, 2);
        GridBagUtils.addIRICell(res, literalForm, 2);
    }

    protected void initSubjectConfiguration() {
        subjectController.setSelectedIndex(0);
        subjectController.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeState(subjectIRI, subjectController.getSelectedIndex() == 0);
                boolean blank = subjectController.getSelectedIndex() == 1;
                String txt = blank ? BLANK_NODE_PLACEHOLDER : toIRI.apply(subjectIRI.getText());
                setText(subjectField, txt);
            }
        });
        subjectIRI.getDocument().addDocumentListener(createIRITextAreaListener(subjectIRI, subjectField));
    }

    protected void initPredicateConfiguration() {
        predicateSystemPropertySelector.setSelectedIndex(0);
        predicateSystemPropertySelector.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) predicateSystemPropertySelector.getSelectedItem();
                setText(predicateField, item != null ? pm.expandPrefix(item) : predicateIRI.getText());
            }
        });
        changeState(predicateSystemPropertySelector, false);
        predicateController.setSelectedIndex(0);
        predicateController.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean custom = predicateController.getSelectedIndex() == 0;
                changeState(predicateIRI, custom);
                changeState(predicateSystemPropertySelector, !custom);
                String item = (String) predicateSystemPropertySelector.getSelectedItem();
                String txt = custom ? toIRI.apply(predicateIRI.getText()) : item != null ? pm.expandPrefix(item) : null;
                setText(predicateField, txt);
            }
        });
        predicateIRI.getDocument().addDocumentListener(createIRITextAreaListener(predicateIRI, predicateField));
    }

    protected void initObjectConfiguration() {
        literalForm.setLayout(new BorderLayout());
        literalForm.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        literalForm.add(literalText, BorderLayout.WEST);
        JPanel literalDataPanel = new JPanel();
        literalDataPanel.setLayout(new CardLayout());
        literalForm.add(literalDataPanel, BorderLayout.EAST);
        literalDataPanel.add(literalDatatypes);
        literalDataPanel.add(literalLangs);

        changeState(literalDatatypes, false);
        changeState(literalLangs, false);
        changeState(literalForm, false);
        changeState(objectBlanks, false);

        literalLangs.setSelectedIndex(0);
        literalDatatypes.setSelectedIndex(0);
        if (objectBlanks.getItemCount() != 0) {
            objectBlanks.setSelectedIndex(0);
        }

        Runnable onEvent = () -> setText(objectField, toLiteral.get());
        literalLangs.addActionListener(createActionListener(onEvent));
        literalDatatypes.addActionListener(createActionListener(onEvent));
        literalText.getDocument().addDocumentListener(createTextFieldListener(onEvent));

        objectBlanks.addActionListener(createActionListener(() -> setText(objectField, getSelectedBlankNode().toString())));
        objectIRI.getDocument().addDocumentListener(createIRITextAreaListener(objectIRI, objectField));

        objectController.setSelectedIndex(0);
        objectController.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeState(objectIRI, objectController.isIRI());
                changeState(objectBlanks, objectController.isModelBlank());
                changeState(literalForm, objectController.isLiteral());
                changeState(literalDatatypes, objectController.isTypedLiteral());
                changeState(literalLangs, objectController.isLangLiteral());

                // return back text
                String txt;
                if (objectController.isLiteral()) {
                    txt = toLiteral.get();
                } else if (objectController.isFreshBlank()) {
                    txt = BLANK_NODE_PLACEHOLDER;
                } else if (objectController.isModelBlank()) {
                    txt = getSelectedBlankNode().toString();
                } else {
                    txt = toIRI.apply(objectIRI.getText());
                }
                setText(objectField, txt);
            }
        });
    }

    protected String getLiteralString() {
        String in = literalText.getText();
        if (isEmpty(in)) {
            return null;
        }
        String suffix = null;
        String dt = findSelectedItem(literalDatatypes).orElse(null);
        if (dt != null) {
            suffix = "^^" + pm.expandPrefix(dt);
        }
        String lang = findSelectedItem(literalLangs).orElse(null);
        if (lang != null) {
            if (suffix != null)
                throw new IllegalStateException();
            suffix = "@" + lang;
        }
        String res = "\"" + in.replace("\n", "\\n") + "\"";
        return suffix == null ? res : res + suffix;
    }

    protected String toIRI(String base, String in) {
        if (isEmpty(in)) {
            return null;
        }
        String res = pm.expandPrefix(in);
        try {
            if (URI.create(res).isAbsolute())
                return res;
        } catch (IllegalArgumentException e) {
            // ignore
        }
        if (!isEmpty(base) && !test(base, String::endsWith, "#", "/") && !test(res, String::contains, ":", "#", "/")) {
            return base + "#" + res;
        }
        return res;
    }

    protected DocumentListener createIRITextAreaListener(JTextComponent input, JTextField output) {
        return createTextFieldListener(x -> setText(output, toIRI.apply(input.getText())));
    }

    protected void setText(JTextField field, String txt) {
        field.setText(txt);
        testValid();
    }

    private TripleModel.BNode getSelectedBlankNode() {
        return (TripleModel.BNode) Objects.requireNonNull(objectBlanks.getSelectedItem());
    }

    @Override
    protected boolean isOK() {
        return hasText(subjectField) && hasText(predicateField) && hasText(objectField);
    }

    public Node getSubjectNode() {
        String txt = subjectField.getText();
        if (isEmpty(txt)) {
            throw new IllegalStateException();
        }
        if (subjectController.getSelectedIndex() == 1) {
            return NodeFactory.createBlankNode();
        }
        return NodeFactory.createURI(txt);
    }

    public Node getPredicateNode() {
        String txt = predicateField.getText();
        if (isEmpty(txt)) {
            throw new IllegalStateException();
        }
        return NodeFactory.createURI(txt);
    }

    public Node getObjectNode() {
        String txt = objectField.getText();
        if (isEmpty(txt)) {
            throw new IllegalStateException();
        }
        if (objectController.isLiteral()) {
            txt = literalText.getText();
            if (objectController.isLangLiteral()) {
                String lang = (String) literalLangs.getSelectedItem();
                return NodeFactory.createLiteral(txt, lang);
            }
            if (objectController.isTypedLiteral()) {
                String dt = (String) literalDatatypes.getSelectedItem();
                return NodeFactory.createLiteral(txt, types.getSafeTypeByName(pm.expandPrefix(dt)));
            }
            return NodeFactory.createLiteral(txt);
        }
        if (objectController.isFreshBlank()) {
            return NodeFactory.createBlankNode();
        }
        if (objectController.isModelBlank()) {
            return NodeFactory.createBlankNode(getSelectedBlankNode().getId());
        }
        return NodeFactory.createURI(txt);
    }

    public Triple getTriple() {
        return new Triple(getSubjectNode(), getPredicateNode(), getObjectNode());
    }

    public static class ObjectController extends JComboBox<String> {
        private static final long serialVersionUID = -9198960251639503015L;

        protected ObjectController(Collection<String> items) {
            super(items.toArray(new String[0]));
        }

        public static ObjectController createObjectController(boolean withBNodes) {
            ArrayList<String> list = new ArrayList<>(Arrays.asList(IRI_CONTROL_OPTION
                    , FRESH_BLANK_NODE_CONTROL_OPTION
                    , MODEL_BLANK_NODE_CONTROL_OPTION
                    , PLAIN_LITERAL_CONTROL_OPTION
                    , TYPED_LITERAL_CONTROL_OPTION
                    , LANG_LITERAL_CONTROL_OPTION));
            if (!withBNodes) {
                list.remove(2);
            }
            return new ObjectController(list);
        }

        public boolean hasModelBlanks() {
            return getItemCount() == 6;
        }

        public boolean isIRI() {
            return getSelectedIndex() == 0;
        }

        public boolean isFreshBlank() {
            return getSelectedIndex() == 1;
        }

        public boolean isModelBlank() {
            return hasModelBlanks() && getSelectedIndex() == 2;
        }

        public boolean isLiteral() {
            return getSelectedIndex() > (hasModelBlanks() ? 2 : 1);
        }

        public boolean isTypedLiteral() {
            return getSelectedIndex() == (hasModelBlanks() ? 4 : 3);
        }

        public boolean isLangLiteral() {
            return getSelectedIndex() == (hasModelBlanks() ? 5 : 4);
        }


    }
}
