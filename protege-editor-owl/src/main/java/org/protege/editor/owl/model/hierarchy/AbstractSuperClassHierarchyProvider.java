package org.protege.editor.owl.model.hierarchy;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.Collections;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 14-Sep-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 * A "reverse hierarchy" of superclass relationships.
 * Children are superclasses of their parents.
 */
public abstract class AbstractSuperClassHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLClass> {
    private volatile OWLClass root;

    public AbstractSuperClassHierarchyProvider(OWLOntologyManager manager) {
        super(manager);
    }

    public void setRoot(OWLClass clazz) {
        root = clazz;
        fireHierarchyChanged();
    }

    @Override
    public Set<OWLClass> getRoots() {
        return root == null ? Collections.emptySet() : Collections.singleton(root);
    }
}
