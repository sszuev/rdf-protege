package org.protege.editor.owl.model.selection;


import org.protege.editor.owl.model.util.OWLAxiomInstance;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: Mar 21, 2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLSelectionModelImpl implements OWLSelectionModel {

    private static final Logger logger = LoggerFactory.getLogger(OWLSelectionModelImpl.class);


    private final List<OWLSelectionModelListener> listeners = new ArrayList<>();

    private Object selectedObject;

    private OWLEntity lastSelectedEntity;

    private OWLClass lastSelectedClass;

    private OWLDataProperty lastSelectedDataProperty;

    private OWLObjectProperty lastSelectedObjectProperty;

    private OWLAnnotationProperty lastSelectedAnnotationProperty;

    private OWLNamedIndividual lastSelectedIndividual;

    private OWLDatatype lastSelectedDatatype;

    private OWLAxiomInstance lastSelectedAxiomInstance;

    private final OWLEntityVisitor updateVisitor = new OWLEntityVisitor() {
        @Override
        public void visit(@Nonnull OWLClass cls) {
            lastSelectedClass = cls;
        }

        @Override
        public void visit(@Nonnull OWLObjectProperty property) {
            lastSelectedObjectProperty = property;
        }

        @Override
        public void visit(@Nonnull OWLDataProperty property) {
            lastSelectedDataProperty = property;
        }

        @Override
        public void visit(@Nonnull OWLAnnotationProperty owlAnnotationProperty) {
            lastSelectedAnnotationProperty = owlAnnotationProperty;
        }

        @Override
        public void visit(@Nonnull OWLNamedIndividual individual) {
            lastSelectedIndividual = individual;
        }

        @Override
        public void visit(@Nonnull OWLDatatype dataType) {
            lastSelectedDatatype = dataType;
        }
    };

    private final OWLEntityVisitor clearVisitor = new OWLEntityVisitor() {
        @Override
        public void visit(@Nonnull OWLClass cls) {
            if (lastSelectedClass != null) {
                if (lastSelectedClass.equals(cls)) {
                    lastSelectedClass = null;
                    fireSelectionChanged();
                }
            }
        }

        @Override
        public void visit(@Nonnull OWLObjectProperty property) {
            if (lastSelectedObjectProperty != null) {
                if (lastSelectedObjectProperty.equals(property)) {
                    lastSelectedObjectProperty = null;
                    fireSelectionChanged();
                }
            }
        }

        @Override
        public void visit(@Nonnull OWLDataProperty property) {
            if (lastSelectedDataProperty != null) {
                if (lastSelectedDataProperty.equals(property)) {
                    lastSelectedDataProperty = null;
                    fireSelectionChanged();
                }
            }
        }

        @Override
        public void visit(@Nonnull OWLAnnotationProperty property) {
            if (lastSelectedAnnotationProperty != null) {
                if (lastSelectedAnnotationProperty.equals(property)) {
                    lastSelectedAnnotationProperty = null;
                    fireSelectionChanged();
                }
            }
        }

        @Override
        public void visit(@Nonnull OWLNamedIndividual individual) {
            if (lastSelectedIndividual != null) {
                if (lastSelectedIndividual.equals(individual)) {
                    lastSelectedIndividual = null;
                    fireSelectionChanged();
                }
            }
        }

        @Override
        public void visit(@Nonnull OWLDatatype dataType) {
            if (lastSelectedDatatype != null) {
                if (lastSelectedDatatype.equals(dataType)) {
                    lastSelectedDatatype = null;
                    fireSelectionChanged();
                }
            }
        }
    };

    public OWLSelectionModelImpl() {
    }

    @Override
    public void addListener(@Nonnull OWLSelectionModelListener listener) {
        listeners.add(checkNotNull(listener));
    }

    @Override
    public void removeListener(@Nonnull OWLSelectionModelListener listener) {
        listeners.remove(checkNotNull(listener));
    }

    @Nullable
    public OWLObject getSelectedOWLObject() {
        return selectedObject instanceof OWLObject ? (OWLObject) selectedObject : null;
    }

    public Object getSelectedObject() {
        return selectedObject;
    }

    @Override
    public void setSelectedObject(@Nullable Object object) {
        if (object == null) {
            if (selectedObject != null) {
                updateSelectedObject(null);
            }
        } else {
            if (selectedObject == null) {
                updateSelectedObject(object);
            } else if (!selectedObject.equals(object)) {
                updateSelectedObject(object);
            }
        }
    }

    private void updateSelectedObject(Object selObj) {
        selectedObject = selObj;
        updateLastSelection();
        logger.debug("Set the selected object to: {}", selObj);
        fireSelectionChanged();
    }

    @Override
    public OWLEntity getSelectedEntity() {
        return lastSelectedEntity;
    }

    private void fireSelectionChanged() {
        for (OWLSelectionModelListener listener : new ArrayList<>(listeners)) {
            try {
                listener.selectionChanged();
            }
            catch (Exception e) {
                logger.warn("A selection model listener threw an error whilst handling a selection changed event: {}", e.getMessage());
            }
        }
    }

    @Override
    public void setSelectedEntity(@Nullable OWLEntity entity) {
        setSelectedObject(entity);
    }

    @Override
    public void setSelectedAxiom(@Nonnull OWLAxiomInstance axiomInstance) {
        lastSelectedAxiomInstance = axiomInstance;
        setSelectedObject(axiomInstance.getAxiom());
    }

    @Override
    public void clearLastSelectedEntity(@Nonnull OWLEntity entity) {
        entity.accept(clearVisitor);
        //noinspection PointlessNullCheck
        if (lastSelectedEntity != null && entity.equals(lastSelectedEntity)) {
            lastSelectedEntity = null;
            fireSelectionChanged();
        }
    }

    private void updateLastSelection() {
        if (selectedObject == null) {
            return;
        }
        if (selectedObject instanceof OWLEntity) {
            lastSelectedEntity = (OWLEntity)selectedObject;

            lastSelectedEntity.accept(updateVisitor);
            lastSelectedAxiomInstance = null; // unlikely we will want the axiom selection to still be valid
        }
    }


    @Override
    public OWLClass getLastSelectedClass() {
        return lastSelectedClass;
    }

    @Override
    public OWLObjectProperty getLastSelectedObjectProperty() {
        return lastSelectedObjectProperty;
    }


    @Override
    public OWLDataProperty getLastSelectedDataProperty() {
        return lastSelectedDataProperty;
    }

    @Override
    public OWLAnnotationProperty getLastSelectedAnnotationProperty() {
        return lastSelectedAnnotationProperty;
    }

    @Override
    public OWLNamedIndividual getLastSelectedIndividual() {
        return lastSelectedIndividual;
    }

    @Override
    public OWLDatatype getLastSelectedDatatype() {
        return lastSelectedDatatype;
    }

    @Override
    public OWLAxiomInstance getLastSelectedAxiomInstance() {
        return lastSelectedAxiomInstance;
    }

    @Override
    public String toString() {
        return toStringHelper("OWLSelectionModelImpl")
                .add("selectedObject", selectedObject)
                .add("lastSelectedEntity", lastSelectedEntity)
                .add("lastSelectedClass", lastSelectedClass)
                .add("lastSelectedObjectProperty", lastSelectedObjectProperty)
                .add("lastSelectedDataProperty", lastSelectedDataProperty)
                .add("lastSelectedAnnotationProperty", lastSelectedAnnotationProperty)
                .add("lastSelectedNamedIndividual", lastSelectedIndividual)
                .add("lastSelectedDatatype", lastSelectedDatatype)
                .add("lastSelectedAxiom", lastSelectedAxiomInstance)
                .toString();
    }
}
