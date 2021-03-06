package org.protege.editor.owl.ui;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.model.OWLModelManager;

import java.util.Comparator;


/**
 * Author: Nick Drummond<br>
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>

 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Dec 8, 2006<br><br>

 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)<br>
 * copyright 2006, The University of Manchester<br>
 */
public class OWLObjectComparator<E extends Comparable<E>> implements Comparator<E>, Disposable {

    private OWLModelManager owlModelManager;

    public OWLObjectComparator(OWLModelManager owlModelManager) {
        this.owlModelManager = owlModelManager;
    }

    @Override
    public int compare(E o1, E o2) {
        return o1.compareTo(o2);
    }

    protected OWLModelManager getOWLModelManager() {
        return owlModelManager;
    }

    @Override
    public void dispose() {
        owlModelManager = null;
    }
}
