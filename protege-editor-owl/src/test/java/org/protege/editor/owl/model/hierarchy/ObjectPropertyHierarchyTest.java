package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Created by @ssz on 18.01.2020.
 */
public class ObjectPropertyHierarchyTest extends HierarchyTestBase<OWLObjectProperty> {

    @Override
    protected AbstractOWLObjectHierarchyProvider<OWLObjectProperty> create(OWLOntologyManager m) {
        return new OWLObjectPropertyHierarchyProvider(m);
    }

    @Override
    protected OWLObjectProperty getRootObject(OWLDataFactory df) {
        return df.getOWLTopObjectProperty();
    }

    @Override
    protected OWLObjectProperty createChildObject(OWLDataFactory df) {
        return df.getOWLObjectProperty("P");
    }

    @Override
    protected OWLAxiom createSubObjectAxiom(OWLDataFactory df, OWLObjectProperty sub, OWLObjectProperty top) {
        return df.getOWLSubObjectPropertyOfAxiom(sub, top);
    }

    @Override
    protected int getDefaultRootCount() {
        return 1;
    }
}
