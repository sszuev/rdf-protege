package org.protege.editor.owl.model.util;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 05-Mar-2007<br><br>
 */
public class ListenerManager<L> {

    private static final int TRACE_START = 3;
    private final Map<L, StackTraceElement[]> listenerMap = new HashMap<>();

    public void recordListenerAdded(L listener) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        listenerMap.put(listener, stackTrace);
    }

    public void recordListenerRemoved(L listener) {
        listenerMap.remove(listener);
    }

    public void dumpWarningForListener(L listener, Logger log, String cleanupMessage) {
        if (!listenerMap.containsKey(listener)) {
            return;
        }
        log.error("*** WARNING BADLY BEHAVING LISTENER: {} ***", listener.getClass().getName());
        StackTraceElement[] trace = listenerMap.get(listener);
        if (trace == null) {
            log.error("     {}", cleanupMessage);
            return;
        }
        log.error("    Possible culprit (trace from when listener was added): ");
        for (int i = TRACE_START; i < trace.length; i++) {
            String s = trace[i].toString();
            log.error("           {}", s);
        }
        log.error("     {}", cleanupMessage);
    }

    public void dumpWarningForAllListeners(Logger log, String cleanupMessage) {
        for (L listener : listenerMap.keySet()) {
            dumpWarningForListener(listener, log, cleanupMessage);
        }
    }
}
