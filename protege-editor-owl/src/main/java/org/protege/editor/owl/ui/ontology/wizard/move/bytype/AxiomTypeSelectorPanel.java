package org.protege.editor.owl.ui.ontology.wizard.move.bytype;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.core.ui.list.RemovableObjectList;
import org.protege.editor.owl.ui.ontology.wizard.move.MoveAxiomsKitConfigurationPanel;
import org.semanticweb.owlapi.model.AxiomType;

import javax.swing.*;
import java.awt.*;
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
 * Date: Nov 28, 2008<br><br>
 */
public class AxiomTypeSelectorPanel extends MoveAxiomsKitConfigurationPanel {

    private final MoveAxiomsByTypeKit kit;
    private MList typeSource;
    private RemovableObjectList<AxiomType<?>> typeSelection;

    public AxiomTypeSelectorPanel(MoveAxiomsByTypeKit kit) {
        this.kit = kit;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialise() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        typeSource = new MList();
        List<Object> data = createTypesByTypeList();

        typeSource.setListData(data.toArray());

        typeSelection = new RemovableObjectList<>();
        typeSelection.setCellRenderer(new DefaultListCellRenderer() {
            @SuppressWarnings("rawtypes")
            @Override
            public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
                if (o instanceof RemovableObjectList.RemovableObjectListItem) {
                    o = ((RemovableObjectList.RemovableObjectListItem) o).getObject();
                }
                return super.getListCellRendererComponent(jList, o, i, b, b1);
            }
        });

        JButton button = new JButton(new AbstractAction(">>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSelectedItems();
            }
        });

        JComponent buttonPanel = new Box(BoxLayout.PAGE_AXIS);
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(button);
        buttonPanel.add(Box.createVerticalGlue());

        add(Box.createHorizontalGlue());
        add(new JScrollPane(typeSource));
        add(buttonPanel);
        add(new JScrollPane(typeSelection));
        add(Box.createHorizontalGlue());
    }

    private void addSelectedItems() {
        Set<AxiomType<?>> selectedTypes = new HashSet<>();
        for (Object o : typeSource.getSelectedValuesList()) {
            if (o instanceof TypeItem) {
                AxiomType<?> axiomType = ((TypeItem) o).getType();
                selectedTypes.add(axiomType);
            }
        }
        selectedTypes.removeAll(typeSelection.getListItems()); // don't allow duplicates
        typeSelection.addObject(selectedTypes);
    }

    private Set<AxiomType<?>> getSelection() {
        return new HashSet<>(typeSelection.getListItems());
    }

    @Override
    public void dispose() {
        // do nothing
    }

    @Override
    public String getID() {
        return getClass().getName();
    }

    @Override
    public String getTitle() {
        return "Axioms by type";
    }

    @Override
    public String getInstructions() {
        return "Please select the types of axiom you would like to move/copy.";
    }

    @Override
    public void update() {
    }

    @Override
    public void commit() {
        kit.setTypes(getSelection());
    }

    private List<Object> createTypesByTypeList() {
        List<Object> data = new ArrayList<>();
        data.add(createMListSectionHeader("Class Axioms"));
        data.add(new TypeItem(AxiomType.EQUIVALENT_CLASSES));
        data.add(new TypeItem(AxiomType.SUBCLASS_OF));
        data.add(new TypeItem(AxiomType.DISJOINT_CLASSES));

        data.add(createMListSectionHeader("Object Property Axioms"));
        data.add(new TypeItem(AxiomType.SUB_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.EQUIVALENT_OBJECT_PROPERTIES));
        data.add(new TypeItem(AxiomType.DISJOINT_OBJECT_PROPERTIES));
        data.add(new TypeItem(AxiomType.INVERSE_OBJECT_PROPERTIES));
        data.add(new TypeItem(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.OBJECT_PROPERTY_DOMAIN));
        data.add(new TypeItem(AxiomType.OBJECT_PROPERTY_RANGE));
        data.add(new TypeItem(AxiomType.FUNCTIONAL_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.TRANSITIVE_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.SYMMETRIC_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.ASYMMETRIC_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.REFLEXIVE_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY));
        data.add(new TypeItem(AxiomType.SUB_PROPERTY_CHAIN_OF));

        data.add(createMListSectionHeader("Data Property Axioms"));
        data.add(new TypeItem(AxiomType.SUB_DATA_PROPERTY));
        data.add(new TypeItem(AxiomType.EQUIVALENT_DATA_PROPERTIES));
        data.add(new TypeItem(AxiomType.DISJOINT_DATA_PROPERTIES));
        data.add(new TypeItem(AxiomType.DATA_PROPERTY_DOMAIN));
        data.add(new TypeItem(AxiomType.DATA_PROPERTY_RANGE));
        data.add(new TypeItem(AxiomType.FUNCTIONAL_DATA_PROPERTY));

        data.add(createMListSectionHeader("Individual Axioms"));
        data.add(new TypeItem(AxiomType.CLASS_ASSERTION));
        data.add(new TypeItem(AxiomType.DIFFERENT_INDIVIDUALS));
        data.add(new TypeItem(AxiomType.SAME_INDIVIDUAL));
        data.add(new TypeItem(AxiomType.OBJECT_PROPERTY_ASSERTION));
        data.add(new TypeItem(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION));
        data.add(new TypeItem(AxiomType.DATA_PROPERTY_ASSERTION));
        data.add(new TypeItem(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION));

        data.add(createMListSectionHeader("Annotation Axioms"));
        data.add(new TypeItem(AxiomType.ANNOTATION_ASSERTION));

        data.add(createMListSectionHeader("Other Axioms"));
        data.add(new TypeItem(AxiomType.SWRL_RULE));
        data.add(new TypeItem(AxiomType.DECLARATION));
        data.add(new TypeItem(AxiomType.DISJOINT_UNION));

        return data;
    }

    private MListSectionHeader createMListSectionHeader(String name) {
        return new MListSectionHeader() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean canAdd() {
                return false;
            }
        };
    }

    static class TypeItem implements MListItem {

        private final AxiomType<?> type;

        TypeItem(AxiomType<?> type) {
            this.type = type;
        }

        public AxiomType<?> getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public void handleEdit() {
            // do nothing
        }

        @Override
        public boolean isDeleteable() {
            return false;
        }

        @Override
        public boolean handleDelete() {
            return false;
        }

        @Override
        public String getTooltip() {
            return null;
        }
    }
}
