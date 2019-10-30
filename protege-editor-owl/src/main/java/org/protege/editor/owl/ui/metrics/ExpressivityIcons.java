package org.protege.editor.owl.ui.metrics;

import org.protege.editor.owl.ui.OWLIcons;
import org.semanticweb.owlapi.util.Construct;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 29-Jun-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ExpressivityIcons {

    private static Map<Construct, Icon> map;


    static {
        map = new HashMap<>();
        // TODO: due to changes in issue <https://github.com/owlcs/owlapi/issues/500> there are no such constants anymore.
        //  In version v4, v5(since 5.1.8) and v6 instead of {@code org.semanticweb.owlapi.util.DLExpressivityChecker.Construct}
        //  there are {@code org.semanticweb.owlapi.util.Construct} and {@org.semanticweb.owlapi.util.Languages}.
        //  Please fix according to the newly adopted logic
        //  (or remove this place at all if it is not really important for RDF version of Protege).
        //  Note: since these changes affect v4,
        //  i suppose it is better to wait until the correct patch is available in the original repo
        //  (see <https://github.com/protegeproject/protege>).
        //  As a workaround quick-fix for now these constants are commented out.
        /*
        map.put(AL, loadIcon("AL.png"));
        map.put(C, loadIcon("C.png"));
        map.put(U, loadIcon("U.png"));
        map.put(E, loadIcon("E.png"));
        map.put(N, loadIcon("N.png"));
        map.put(Q, loadIcon("Q.png"));
        map.put(H, loadIcon("H.png"));
        map.put(I, loadIcon("I.png"));
        map.put(O, loadIcon("O.png"));
        map.put(F, loadIcon("F.png"));
        map.put(S, loadIcon("S.png"));
        map.put(D, loadIcon("Datatype.png"));
        */
    }


    public static Icon getIcon(Construct construct) {
        return map.get(construct);
    }


    private static Icon loadIcon(String name) {
        return OWLIcons.getIcon(name);
    }
}
