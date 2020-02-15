package org.protege.editor.owl.ui.action;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.ontology.OntologyIDJDialog;
import org.semanticweb.owlapi.model.*;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 07-Mar-2007<br><br>
 */
public class ChangeOntologyIRI extends ProtegeOWLAction {
    private static final long serialVersionUID = -6080240335045735182L;

    @Override
    public void actionPerformed(ActionEvent e) {
        OWLModelManager manager = getOWLModelManager();
        OWLOntology ont = manager.getActiveOntology();
        OWLOntologyID id = OntologyIDJDialog.showDialog(getOWLEditorKit(), ont.getOntologyID());
        if (id == null) {
            return;
        }
        manager.applyChanges(getChanges(ont, id));
    }

    private static List<OWLOntologyChange> getChanges(OWLOntology ontology, OWLOntologyID id) {
        List<OWLOntologyChange> res = new ArrayList<>();
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLOntologyID oldId = ontology.getOntologyID();
        res.add(new SetOntologyID(ontology, id));
        if (id.isAnonymous() || id.equals(oldId)) {
            return res;
        }
        manager.ontologies().forEach(ont -> ont.importsDeclarations().forEach(declaration -> {
            Optional<IRI> iri = Optional.of(declaration.getIRI());
            OWLImportsDeclaration newDeclaration = null;
            if (iri.equals(oldId.getVersionIRI())) {
                newDeclaration = df.getOWLImportsDeclaration(id.getDefaultDocumentIRI()
                        .orElseThrow(IllegalArgumentException::new));
            } else if (iri.equals(oldId.getOntologyIRI())) {
                newDeclaration = df.getOWLImportsDeclaration(id.getOntologyIRI()
                        .orElseThrow(IllegalArgumentException::new));
            }
            if (newDeclaration == null)
                return;
            res.add(new RemoveImport(ont, declaration));
            res.add(new AddImport(ont, newDeclaration));
        }));
        return res;
    }

    @Override
    public void initialise() throws Exception {
    }

    @Override
    public void dispose() throws Exception {
    }
}
