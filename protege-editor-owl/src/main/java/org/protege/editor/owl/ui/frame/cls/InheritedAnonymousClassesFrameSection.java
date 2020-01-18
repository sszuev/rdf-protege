package org.protege.editor.owl.ui.frame.cls;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences.OptionalInferenceTask;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Feb-2007<br><br>
 */
public class InheritedAnonymousClassesFrameSection extends AbstractOWLFrameSection<OWLClass, OWLClassAxiom, OWLClassExpression> {

    private static final String LABEL = "SubClass Of (Anonymous Ancestor)";

    private final Set<OWLClass> processedClasses = new HashSet<>();

    public InheritedAnonymousClassesFrameSection(OWLEditorKit editorKit, OWLFrame<? extends OWLClass> frame) {
        super(editorKit, LABEL, "Anonymous Ancestor Class", frame);
    }

    @Override
    protected OWLSubClassOfAxiom createAxiom(OWLClassExpression object) {
        return null; // canAdd() = false
    }

    @Override
    public OWLObjectEditor<OWLClassExpression> getObjectEditor() {
        return null; // canAdd() = false
    }

    @Override
    protected void refill(OWLOntology o) {
        Set<OWLClass> clses = getOWLModelManager().getOWLHierarchyManager()
                .getOWLClassHierarchyProvider().getAncestors(getRootObject());
        clses.remove(getRootObject());
        OWLEditorKit kit = getOWLEditorKit();
        for (OWLClass cls : clses) {
            Stream.concat(o.subClassAxiomsForSubClass(cls).filter(ax -> ax.getSuperClass().isAnonymous()),
                    o.equivalentClassesAxioms(cls))
                    .forEach(ax -> addRow(new InheritedAnonymousClassesFrameSectionRow(kit, this, o, cls, ax)));
            processedClasses.add(cls);
        }
    }

    @Override
    protected void refillInferred() {
        getOWLModelManager().getReasonerPreferences().executeTask(OptionalInferenceTask.SHOW_INFERRED_SUPER_CLASSES,
                this::refillInferredDoIt);
    }

    private void refillInferredDoIt() {
        OWLReasoner reasoner = getOWLModelManager().getReasoner();
        if (!reasoner.isConsistent()) {
            return;
        }
        if (!reasoner.isSatisfiable(getRootObject())) {
            return;
        }
        OWLEditorKit kit = getOWLEditorKit();
        OWLDataFactory df = getOWLDataFactory();
        OWLOntology active = getOWLModelManager().getActiveOntology();
        OWLClass root = getRootObject();
        getReasoner().getSuperClasses(getRootObject(), true).entities()
                .filter(x -> !processedClasses.contains(x) && !x.equals(root))
                .forEach(c -> active.importsClosure().forEach(o -> {
                    o.subClassAxiomsForSubClass(c)
                            .filter(x -> x.getSuperClass().isAnonymous())
                            .forEach(x -> {
                                OWLSubClassOfAxiom entailed = df.getOWLSubClassOfAxiom(root, x.getSuperClass());
                                addRow(new InheritedAnonymousClassesFrameSectionRow(kit,
                                        InheritedAnonymousClassesFrameSection.this, null, c, entailed));
                            });

                    o.equivalentClassesAxioms(c)
                            .forEach(ax -> {
                                Set<OWLClassExpression> descs = ax.classExpressions().collect(Collectors.toSet());
                                descs.remove(root);
                                for (OWLClassExpression superCls : descs) {
                                    if (!superCls.isAnonymous()) {
                                        continue;
                                    }
                                    OWLSubClassOfAxiom entailed = df.getOWLSubClassOfAxiom(root, superCls);
                                    addRow(new InheritedAnonymousClassesFrameSectionRow(kit,
                                            InheritedAnonymousClassesFrameSection.this, null, c, entailed));
                                }
                            });
                }));
    }

    @Override
    public boolean canAdd() {
        return false;
    }

    @Override
    protected boolean isResettingChange(OWLOntologyChange change) {
        return change.isAxiomChange() && (change.getAxiom() instanceof OWLSubClassOfAxiom || change.getAxiom() instanceof OWLEquivalentClassesAxiom);
    }

    @Override
    protected void clear() {
        processedClasses.clear();
    }

    @Override
    public Comparator<OWLFrameSectionRow<OWLClass, OWLClassAxiom, OWLClassExpression>> getRowComparator() {
        return null;
    }
}
