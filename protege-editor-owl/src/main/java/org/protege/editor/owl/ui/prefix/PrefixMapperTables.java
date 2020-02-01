package org.protege.editor.owl.ui.prefix;

import com.github.owlcs.ontapi.OWLAdapter;
import com.github.owlcs.ontapi.jena.utils.Models;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.protege.editor.owl.ui.renderer.prefix.PrefixBasedRenderer;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PrefixMapperTables extends JPanel {
	private static final long serialVersionUID = -7430862544150495635L;

	private PrefixMapperTable table;
	private OWLOntology ontology;
	private final OWLModelManager manager;

	private final Set<SelectedOntologyListener> listeners = new HashSet<>();

	private TableModelListener editListener = new TableModelListener() {

		@Override
		public void tableChanged(TableModelEvent e) {
			if (table == null) {
				return;
			}
			Map<String, String> res = table.getModel().commitPrefixes();
			if (res.isEmpty()) {
				return;
			}
			// copy prefixes inside model:
			Models.setNsPrefixes(OWLAdapter.get().asONT(ontology).asGraphModel(), res);
			// reset RDF views:
			manager.setDirty(ontology);
			OWLModelManagerEntityRenderer renderer = manager.getOWLEntityRenderer();
			if (renderer instanceof PrefixBasedRenderer) {
				manager.refreshRenderer();
			}
		}
	};

	public PrefixMapperTables(OWLModelManager manager) {
		this.manager = Objects.requireNonNull(manager);
		setLayout(new BorderLayout());
		setOntology(manager.getActiveOntology());
	}

	public void refill() {
		table.getModel().refill();
	}

	public void setOntology(OWLOntology ontology) {
		if (table != null) {
			table.getModel().removeTableModelListener(editListener);
		}
		this.ontology = ontology;
		PrefixDocumentFormat prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(ontology);
		table = new PrefixMapperTable(prefixManager);
		table.getModel().addTableModelListener(editListener);
		removeAll();
		add(new JScrollPane(table));
		for (SelectedOntologyListener listener : listeners) {
			listener.selectedOntologyChanged();
		}
	}

	public void addListener(SelectedOntologyListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SelectedOntologyListener listener) {
		listeners.remove(listener);
	}

	public OWLOntology getOntology() {
		return ontology;
	}

	public PrefixMapperTable getPrefixMapperTable() {
		return table;
	}

	public interface SelectedOntologyListener {
		void selectedOntologyChanged();
	}

}
