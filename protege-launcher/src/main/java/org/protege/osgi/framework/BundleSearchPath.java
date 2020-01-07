package org.protege.osgi.framework;

import com.google.common.base.MoreObjects;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.toStringHelper;

public class BundleSearchPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleSearchPath.class.getCanonicalName());

    private List<File> path = new ArrayList<>();
    private List<String> allowedBundles = new ArrayList<>();

    protected BundleSearchPath() {
    }

    public static BundleSearchPath create(Collection<String> names,
                                          Collection<String> paths,
                                          Map<String, String> properties) {
        return create(names, paths, k -> System.getProperty(k, properties.get(k)));
    }

    public static BundleSearchPath create(Collection<String> names,
                                          Collection<String> paths,
                                          UnaryOperator<String> properties) {
        BundleSearchPath res = new BundleSearchPath();
        names.forEach(res::addAllowedBundle);
        paths.forEach(s -> res.addSearchPath(s, properties));
        return res;
    }

    /**
     * Sets path.
     * The input may content reference to the system properties, e.g. "${user.home}/.Protege/bundles"
     *
     * @param dir string path to directory, not null
     * @throws NullPointerException     if arg is null
     * @throws IllegalArgumentException if arg contains reference to non-existing system property
     */
    @SuppressWarnings("unused")
    public void addSearchPath(String dir) throws NullPointerException, IllegalArgumentException {
        addSearchPath(dir, System::getProperty);
    }

    /**
     * @param dir path to directory, not null
     * @throws NullPointerException     if arg is null
     * @throws IllegalArgumentException if {@code map} returns null for the arg
     */
    protected void addSearchPath(String dir, UnaryOperator<String> map) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(dir, "Null directory path!");
        while (dir.matches(".*\\$\\{[^}]+}.*")) {
            String var = dir.replaceFirst(".*(\\$\\{[^}]+}).*", "$1");
            String key = var.substring(2, var.length() - 1);
            String value = map.apply(key);
            if (value == null) throw new IllegalArgumentException("Unable to find '" + key + "'");
            dir = dir.replace(var, value);
        }
        path.add(Paths.get(dir).toFile());
    }


    public List<File> getPath() {
        return path;
    }

    @SuppressWarnings("unused")
    public List<String> getAllowedBundles() {
        return allowedBundles;
    }

    public void addAllowedBundle(String bundle) {
        allowedBundles.add(bundle);
    }

    public Collection<File> search() {
        Map<SymbolicName, BundleInfo> nameToFileMap = new LinkedHashMap<>();
        for (File dir : path) {
            if (!dir.exists() || !dir.isDirectory()) {
                continue;
            }
            File[] contents = dir.listFiles();
            if (contents == null) {
                continue;
            }
            for (File jar : contents) {
                String jarName = jar.getName();
                if (jar.getName().endsWith(".jar") && isAllowedBundle(jarName)) {
                    toBundleInfo(jar).ifPresent(bundleInfo -> addJar(bundleInfo, nameToFileMap));
                }
            }
        }
        return nameToFileMap.values().stream().map(BundleInfo::getBundleFile).collect(Collectors.toList());
    }

    private boolean isAllowedBundle(String jarName) {
        return allowedBundles.isEmpty() || allowedBundles.contains(jarName);
    }

    private void addJar(BundleInfo bundleInfo, Map<SymbolicName, BundleInfo> nameToFileMap) {
        SymbolicName symbolicName = bundleInfo.getSymbolicName();
        BundleInfo existingBundleInfo = nameToFileMap.get(symbolicName);
        if (existingBundleInfo == null) {
            nameToFileMap.put(symbolicName, bundleInfo);
            return;
        }

        if (bundleInfo.isNewerVersionThan(existingBundleInfo)) {
            nameToFileMap.put(symbolicName, bundleInfo);
            LOGGER.warn("Found duplicate plugin/bundle.  " +
                            "Using the latest version, {} and ignoring the previous version, {}.",
                    bundleInfo.getBundleFile().getName(),
                    existingBundleInfo.getBundleFile().getName());
        } else if (bundleInfo.isNewerTimestampThan(existingBundleInfo)) {
            nameToFileMap.put(symbolicName, bundleInfo);
            LOGGER.warn("Found duplicate plugin/bundle. " +
                            "Using the most recent, {} (modified {}) " +
                            "and ignoring the older copy, {} (modified {}).",
                    bundleInfo.getBundleFile().getName(),
                    String.format("%tc", bundleInfo.getBundleFile().lastModified()),
                    existingBundleInfo.getBundleFile().getName(),
                    String.format("%tc", existingBundleInfo.getBundleFile().lastModified())

            );
        } else {
            LOGGER.warn(
                    "Ignoring plugin/bundle ({}) because it is a duplicate of {}.",
                    existingBundleInfo.getBundleFile().getName(),
                    bundleInfo.getBundleFile().getName()
            );
        }

    }

    private Optional<BundleInfo> toBundleInfo(File file) {
        try (JarInputStream is = new JarInputStream(new FileInputStream(file))) {
            Manifest mf = is.getManifest();
            if (mf == null) {
                LOGGER.warn("Could not parse {} as plugin/bundle because the manifest.mf file is not present.", file);
                return Optional.empty();
            }
            Attributes attributes = mf.getMainAttributes();
            String symbolicName = attributes.getValue(Constants.BUNDLE_SYMBOLICNAME);
            if (symbolicName == null) {
                return Optional.empty();
            }
            String versionString = attributes.getValue(Constants.BUNDLE_VERSION);
            Optional<Version> version =
                    versionString == null ? Optional.empty() : Optional.of(new Version(versionString));
            return Optional.of(new BundleInfo(file, new SymbolicName(symbolicName), version));
        } catch (Exception e) {
            LOGGER.warn("Could not parse {} as plugin/bundle. Error: ", file, e);
            return Optional.empty();
        }
    }


    @Override
    public String toString() {
        MoreObjects.ToStringHelper ts = toStringHelper("BundleSearchPath");
        for (File path : getPath()) {
            ts.add("path", path.getAbsolutePath());
        }
        for (String allowedBundle : allowedBundles) {
            ts.add("allowedBundle", allowedBundle);
        }
        return ts.toString();
    }
}
