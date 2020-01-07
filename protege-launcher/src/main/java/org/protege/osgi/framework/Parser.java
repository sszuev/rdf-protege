package org.protege.osgi.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private DocumentBuilderFactory factory;

    private Map<String, String> frameworkProperties;
    private Map<String, String> systemProperties;
    private List<BundleSearchPath> searchPaths = new ArrayList<>();

    public Parser() {
        factory = DocumentBuilderFactory.newInstance();
    }

    public Map<String, String> getFrameworkProperties() {
        return frameworkProperties;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public List<BundleSearchPath> getSearchPaths() {
        return searchPaths;
    }

    public void reset() {
        frameworkProperties = new TreeMap<>();
        systemProperties = new TreeMap<>();
        searchPaths.clear();
    }

    public void parse(File f) throws ParserConfigurationException, SAXException, IOException {
        reset();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(f);
        Node top = null;
        for (int j = 0; j < doc.getChildNodes().getLength(); j++) {
            Node node = doc.getChildNodes().item(j);
            if ("launch".equals(node.getNodeName())) {
                top = node;
                break;
            }
        }
        if (top == null) {
            throw new ParserConfigurationException("Can't find <launch>");
        }
        NodeList nodes = top.getChildNodes();
        List<Node> bundles = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node job = nodes.item(i);
            if (!(job instanceof Element)) continue;
            if ("systemProperties".equals(job.getNodeName())) {
                systemProperties = readProperties(job.getChildNodes());
            } else if ("frameworkProperties".equals(job.getNodeName())) {
                frameworkProperties = readProperties(job.getChildNodes());
            } else if ("bundles".equals(job.getNodeName())) {
                bundles.add(job);
            }
        }
        bundles.forEach(e -> {
            BundleSearchPath directory = readDirectories(e, systemProperties);
            searchPaths.add(directory);
            LOGGER.debug("Added bundle search path: {}", directory);
        });
    }

    protected Map<String, String> readProperties(NodeList nodes) {
        Map<String, String> res = new TreeMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node propertyNode = nodes.item(i);
            if (!(propertyNode instanceof Element) || !"property".equals(propertyNode.getNodeName())) {
                continue;
            }
            NamedNodeMap attributes = propertyNode.getAttributes();
            Node nameNode = attributes.getNamedItem("name");
            Node valueNode = attributes.getNamedItem("value");
            if (nameNode != null && valueNode != null) {
                res.put(nameNode.getNodeValue(), valueNode.getNodeValue());
            }
        }
        return res;
    }

    protected BundleSearchPath readDirectories(Node node, Map<String, String> properties) {
        List<String> names = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!(child instanceof Element)) {
                continue;
            }
            if ("bundle".equals(child.getNodeName())) {
                Node bundleNameNode = child.getAttributes().getNamedItem("name");
                if (bundleNameNode != null) {
                    names.add(bundleNameNode.getNodeValue());
                }
            } else if ("search".equals(child.getNodeName())) {
                Node searchPathNode = child.getAttributes().getNamedItem("path");
                if (searchPathNode != null) {
                    paths.add(searchPathNode.getNodeValue());
                }
            }
        }
        return BundleSearchPath.create(names, paths, properties);
    }
}
