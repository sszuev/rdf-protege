package org.protege.editor.owl.ui.list;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.AnnotationContainer;
import org.protege.editor.owl.model.entity.AnnotationPropertyComparator;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.editor.OWLAnnotationEditor;
import org.protege.editor.owl.ui.renderer.OWLAnnotationCellRenderer2;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/*
 * Copyright (C) 2007, University of Manchester
 *
 *
 */

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 8, 2009<br><br>
 * <p>
 * Don't want to write this again - Matthew is adding an interface that Axiom and OWLOntology can implement
 * that allows us to get the annotations.
 */
public abstract class AbstractAnnotationsList<O extends AnnotationContainer> extends MList<Object> {

    private static final String HEADER_TEXT = "Annotations";
    private final OWLEditorKit editorKit;
    private final MListSectionHeader header = new MListSectionHeader() {

        @Override
        public String getName() {
            return HEADER_TEXT;
        }

        @Override
        public boolean canAdd() {
            return true;
        }
    };
    private final OWLOntologyChangeListener ontChangeListener = this::handleOntologyChanges;

    private OWLAnnotationEditor editor;
    private O root;

    public AbstractAnnotationsList(OWLEditorKit eKit) {
        this.editorKit = eKit;
        uncheckedSetCellRenderer(new OWLAnnotationCellRenderer2(eKit));
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleEdit();
                }
            }
        };
        addMouseListener(mouseListener);
        eKit.getOWLModelManager().addOntologyChangeListener(ontChangeListener);
    }

    protected abstract List<OWLOntologyChange> getAddChanges(OWLAnnotation annot);

    protected abstract List<OWLOntologyChange> getReplaceChanges(OWLAnnotation oldAnnotation, OWLAnnotation newAnnotation);

    protected abstract List<OWLOntologyChange> getDeleteChanges(OWLAnnotation annot);

    protected abstract void handleOntologyChanges(List<? extends OWLOntologyChange> changes);

    @Override
    protected void handleAdd() {
        // don't need to check the section as only the direct imports can be added
        if (editor == null) {
            editor = new OWLAnnotationEditor(editorKit);
        }

        editor.setEditedObject(null);

        UIHelper uiHelper = new UIHelper(editorKit);
        int ret = uiHelper.showValidatingDialog("Create Annotation", editor.getEditorComponent(), null);

        if (ret == JOptionPane.OK_OPTION) {
            OWLAnnotation annot = editor.getEditedObject();
            if (annot != null) {
                editorKit.getModelManager().applyChanges(getAddChanges(annot));
            }
        }
    }

    protected OWLOntology getActiveOntology() {
        return editorKit.getOWLModelManager().getActiveOntology();
    }

    public void setRootObject(O root) {
        this.root = root;
        refill(root);
    }

    protected void refill(O root) {
        List<Object> data = new ArrayList<>();

        data.add(header);

        if (root != null) {
            List<OWLAnnotation> annotations = new ArrayList<>(root.getAnnotations());
            Comparator<OWLObject> owlObjectComparator = editorKit.getOWLModelManager().getOWLObjectComparator();
            AnnotationPropertyComparator annotationPropertyComparator =
                    AnnotationPropertyComparator.withDefaultOrdering(owlObjectComparator);
            annotations.sort((a1, a2) -> {
                int propComp = annotationPropertyComparator.compare(a1.getProperty(), a2.getProperty());
                if (propComp != 0) {
                    return propComp;
                }
                return owlObjectComparator.compare(a1.getValue(), a2.getValue());
            });
            for (OWLAnnotation annot : annotations) {
                data.add(new AnnotationsListItem(annot));
            }
        }

        setListData(data.toArray());
        revalidate();
    }


    public O getRoot() {
        return root;
    }

    protected void refresh() {
        setRootObject(root);
    }

    public void dispose() {
        editorKit.getOWLModelManager().removeOntologyChangeListener(ontChangeListener);
        if (editor != null) {
            editor.dispose();
            editor = null;
        }
    }

    public class AnnotationsListItem implements MListItem {

        private final OWLAnnotation annotation;

        public AnnotationsListItem(OWLAnnotation annotation) {
            this.annotation = annotation;
        }

        public OWLAnnotation getAnnotation() {
            return annotation;
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public void handleEdit() {
            // don't need to check the section as only the direct imports can be added
            if (editor == null) {
                editor = new OWLAnnotationEditor(editorKit);
            }
            editor.setEditedObject(annotation);
            UIHelper uiHelper = new UIHelper(editorKit);
            int ret = uiHelper.showValidatingDialog("Ontology Annotation", editor.getEditorComponent(), null);

            if (ret == JOptionPane.OK_OPTION) {
                OWLAnnotation newAnnotation = editor.getEditedObject();
                if (newAnnotation != null && !newAnnotation.equals(annotation)) {
                    List<OWLOntologyChange> changes = getReplaceChanges(annotation, newAnnotation);
                    editorKit.getModelManager().applyChanges(changes);
                }
            }
        }

        @Override
        public boolean isDeleteable() {
            return true;
        }

        @Override
        public boolean handleDelete() {
            List<OWLOntologyChange> changes = getDeleteChanges(annotation);
            editorKit.getModelManager().applyChanges(changes);
            return true;
        }

        @Override
        public String getTooltip() {
            return "";
        }
    }
}
