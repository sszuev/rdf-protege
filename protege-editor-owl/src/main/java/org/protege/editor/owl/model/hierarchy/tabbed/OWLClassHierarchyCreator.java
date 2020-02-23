package org.protege.editor.owl.model.hierarchy.tabbed;

import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.model.entity.OWLEntityCreationException;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.entity.OWLEntityFactory;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Aug 12, 2008<br><br>
 */
public class OWLClassHierarchyCreator {

    private final OWLDataFactory df;
    private final OWLEntityFactory entityFactory;

    private final OWLClass root;
    private final List<Edge> edges;
    private final OWLOntology ont;
    private final boolean siblingsDisjoint;

    private final Map<String, OWLClass> nameMap = new HashMap<>();
    private final List<OWLOntologyChange> changes = new ArrayList<>();
    private final Map<OWLClass, Set<OWLClass>> parent2ChildMap = new HashMap<>();

    public OWLClassHierarchyCreator(OWLDataFactory df,
                                    OWLEntityFactory fac,
                                    OWLClass rootClass,
                                    boolean makeSiblingClassesDisjoint,
                                    OWLOntology ontology,
                                    List<Edge> edges) {
        this.df = df;
        this.entityFactory = fac;
        this.root = rootClass;
        this.edges = edges;
        this.ont = ontology;
        this.siblingsDisjoint = makeSiblingClassesDisjoint;
    }

    public List<OWLOntologyChange> createHierarchy() {
        changes.clear();
        for (Edge edge : edges) {
            handleEdge(edge);
        }
        if (siblingsDisjoint) {
            handleDisjoints();
        }
        return changes;
    }

    private void handleDisjoints() {
        parent2ChildMap.values().stream()
                .filter(set -> set.size() > 1)
                .map(set -> new AddAxiom(ont, df.getOWLDisjointClassesAxiom(set)))
                .forEach(changes::add);
    }

    private void handleEdge(Edge edge) {
        OWLClass child = getOWLClass(edge.getChildName());
        OWLClass parent = root;
        if (!edge.isRoot()) {
            parent = getOWLClass(edge.getParentName().orElse(null));
        }
        if (siblingsDisjoint) {
            addToMap(parent, child);
        }
        changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(child, parent)));
    }

    protected OWLClass getOWLClass(String name) {
        OWLClass cls = nameMap.get(name);
        if (cls != null) {
            return cls;
        }
        try {
            OWLEntityCreationSet<OWLClass> creationSet = entityFactory.createOWLClass(name, null);
            changes.addAll(creationSet.getOntologyChanges());
            cls = creationSet.getOWLEntity();
            nameMap.put(name, cls);
        } catch (OWLEntityCreationException e) {
            ErrorLogPanel.showErrorDialog(e);
        }
        return cls;
    }

    private void addToMap(OWLClass parent, OWLClass child) {
        parent2ChildMap.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
    }
}
