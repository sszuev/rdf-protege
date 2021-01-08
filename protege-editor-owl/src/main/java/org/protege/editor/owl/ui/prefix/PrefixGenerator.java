package org.protege.editor.owl.ui.prefix;

import org.apache.jena.ext.xerces.util.XMLChar;
import org.apache.jena.rdf.model.impl.Util;

import java.util.Random;

/**
 * Created by @ssz on 08.01.2021.
 */
public class PrefixGenerator {
    private static final Random RANDOM = new Random();

    /**
     * Generates the prefix from the given uri.
     *
     * @param uri {@code String}, not {@code null}, not empty
     * @return {@code String}, never {@code null}
     */
    public String generate(String uri) {
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("Empty uri.");
        }
        String res;
        res = fromProtege(uri);
        if (res != null) return res;
        res = calculatePrefix(uri);
        if (res != null) return res;
        res = randomPrefix(uri);
        if (res != null) return res;
        throw new IllegalArgumentException("Can't generate prefix from URI <" + uri + ">");
    }

    /**
     * Answers {@code true} if the given string is a legal prefix (can be used in XML also).
     *
     * @param prefix {@code String}
     * @return {@code boolean}
     * @see org.apache.jena.shared.impl.PrefixMappingImpl
     */
    public static boolean isLegalPrefix(String prefix) {
        return prefix.isEmpty() || org.apache.jena.ext.xerces.util.XMLChar.isValidNCName(prefix);
    }

    /**
     * Original way to generate the prefix from uri.
     *
     * @param uri {@code String}, not {@code null}
     * @return {@code String} or {@code null}
     */
    public static String fromProtege(String uri) {
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        String res = uri.substring(uri.lastIndexOf('/') + 1);
        if (res.endsWith(".owl")) {
            res = res.substring(0, res.length() - 4);
        }
        res = res.toLowerCase();
        if (isLegalPrefix(res)) {
            // the original way:
            return res;
        }
        return null;
    }

    /**
     * Generates a random prefix from the given string.
     *
     * @param uri {@code String}, not {@code null}
     * @return {@code String} or {@code null}
     */
    public static String randomPrefix(String uri) {
        return randomPrefix(uri, 5);
    }

    /**
     * Generates a random prefix from the given string.
     *
     * @param uri       {@code String}, not {@code null}
     * @param maxLength {@code int}, maximum allowed length of the prefix
     * @return {@code String} or {@code null}
     */
    public static String randomPrefix(String uri, int maxLength) {
        if (uri.length() <= maxLength) {
            throw new IllegalArgumentException("To short uri: <" + uri + ">");
        }
        StringBuilder src = new StringBuilder(uri.toLowerCase());
        StringBuilder res = new StringBuilder();
        while (res.length() < maxLength && src.length() > 0) {
            int pos = RANDOM.nextInt(src.length());
            char ch = src.charAt(pos);
            src.deleteCharAt(pos);
            if (res.length() == 0 ? XMLChar.isNCNameStart(ch) : XMLChar.isNCName(ch)) {
                res.append(ch);
            }
        }
        if (res.length() == 0) {
            return null;
        }
        return res.toString();
    }

    /**
     * Auxiliary method,
     * that attempts to compute a good-looking prefix from the given URI.
     *
     * @param uri {@code String}, not {@code null}
     * @return {@code String} or {@code null}
     */
    public static String calculatePrefix(String uri) {
        return calculatePrefix(uri, 10);
    }

    /**
     * Tries to compute a good-looking prefix from the given URI with length restriction.
     *
     * @param uri       {@code String}, not {@code null}
     * @param maxLength {@code int}, maximum allowed length of the prefix
     * @return {@code String} or {@code null}
     */
    public static String calculatePrefix(String uri, int maxLength) {
        String body = stripURI(getNameSpace(uri));
        String name = getLocalName(body);
        if (name != null && name.length() <= maxLength && isLegalPrefix(name)) {
            return name.toLowerCase();
        }
        return abbreviate(body, maxLength);
    }

    /**
     * Abbreviates the URI by choosing only the first letter from the every significant parts of it.
     *
     * @param uri       {@code String}, not {@code null}
     * @param maxLength {@code int}
     * @return {@code String} or {@code null}
     */
    public static String abbreviate(String uri, int maxLength) {
        StringBuilder res = new StringBuilder();
        for (String part : uri.split("[.\\-/\\d]")) {
            char c = firstNCName(part, res.length() == 0);
            if (c != 0)
                res.append(c);
            if (res.length() > maxLength) {
                break;
            }
        }
        return res.length() == 0 ? null : res.toString();
    }

    /**
     * Finds and returns the valid NCName character from the given string and converts it to lower case.
     *
     * @param s     String
     * @param start boolean
     * @return char or {@code 0}
     */
    private static char firstNCName(String s, boolean start) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (start ? XMLChar.isNCNameStart(ch) : XMLChar.isNCName(ch)) {
                return Character.toLowerCase(ch);
            }
        }
        return 0;
    }

    /**
     * Strips the URI to the body part, removing the schema part and the end symbols ({@code #} or {@code /}).
     *
     * @param uri String, not {@code null}
     * @return String, not {@code null} (possibly, the same)
     */
    private static String stripURI(String uri) {
        return uri.replaceFirst("^[^:]+:[/]*(.+[^#/])[#/]*$", "$1");
    }

    /**
     * Gets a namespace part from URI.
     *
     * @param uri String, not {@code null}
     * @return String, not {@code null}
     */
    private static String getNameSpace(String uri) {
        int i = Util.splitNamespaceXML(uri);
        return i == uri.length() ? uri : uri.substring(0, i);
    }

    /**
     * Gets a local part from URI of {@code null}.
     *
     * @param uri String, not {@code null}
     * @return String or {@code null}
     */
    private static String getLocalName(String uri) {
        int i = Util.splitNamespaceXML(uri);
        return i == uri.length() || i <= 1 ? null : uri.substring(i);
    }
}
