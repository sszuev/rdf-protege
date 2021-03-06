package org.protege.editor.owl.ui.prefix;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 22-Sep-2006<br><br>
 * <p>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class GeneratePrefixFromOntologyAction extends AbstractAction {
    private final OWLEditorKit owlEditorKit;
    private final PrefixMapperTables tables;
    private final PrefixGenerator generator;

    public GeneratePrefixFromOntologyAction(OWLEditorKit owlEditorKit, PrefixMapperTables tables) {
        this(owlEditorKit, tables, new PrefixGenerator());
    }

    public GeneratePrefixFromOntologyAction(OWLEditorKit kit, PrefixMapperTables tables, PrefixGenerator generator) {
        super("Generate from ontology URI", OWLIcons.getIcon("prefix.generate.png"));
        putValue(AbstractAction.SHORT_DESCRIPTION, "Generate prefix mappings from ontology URIs...");
        this.owlEditorKit = Objects.requireNonNull(kit);
        this.tables = Objects.requireNonNull(tables);
        this.generator = Objects.requireNonNull(generator);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UIHelper uiHelper = new UIHelper(owlEditorKit);
        Set<OWLOntology> ontologies = uiHelper.pickOWLOntologies();
        for (OWLOntology ont : ontologies) {
            // TODO what about anonymous ontologies?
            String uri = ont.getOntologyID().getDefaultDocumentIRI().orElseThrow(IllegalStateException::new).toString();
            generatePrefix(uri);
        }
    }

    private void generatePrefix(String uri) {
        String prefix = generator.generate(uri);
        if (!uri.endsWith("#") && !uri.endsWith("/")) {
            uri = uri + "#";
        }
        PrefixMapperTable table = tables.getPrefixMapperTable();
        int index = table.getModel().addMapping(prefix, uri);
        table.getSelectionModel().setSelectionInterval(index, index);
    }
}
