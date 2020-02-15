package org.protege.editor.owl.ui.action;

import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Nick Drummond<br>
 * The University Of Manchester<br>
 * BioHealth Informatics Group<br>
 * Date: May 19, 2008
 */
public class AmalgamateDisjointClassesAction extends ProtegeOWLAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmalgamateDisjointClassesAction.class);

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        List<OWLOntologyChange> changes = new ArrayList<>();
        int axiomsRemoved = 0;
        int axiomsAdded = 0;
        int numberOfDisjoints = 0;

        for (OWLOntology ont : getOWLModelManager().getActiveOntologies()) {

            // act on each ontology in turn
            CliqueFinder<OWLClassExpression> merger = new CliqueFinder<>();

            Set<OWLDisjointClassesAxiom> oldAxioms = ont.axioms(AxiomType.DISJOINT_CLASSES).collect(Collectors.toSet());
            numberOfDisjoints += oldAxioms.size();

            for (OWLDisjointClassesAxiom ax : oldAxioms) {
                merger.add(ax.classesInSignature().collect(Collectors.toSet()));
            }

            for (Set<OWLClassExpression> newAxioms : merger.getResults()) {
                OWLDisjointClassesAxiom newAxiom = getOWLModelManager().getOWLDataFactory().getOWLDisjointClassesAxiom(newAxioms);
                if (oldAxioms.contains(newAxiom)) {
                    oldAxioms.remove(newAxiom);
                } else {
                    changes.add(new AddAxiom(ont, newAxiom));
                    axiomsAdded++;
                }
            }

            for (OWLDisjointClassesAxiom oldAxiom : oldAxioms) {
                changes.add(new RemoveAxiom(ont, oldAxiom));
                axiomsRemoved++;
            }
        }
        getOWLModelManager().applyChanges(changes);
        LOGGER.info("{} (of {} total) disjoint class axioms replaced with {}", axiomsRemoved, numberOfDisjoints, axiomsAdded);
    }

    @Override
    public void initialise() throws Exception {
        // do nothing
    }

    @Override
    public void dispose() throws Exception {
        // do nothing
    }

    /**
     * A clique is a complete subgraph - that is, one in which all the vertices are connect to each other.
     * Given a set of existing cliques, this class generates a more optimal set
     * eg pairwise disjoints can be turned into disjoint sets
     */
    static class CliqueFinder<O> {

        private Map<O, Set<O>> edgesByVertex = new HashMap<>();
        private Set<Set<O>> originalCliques = new HashSet<>();
        private Set<Set<O>> resultCliques;

        private void add(Set<O> clique) {
            resultCliques = null;
            final Set<O> unmodClique = Collections.unmodifiableSet(clique);
            originalCliques.add(unmodClique);
            List<O> orderedOperands = new ArrayList<>(clique);
            for (int i = 0; i < orderedOperands.size(); i++) {
                O a = orderedOperands.get(i);
                for (int j = i + 1; j < orderedOperands.size(); j++) {
                    O b = orderedOperands.get(j);
                    addEdge(a, b);
                    addEdge(b, a);
                }
            }
        }

        public void clear() {
            resultCliques = null;
            originalCliques.clear();
            edgesByVertex.clear();
        }

        public Set<Set<O>> getResults() {
            if (resultCliques != null) {
                return resultCliques;
            }
            resultCliques = new HashSet<>();
            Set<Integer> skip = new HashSet<>();

            List<Set<O>> workingCliques = new ArrayList<>(originalCliques);
            for (int i = 0; i < workingCliques.size(); i++) {
                if (skip.contains(i)) {
                    continue;
                }
                Set<O> g1 = new HashSet<>(workingCliques.get(i));
                for (int j = i + 1; j < workingCliques.size(); j++) {
                    if (skip.contains(j)) {
                        continue;
                    }
                    Set<O> g2 = workingCliques.get(j);
                    if (canMerge(g1, g2)) {
                        g1.addAll(g2);
                        skip.add(j);
                    }
                }
                resultCliques.add(g1);
            }
            return resultCliques;
        }

        private void addEdge(O d1, O d2) {
            edgesByVertex.computeIfAbsent(d1, k -> new HashSet<>()).add(d2);
        }

        /**
         * @return true if the two subgraphs can be merged because they form a complete graph
         */
        private boolean canMerge(Set<O> g1, Set<O> g2) {
            for (O vertexInG2 : g2) {
                if (g1.contains(vertexInG2)) {
                    continue;
                }
                for (O vertexInG1 : g1) {
                    if (!isEdge(vertexInG2, vertexInG1)) {
                        return false; // found a vertex in g2 that is not adjacent to a vertex in g1
                    }
                }
            }
            return true;
        }

        private boolean isEdge(O v1, O v2) {
            return edgesByVertex.get(v1).contains(v2);
        }
    }

    public static void main(String[] args) { // todo: wrong place for test
        CliqueFinder<String> finder = new CliqueFinder<>();
        finder.add(new HashSet<>(Arrays.asList("A", "B")));
        finder.add(new HashSet<>(Arrays.asList("B", "C")));
        finder.add(new HashSet<>(Arrays.asList("C", "D")));
        finder.add(new HashSet<>(Arrays.asList("A", "D")));
        finder.add(new HashSet<>(Arrays.asList("B", "D")));

        Set<Set<String>> results = finder.getResults();
        assert (results.size() == 2);
        assert (results.contains(new HashSet<>(Arrays.asList("C", "B", "D"))));
        assert (results.contains(new HashSet<>(Arrays.asList("A", "B", "D"))));


        finder.clear();
        finder.add(new HashSet<>(Arrays.asList("A", "X")));
        finder.add(new HashSet<>(Arrays.asList("B", "X")));
        finder.add(new HashSet<>(Arrays.asList("Y", "A")));
        finder.add(new HashSet<>(Arrays.asList("B", "D", "A")));
        finder.add(new HashSet<>(Arrays.asList("C", "B")));

        results = finder.getResults();
        assert (results.size() == 4);
        assert (results.contains(new HashSet<>(Arrays.asList("D", "A", "B"))));
        assert (results.contains(new HashSet<>(Arrays.asList("A", "B", "X"))));
        assert (results.contains(new HashSet<>(Arrays.asList("A", "Y"))));
        assert (results.contains(new HashSet<>(Arrays.asList("C", "B"))));


        finder.clear();
        finder.add(new HashSet<>(Arrays.asList("A", "B", "C")));
        finder.add(new HashSet<>(Arrays.asList("X", "Y", "Z")));
        finder.add(new HashSet<>(Arrays.asList("X", "A")));
        finder.add(new HashSet<>(Arrays.asList("X", "C")));

        for (Set<String> result : finder.getResults()) {
            System.out.print("<");
            for (String s : result) {
                System.out.print(s + " ");
            }
            System.out.println(">");
        }
    }
}