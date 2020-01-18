package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by @ssz on 18.01.2020.
 */
public class ClassHierarchyTest extends HierarchyTestBase<OWLClass> {
    @Override
    protected AbstractOWLObjectHierarchyProvider<OWLClass> create(OWLOntologyManager m) {
        return new AssertedClassHierarchyProvider(m);
    }

    @Override
    protected OWLClass getRootObject(OWLDataFactory df) {
        return df.getOWLThing();
    }

    @Override
    protected OWLClass createChildObject(OWLDataFactory df) {
        return df.getOWLClass("C");
    }

    @Override
    protected OWLAxiom createSubObjectAxiom(OWLDataFactory df, OWLClass sub, OWLClass top) {
        return df.getOWLSubClassOfAxiom(sub, top);
    }

    @Override
    protected int getDefaultRootCount() {
        return 1;
    }
}
