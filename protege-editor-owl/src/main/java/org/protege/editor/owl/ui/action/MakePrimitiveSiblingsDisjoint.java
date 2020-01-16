package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLHierarchyProvider;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 30-Jun-2006<br><br>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class MakePrimitiveSiblingsDisjoint extends SelectedOWLClassAction {

    @Override
    protected void initialiseAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLClass selCls = getOWLClass();
        if (selCls == null) {
            return;
        }
        OWLModelManager m = getOWLModelManager();
        // TODO: Extract this and make less dependent on hierarchy provider
        OWLHierarchyProvider<OWLClass> provider = m.getOWLHierarchyManager().getOWLClassHierarchyProvider();
        Set<OWLClass> clses = new HashSet<>();
        for (OWLClass par : provider.getParents(selCls)) {
            provider.children(par).forEach(clses::add);
        }
        for (Iterator<OWLClass> it = clses.iterator(); it.hasNext(); ) {
            OWLClass cls = it.next();
            for (OWLOntology ont : m.getActiveOntologies()) {
                if (EntitySearcher.isDefined(cls, ont)) {
                    it.remove();
                    break;
                }
            }
        }

        if (clses.size() > 1) {
            OWLAxiom ax = getOWLDataFactory().getOWLDisjointClassesAxiom(clses);
            m.applyChange(new AddAxiom(m.getActiveOntology(), ax));
        }
        // 2) Get the named subs

//        try {
//            OWLOntology owlOntology = m.getActiveOntology();
//            DisjointAxiomCreator creator = new DisjointAxiomCreator(m.getOWLClassHierarchyProvider(),
//                                                                    owlOntology,
//                                                                    m.getActiveOntologies());
//            m.applyChanges(creator.getChanges());
//        } catch (OWLException ex) {
//            logger.error(ex);
//        }
    }
}
