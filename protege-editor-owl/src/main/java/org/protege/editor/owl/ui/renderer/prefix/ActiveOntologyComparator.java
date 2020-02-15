package org.protege.editor.owl.ui.renderer.prefix;

import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Comparator;

public class ActiveOntologyComparator implements Comparator<OWLOntology> {

    @Override
    public int compare(OWLOntology o1, OWLOntology o2) {
        boolean o1ImportsO2 = o1.importsClosure().anyMatch(o2::equals);
        boolean o2ImportsO1 = o2.importsClosure().anyMatch(o1::equals);
        if (o1ImportsO2 && !o2ImportsO1) {
            return -1;
        }
        if (o2ImportsO1 && !o1ImportsO2) {
            return +1;
        }
        return o1.compareTo(o2);
    }
}
