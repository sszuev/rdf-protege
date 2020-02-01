package org.protege.editor.owl.ui.renderer;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.protege.editor.owl.ui.renderer.prefix.PrefixBasedRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.vocab.Namespaces;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 20-Jun-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLEntityQNameRenderer extends AbstractOWLEntityRenderer implements PrefixBasedRenderer {

    private final PrefixManager prefixManager = PrefixUtilities.createFreshPrefixManager();

    @Override
    public void initialise() {
        for (Namespaces ns : Namespaces.values()) {
            String prefixName = ns.getPrefixName();
            String prefixIRI = ns.getPrefixIRI();
            prefixManager.setPrefix(prefixName + ":", prefixIRI);
        }
        PrefixManager localPrefixes = PrefixUtilities.getPrefixOWLOntologyFormat(getOWLModelManager());
        localPrefixes.prefixNames().forEach(name -> {
            String iri = localPrefixes.getPrefix(name);
            if (iri == null) return;
            prefixManager.setPrefix(name, iri);
        });
    }
    
    @Override
    public void ontologiesChanged() {
    	initialise();
    }

    @Override
    public String render(IRI iri) {
        try {
            String s = prefixManager.getPrefixIRI(iri);
            return s != null ? s : iri.toQuotedString();
        } catch (Exception e) {
            return "<Error! " + e.getMessage() + ">";
        }
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public boolean configure(OWLEditorKit eKit) {
        throw new IllegalStateException("This renderer is not configurable");
    }

    @Override
    protected void disposeRenderer() {
        // do nothing
    }
}
