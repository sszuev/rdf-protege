package org.protege.editor.owl.ui.tree;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.protege.editor.owl.ui.OWLObjectComparator;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;

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

    public OWLObjectTree(OWLEditorKit kit,
                         OWLHierarchyProvider<N> provider,
                         Comparator<? super N> comparator) {
        this(kit, provider, provider.getRoots(), getNodeComparator(comparator, kit));
    }

    public OWLObjectTree(OWLEditorKit eKit,
                         OWLHierarchyProvider<N> provider,
                         Collection<N> rootObjects,
                         Comparator<? super N> comparator) {
        super(eKit, provider, rootObjects, comparator);
    }

    @SuppressWarnings("unchecked")
    private static <N> Comparator<? super N> getNodeComparator(Comparator<? super N> comparator, OWLEditorKit kit) {
        return comparator != null ? comparator : (Comparator<? super N>) kit.getOWLModelManager().getOWLObjectComparator();
    }

    @Override
    public Comparator<? super N> getCopyComparator() {
        return new OWLObjectComparator<>(getOWLModelManager());
    }

    @Override
    protected String getRendering(N obj) {
        return getOWLModelManager().getRendering(obj);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        N obj = getObjectAtMousePosition(event);
        if (obj instanceof OWLEntity) {
            return ((OWLEntity) obj).getIRI().toString();
        }
        return super.getToolTipText(event);
    }
}
