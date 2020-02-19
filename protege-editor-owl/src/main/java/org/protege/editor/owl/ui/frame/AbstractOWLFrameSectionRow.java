package org.protege.editor.owl.ui.frame;

import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.editor.OWLObjectEditorHandler;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 *
 * @param <R> - anything
 * @param <A> - {@link OWLAxiom}
 * @param <E> - anything
 */
public abstract class AbstractOWLFrameSectionRow<R, A extends OWLAxiom, E>
        implements OWLFrameSectionRow<R, A, E>, OWLObjectEditorHandler<E> {

    public static final String DEFAULT_DELIMETER = ", ";
    public static final String DEFAULT_PREFIX = "";
    public static final String DEFAULT_SUFFIX = "";

    private final OWLEditorKit owlEditorKit;
    private final OWLOntology ontology;
    private final R rootObject;
    protected final A axiom;
    private final OWLFrameSection<R, A, E> section;

    private Object userObject;

    protected AbstractOWLFrameSectionRow(OWLEditorKit owlEditorKit,
                                         OWLFrameSection<R, A, E> section,
                                         OWLOntology ontology,
                                         R rootObject,
                                         A axiom) {
        this.owlEditorKit = owlEditorKit;
        this.section = section;
        this.ontology = ontology;
        this.rootObject = rootObject;
        this.axiom = axiom;
    }

    @Override
    public OWLFrameSection<R, A, E> getFrameSection() {
        return section;
    }

    public R getRootObject() {
        return rootObject;
    }

    public boolean isFixedHeight() {
        return false;
    }

    @Override
    public final OWLObjectEditor<E> getEditor() {
        OWLObjectEditor<E> editor = getObjectEditor();
        if (editor != null) {
            editor.setHandler(this);
        }
        return editor;
    }

    protected abstract OWLObjectEditor<E> getObjectEditor();

    @Override
    public boolean checkEditorResults(OWLObjectEditor<E> editor) {
        return true;
    }

    @Override
    public void handleEditingFinished(Set<E> editedObjects) {
        if (editedObjects.isEmpty()) {
            return;
        }
        OWLAxiom newAxiom = createAxiom(editedObjects.iterator().next());
        // the editor should protect from this, but just in case
        if (newAxiom == null) {
            return;
        }
        A oldAxiom = getAxiom();
        if (oldAxiom.isAnnotated()) {
            newAxiom = newAxiom.getAnnotatedAxiom(oldAxiom.annotations());
        }
        List<OWLOntologyChange> changes = new ArrayList<>();
        OWLOntology ontology = getOntology();
        if (ontology != null) {
            changes.add(new RemoveAxiom(ontology, oldAxiom));
            changes.add(new AddAxiom(ontology, newAxiom));
        } else {
            OWLOntology activeOntology = getOWLModelManager().getActiveOntology();
            changes.add(new AddAxiom(activeOntology, newAxiom));
        }
        getOWLModelManager().applyChanges(changes);
    }

    protected abstract A createAxiom(E editedObject);

    /**
     * This row represents an assertion in a particular ontology.
     * This gets the ontology that the assertion belongs to.
     *
     * @return {@link OWLOntology}
     */
    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    public OWLModelManager getOWLModelManager() {
        return owlEditorKit.getModelManager();
    }

    public OWLDataFactory getOWLDataFactory() {
        return getOWLModelManager().getOWLDataFactory();
    }

    @Override
    public OWLOntologyManager getOWLOntologyManager() {
        return getOWLModelManager().getOWLOntologyManager();
    }

    public OWLEditorKit getOWLEditorKit() {
        return owlEditorKit;
    }

    /**
     * Gets the root object of the frame that this row belongs to.
     *
     * @return {@link R}
     */
    @Override
    public R getRoot() {
        return rootObject;
    }

    /**
     * Gets the object that the row holds.
     *
     * @return {@link A}
     */
    @Override
    public A getAxiom() {
        return axiom;
    }

    @Override
    public boolean canAcceptDrop(List<OWLObject> objects) {
        return false;
    }

    @Override
    public boolean dropObjects(List<OWLObject> objects) {
        return false;
    }

    @Override
    public String getTooltip() {
        if (ontology == null) {
            return "Inferred";
        }
        UIHelper helper = new UIHelper(owlEditorKit);
        StringBuilder sb = new StringBuilder("<html>\n\t<body>\n\t\tAsserted in: ");
        sb.append(helper.getHTMLOntologyList(Collections.singleton(ontology)));
        A axiom = getAxiom();
        if (axiom.isAnnotated()) {
            OWLModelManager protegeManager = getOWLModelManager();
            sb.append("\n\t\t<p>Annotations:");
            sb.append("\n\t\t<dl>");
            axiom.annotations().forEach(annotation -> {
                sb.append("\n\t\t\t<dt>");
                sb.append(protegeManager.getRendering(annotation.getProperty()));
                sb.append("</dt>\n\t\t\t<dd>");
                sb.append(protegeManager.getRendering(annotation.getValue()));
                sb.append("</dd>");
            });
            sb.append("\n\t\t</dl>\n\t</p>\n");
        }
        sb.append("\t</body>\n</html>");
        return sb.toString();
    }

    public String toString() {
        return getRendering();
    }


    /**
     * Deletes this row.
     * This will alter the underlying model of which this row is a representation.
     * This method should not be called if the <code>isEditable</code> method returns <code>false</code>.
     *
     * @return {@code List}
     */
    @Override
    public List<? extends OWLOntologyChange> getDeletionChanges() {
        if (!isDeleteable()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new RemoveAxiom(getOntology(), getAxiom()));
    }

    /**
     * Determines if this row is editable.
     * If a row is editable then it may be deleted/removed and column values may be edited.
     *
     * @return <code>true</code> if the row is editable, <code>false</code> if the row is not editable.
     */
    @Override
    public boolean isEditable() {
        return getOntology() != null;
    }

    public String getPrefix() {
        return DEFAULT_PREFIX;
    }

    public String getDelimeter() {
        return DEFAULT_DELIMETER;
    }

    public String getSuffix() {
        return DEFAULT_SUFFIX;
    }

    protected Object getObjectRendering(OWLObject ob) {
        return getOWLModelManager().getRendering(ob);
    }

    @Override
    public boolean isInferred() {
        return ontology == null;
    }

    /**
     * Gets the rendering of the value of a particular column.
     *
     * @return The <code>String</code> representation of the column value.
     */
    public String getRendering() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPrefix());
        for (Iterator<? extends OWLObject> it = getManipulatableObjects().iterator(); it.hasNext(); ) {
            OWLObject obj = it.next();
            sb.append(getObjectRendering(obj));
            if (it.hasNext()) {
                sb.append(getDelimeter());
            }
        }
        sb.append(getSuffix());
        return sb.toString();
    }

    @Override
    public boolean isDeleteable() {
        return isEditable();
    }

    @Override
    public void handleEdit() {
    }

    @Override
    public boolean handleDelete() {
        return false;
    }

    public List<MListButton> getAdditionalButtons() {
        return Collections.emptyList();
    }
}
