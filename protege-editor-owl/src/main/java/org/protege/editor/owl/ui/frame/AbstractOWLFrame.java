package org.protege.editor.owl.ui.frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jan-2007<br><br>
 */
public abstract class AbstractOWLFrame<R> implements OWLFrame<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOWLFrame.class);

    private R rootObject;

    private final List<OWLFrameListener> listeners;
    private final List<OWLFrameSection<? super R, ?, ?>> sections;

    public AbstractOWLFrame() {
        listeners = new ArrayList<>();
        sections = new ArrayList<>();
    }

    protected void addSection(OWLFrameSection<? super R, ?, ?> section, int index) {
        sections.add(index, section);
    }

    protected void addSection(OWLFrameSection<? super R, ?, ?> section) {
        sections.add(section);
    }

    protected void clearSections() {
        sections.clear();
        fireContentChanged();
    }

    @Override
    public void dispose() {
        sections.forEach(OWLFrameSection::dispose);
    }

    @Override
    public List<OWLFrameSection<? super R, ?, ?>> getFrameSections() {
        return sections;
    }

    @Override
    public R getRootObject() {
        return rootObject;
    }

    @Override
    public void setRootObject(R rootObject) {
        this.rootObject = rootObject;
        refill();
        fireContentChanged();
    }

    public void refill() {
        getFrameSections().forEach(section -> {
            try {
                section.setRootObject(rootObject);
            } catch (Exception ex) {
                LOGGER.warn("An error occurred whilst refilling the {} frame.  Error: ", getClass().getName(), ex);
            }
        });
    }

    @Override
    public void addFrameListener(OWLFrameListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeFrameListener(OWLFrameListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fireContentChanged() {
        listeners.forEach(listener -> {
            try {
                listener.frameContentChanged();
            } catch (Exception e) {
                LOGGER.warn("An error was thrown whilst dispatching a fireContentChanged event " +
                        "to a registered frame listener: '{}'", e.getMessage(), e);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRootObject());
        sb.append("\n\n");

        for (OWLFrameSection<?, ?, ?> sec : getFrameSections()) {
            sb.append(sec.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
