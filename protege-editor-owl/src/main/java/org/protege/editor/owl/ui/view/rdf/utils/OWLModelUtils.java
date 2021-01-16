package org.protege.editor.owl.ui.view.rdf.utils;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.internal.AxiomTranslator;
import com.github.owlcs.ontapi.internal.InternalConfig;
import com.github.owlcs.ontapi.internal.ONTObjectFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;

/**
 * Created by @ssz on 16.01.2021.
 */
public class OWLModelUtils {

    public static OntModel getGraphModel(Ontology ont) {
        return OWLAdapter.get().asBaseModel(ont).getBase();
    }

    public static InternalConfig getConfig(OntModel model) {
        return AxiomTranslator.getConfig(model);
    }

    public static ONTObjectFactory getObjectFactory(OntModel model) {
        return AxiomTranslator.getObjectFactory(model);
    }
}
