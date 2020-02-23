package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrameSection;
import org.protege.editor.owl.ui.util.OWLComponentFactory;
import org.semanticweb.owlapi.model.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Feb-2007<br><br>
 */
public class InheritedAnonymousClassesFrameSectionRow
        extends AbstractOWLFrameSectionRow<OWLClass, OWLClassAxiom, OWLClassExpression> {

    public InheritedAnonymousClassesFrameSectionRow(OWLEditorKit kit,
                                                    OWLFrameSection<OWLClass, OWLClassAxiom, OWLClassExpression> section,
                                                    OWLOntology ontology,
                                                    OWLClass rootObject,
                                                    OWLClassAxiom axiom) {
        super(kit, section, ontology, rootObject, axiom);
    }

    @Override
    protected OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        OWLClassAxiom axiom = getAxiom();
        OWLComponentFactory factory = getOWLComponentFactory();
        if (axiom instanceof OWLSubClassOfAxiom) {
            OWLClassExpression superCls = ((OWLSubClassOfAxiom) axiom).getSuperClass();
            return factory.getOWLClassDescriptionEditor(superCls, AxiomType.SUBCLASS_OF);
        }
        Set<OWLClassExpression> res = ((OWLEquivalentClassesAxiom) axiom).classExpressions().collect(Collectors.toSet());
        res.remove(getRoot());
        OWLClassExpression desc;
        if (res.isEmpty()) {
            // in the weird case that something is asserted equiv to itself
            desc = getRoot();
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
        return factory.getOWLEquivalentClassesAxiom(getRoot(), editedObject);
    }

    @Override
    public Stream<OWLClassExpression> manipulatableObjects() {
        OWLClassAxiom axiom = getAxiom();
        if (axiom instanceof OWLSubClassOfAxiom) {
            return Stream.of(((OWLSubClassOfAxiom) axiom).getSuperClass());
        }
        OWLClass root = getRoot();
        Set<OWLClassExpression> res = ((OWLEquivalentClassesAxiom) axiom).classExpressions().collect(Collectors.toSet());
        res.remove(root);
        if (res.isEmpty()) {
            // in the weird case that something is asserted equiv to itself
            return Stream.of(root);
        }
        return Stream.of(res.iterator().next());
    }

    @Override
    public String getTooltip() {
        return "Inherited from " + getOWLModelManager().getRendering(getRoot());
    }
}
