package org.protege.editor.owl.ui.tree;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.OWLObjectComparator;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Comparator;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 01-Jun-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLObjectTree<N extends OWLObject> extends ObjectTree<N> {

    public OWLObjectTree(OWLEditorKit eKit, OWLHierarchyProvider<N> provider) {
        this(eKit, provider, null);
    }

    public OWLObjectTree(OWLEditorKit eKit,
                         OWLHierarchyProvider<N> provider,
                         Comparator<? super N> objectComparator) {
        this(eKit, provider, provider.getRoots(), objectComparator);
    }

    public OWLObjectTree(OWLEditorKit eKit,
                         OWLHierarchyProvider<N> provider,
                         Set<N> rootObjects,
                         Comparator<? super N> owlObjectComparator) {
        super(eKit, provider, rootObjects, owlObjectComparator);
    }

    /**
     * @return the comparator used to order sibling tree nodes
     */
    @Override
    public Comparator<? super N> getNodeComparator() {
        return comparator != null ? comparator : eKit.getOWLModelManager().getOWLObjectComparator();
    }

    @Override
    public Comparator<? super N> getCopyComparator() {
        return new OWLObjectComparator<>(getOWLModelManager());
    }

    @Override
    protected String getRendering(N obj) {
        return getOWLModelManager().getRendering(obj);
    }
}
