package org.protege.editor.owl.ui.view.rdf.utils;

import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.config.AxiomsSettings;
import com.github.owlcs.ontapi.internal.AxiomTranslator;
import com.github.owlcs.ontapi.internal.ONTObject;
import com.github.owlcs.ontapi.internal.ONTObjectFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.owlcs.ontapi.jena.model.OntStatement;
import com.github.owlcs.ontapi.jena.utils.Iter;
import com.github.owlcs.ontapi.jena.utils.OntModels;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.protege.editor.owl.ui.view.rdf.RootTriple;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Collection;

/**
 * Created by @ssz on 16.01.2021.
 */
public class OWLTripleUtils {

    public static boolean isRoot(Triple t) {
        return t instanceof RootTriple;
    }

    public static Collection<ONTObject<? extends OWLAxiom>> getAxioms(Triple t, Ontology ont) {
        OntModel m = OWLModelUtils.getGraphModel(ont);
        AxiomsSettings c = OWLModelUtils.getConfig(m);
        ONTObjectFactory f = OWLModelUtils.getObjectFactory(m);
        return getAxioms(t, m, c, f);
    }

    public static Collection<ONTObject<? extends OWLAxiom>> getAxioms(Triple t, OntModel m, AxiomsSettings c, ONTObjectFactory f) {
        OntStatement s = OntModels.toOntStatement(t, m);
        return listAxioms(s, c, f).toSet();
    }

    public static ExtendedIterator<ONTObject<? extends OWLAxiom>> listAxioms(OntStatement s, AxiomsSettings c, ONTObjectFactory f) {
        return Iter.create(AxiomType.AXIOM_TYPES).mapWith(AxiomTranslator::get)
                .filterKeep(x -> x.testStatement(s, c))
                .mapWith(x -> x.toAxiom(s, f, c));
    }
}
