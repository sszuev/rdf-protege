package org.protege.editor.owl.model.hierarchy;

import org.github.owlcs.ontapi.OWLManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;

/**
 * Created by @ssz on 18.01.2020.
 */
public abstract class HierarchyTestBase<X> {
    private OWLOntology ontology;
    private AbstractOWLObjectHierarchyProvider<X> provider;

    protected abstract AbstractOWLObjectHierarchyProvider<X> create(OWLOntologyManager m);

    protected abstract X getRootObject(OWLDataFactory df);

    protected abstract X createChildObject(OWLDataFactory df);

    protected abstract OWLAxiom createSubObjectAxiom(OWLDataFactory df, X sub, X top);

    protected abstract int getDefaultRootCount();

    @Before
    public final void before() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        provider = create(manager);
        ontology = manager.createOntology();
        provider.setOntology(ontology);
    }

    @Test
    public final void testAddRemoveSubNode() {
        OWLDataFactory df = ontology.getOWLOntologyManager().getOWLDataFactory();
        X top = getRootObject(df);
        int count = getDefaultRootCount();
        Assert.assertEquals(count, provider.roots().count());
        Assert.assertEquals(0, provider.children(top).count());
        Assert.assertEquals(0, provider.parents(top).count());
        Assert.assertEquals(0, provider.ancestors(top).count());
        Assert.assertEquals(0, provider.descendants(top).count());

        X p = createChildObject(df);
        OWLAxiom ax = createSubObjectAxiom(df, p, top);
        ontology.addAxiom(ax);
        Assert.assertEquals(count, provider.roots().count());
        Assert.assertEquals(1, provider.children(top).count());
        Assert.assertEquals(0, provider.parents(top).count());
        Assert.assertEquals(0, provider.ancestors(top).count());
        Assert.assertEquals(1, provider.descendants(top).count());
        Assert.assertEquals(0, provider.children(p).count());
        Assert.assertEquals(1, provider.parents(p).count());
        Assert.assertEquals(0, provider.descendants(p).count());
        Assert.assertEquals(1, provider.ancestors(p).count());

        ontology.removeAxiom(ax);
        Assert.assertEquals(count, provider.roots().count());
        Assert.assertEquals(0, provider.children(top).count());
        Assert.assertEquals(0, provider.parents(top).count());
        Assert.assertEquals(0, provider.ancestors(top).count());
        Assert.assertEquals(0, provider.descendants(top).count());

        Assert.assertEquals(0, provider.children(p).count());
        Assert.assertEquals(0, provider.parents(p).count());
        Assert.assertEquals(0, provider.descendants(p).count());
        Assert.assertEquals(0, provider.ancestors(p).count());
    }

}
