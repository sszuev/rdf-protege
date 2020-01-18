package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by @ssz on 18.01.2020.
 */
public class DataPropertyHierarchyTest extends HierarchyTestBase<OWLDataProperty> {

    @Override
    protected AbstractOWLObjectHierarchyProvider<OWLDataProperty> create(OWLOntologyManager m) {
        return new OWLDataPropertyHierarchyProvider(m);
    }

    @Override
    protected OWLDataProperty getRootObject(OWLDataFactory df) {
        return df.getOWLTopDataProperty();
    }

    @Override
    protected OWLDataProperty createChildObject(OWLDataFactory df) {
        return df.getOWLDataProperty("P");
    }

    @Override
    protected OWLAxiom createSubObjectAxiom(OWLDataFactory df, OWLDataProperty sub, OWLDataProperty top) {
        return df.getOWLSubDataPropertyOfAxiom(sub, top);
    }

    @Override
    protected int getDefaultRootCount() {
        return 1;
    }
}
