package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by @ssz on 18.01.2020.
 */
public class AnnotationHierarchyTest extends HierarchyTestBase<OWLAnnotationProperty> {

    @Override
    protected AbstractOWLObjectHierarchyProvider<OWLAnnotationProperty> create(OWLOntologyManager m) {
        return new OWLAnnotationPropertyHierarchyProvider(m);
    }

    @Override
    protected OWLAnnotationProperty getRootObject(OWLDataFactory df) {
        return df.getRDFSComment();
    }

    @Override
    protected OWLAnnotationProperty createChildObject(OWLDataFactory df) {
        return df.getOWLAnnotationProperty("P");
    }

    @Override
    protected OWLAxiom createSubObjectAxiom(OWLDataFactory df, OWLAnnotationProperty p, OWLAnnotationProperty top) {
        return df.getOWLSubAnnotationPropertyOfAxiom(p, top);
    }

    @Override
    protected int getDefaultRootCount() {
        return 9;
    }
}
