package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.util.OWLComponentFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Feb-2007<br><br>
 */
public class InheritedAnonymousClassesFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLClass, OWLClassAxiom, OWLClassExpression> {

    public InheritedAnonymousClassesFrameSectionRow(OWLEditorKit owlEditorKit,
                                                    OWLFrameSection<OWLClass, OWLClassAxiom, OWLClassExpression> section,
                                                    OWLOntology ontology,
                                                    OWLClass rootObject,
                                                    OWLClassAxiom axiom) {
        super(owlEditorKit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        OWLClassAxiom axiom = getAxiom();
        OWLComponentFactory factory = getOWLEditorKit().getWorkspace().getOWLComponentFactory();
        if (axiom instanceof OWLSubClassOfAxiom) {
            OWLClassExpression superCls = ((OWLSubClassOfAxiom) axiom).getSuperClass();
            return factory.getOWLClassDescriptionEditor(superCls, AxiomType.SUBCLASS_OF);
        }
        Set<OWLClassExpression> res = ((OWLEquivalentClassesAxiom) axiom).classExpressions().collect(Collectors.toSet());
        res.remove(getRootObject());
        OWLClassExpression desc;
        if (res.isEmpty()) {
            // in the weird case that something is asserted equiv to itself
            desc = getRootObject();
        } else {
            desc = res.iterator().next();
        }
        return factory.getOWLClassDescriptionEditor(desc, AxiomType.EQUIVALENT_CLASSES);

    }

    @Override
    protected OWLClassAxiom createAxiom(OWLClassExpression editedObject) {
        OWLDataFactory factory = getOWLDataFactory();
        if (getAxiom() instanceof OWLSubClassOfAxiom) {
            return factory.getOWLSubClassOfAxiom(getRoot(), editedObject);
        }
        return factory.getOWLEquivalentClassesAxiom(CollectionFactory.createSet(getRoot(), editedObject));

    }

    @Override
    public List<OWLClassExpression> getManipulatableObjects() {
        OWLClassAxiom axiom = getAxiom();
        if (axiom instanceof OWLSubClassOfAxiom) {
            return Collections.singletonList(((OWLSubClassOfAxiom) axiom).getSuperClass());
        }
        Set<OWLClassExpression> res = ((OWLEquivalentClassesAxiom) axiom).classExpressions().collect(Collectors.toSet());
        res.remove(getRootObject());
        if (res.isEmpty()) {
            // in the weird case that something is asserted equiv to itself
            OWLClassExpression cls = getRootObject();
            return Collections.singletonList(cls);
        }
        return Collections.singletonList(res.iterator().next());
    }

    @Override
    public String getTooltip() {
        return "Inherited from " + getOWLModelManager().getRendering(getRootObject());
    }
}
