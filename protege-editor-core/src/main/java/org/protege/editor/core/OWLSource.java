package org.protege.editor.core;

import com.github.owlcs.ontapi.OWLAdapter;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A source (file/uri) with loading settings.
 * Created by @ssz on 08.01.2020.
 */
public class OWLSource {

    public static final String LOAD_FILE_WITH_TR_CONFIG_KEY = "WithTR";

    private final URI uri;
    private Map<String, Object> properties;

    private OWLSource(URI uri, Map<String, Object> properties) {
        this.uri = uri;
        this.properties = Objects.requireNonNull(properties);
    }

    public static OWLSource create(File file, Map<String, Object> properties) {
        return create(file == null ? null : file.toPath().toUri(), properties);
    }

    public static OWLSource create(URI uri, Map<String, Object> properties) {
        return new OWLSource(uri, properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(properties));
    }

    public URI toURI() {
        return uri;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean isEmpty() {
        return uri == null;
    }

    private boolean withTR() {
        return (boolean) getProperties().getOrDefault(OWLSource.LOAD_FILE_WITH_TR_CONFIG_KEY, true);
    }

    public OWLOntologyLoaderConfiguration configure(OWLOntologyLoaderConfiguration conf) {
        return OWLAdapter.get().asONT(conf).setPerformTransformation(withTR());
    }
}
