package hr.bmestric.sevens.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings({"java:S112", "java:S2221"})
public class XmlConfigProvider implements IConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(XmlConfigProvider.class);
    private static final String DEFAULT_CONFIG_NAME = "game-config.xml";
    private static final Path DEFAULT_CONFIG_PATH = Paths.get("src/main/resources/config", DEFAULT_CONFIG_NAME);

    @Override
    public GameConfiguration loadConfig(Path configPath) throws IOException {
        if (configPath == null) {
            throw new IllegalArgumentException("Config path cannot be null");
        }
        if (!Files.exists(configPath)) {
            logger.warn("Config file not found: {}, using defaults", configPath);
            return new GameConfiguration();
        }

        logger.info("Loading configuration from: {}", configPath);

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setNamespaceAware(true);

            SAXParser saxParser = factory.newSAXParser();
            ConfigHandler handler = new ConfigHandler();
            saxParser.parse(configPath.toFile(), handler);

            logger.info("Configuration loaded successfully");
            return handler.getConfiguration();
        } catch (Exception e) {
            logger.error("Failed to load configuration from {}", configPath, e);
            throw new IOException("Failed to parse configuration file: " + configPath, e);
        }
    }

    @Override
    public void saveConfig(GameConfiguration configuration, Path configPath) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        if (configPath == null) {
            throw new IllegalArgumentException("Config path cannot be null");
        }

        logger.info("Saving configuration to: {}", configPath);

        Path parent = configPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            docFactory.setXIncludeAware(false);
            docFactory.setExpandEntityReferences(false);

            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Root element
            Element rootElement = doc.createElement("gameConfiguration");
            doc.appendChild(rootElement);

            // Game settings
            Element gameSettings = doc.createElement("gameSettings");
            rootElement.appendChild(gameSettings);
            addElement(doc, gameSettings, "maxHandSize", String.valueOf(configuration.getMaxHandSize()));
            addElement(doc, gameSettings, "deckSize", String.valueOf(configuration.getDeckSize()));

            // Network settings
            Element networkSettings = doc.createElement("networkSettings");
            rootElement.appendChild(networkSettings);
            addElement(doc, networkSettings, "networkMode", configuration.getNetworkMode());
            addElement(doc, networkSettings, "tcpPort", String.valueOf(configuration.getTcpPort()));
            addElement(doc, networkSettings, "udpPort", String.valueOf(configuration.getUdpPort()));
            addElement(doc, networkSettings, "connectionTimeoutSeconds",
                    String.valueOf(configuration.getConnectionTimeoutSeconds()));

            // RMI settings
            Element rmiSettings = doc.createElement("rmiSettings");
            rootElement.appendChild(rmiSettings);
            addElement(doc, rmiSettings, "registryHost", configuration.getRmiRegistryHost());
            addElement(doc, rmiSettings, "registryPort", String.valueOf(configuration.getRmiRegistryPort()));
            addElement(doc, rmiSettings, "serviceName", configuration.getRmiServiceName());

            // Application settings
            Element appSettings = doc.createElement("applicationSettings");
            rootElement.appendChild(appSettings);
            addElement(doc, appSettings, "savedGamesDirectory", configuration.getSavedGamesDirectory());
            addElement(doc, appSettings, "locale", configuration.getLocale());
            addElement(doc, appSettings, "enableChat", String.valueOf(configuration.isEnableChat()));

            // Write to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(configPath.toFile());
            transformer.transform(source, result);

            logger.info("Configuration saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save configuration to {}", configPath, e);
            throw new IOException("Failed to write configuration file: " + configPath, e);
        }
    }

    @Override
    public GameConfiguration loadDefaultConfig() {
        try {
            if (Files.exists(DEFAULT_CONFIG_PATH)) {
                return loadConfig(DEFAULT_CONFIG_PATH);
            }
        } catch (IOException e) {
            logger.warn("Failed to load default config, using hardcoded defaults", e);
        }
        return new GameConfiguration();
    }

    private void addElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.appendChild(doc.createTextNode(value));
        parent.appendChild(element);
    }

    private static class ConfigHandler extends DefaultHandler {
        private final GameConfiguration config = new GameConfiguration();
        private final StringBuilder currentValue = new StringBuilder();

        public GameConfiguration getConfiguration() {
            return config;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            currentValue.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentValue.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String value = currentValue.toString().trim();
            if (value.isEmpty()) {
                return;
            }

            switch (qName) {
                case "maxHandSize" -> config.setMaxHandSize(Integer.parseInt(value));
                case "deckSize" -> config.setDeckSize(Integer.parseInt(value));
                case "networkMode" -> config.setNetworkMode(value);
                case "tcpPort" -> config.setTcpPort(Integer.parseInt(value));
                case "udpPort" -> config.setUdpPort(Integer.parseInt(value));
                case "connectionTimeoutSeconds" -> config.setConnectionTimeoutSeconds(Integer.parseInt(value));
                case "registryHost" -> config.setRmiRegistryHost(value);
                case "registryPort" -> config.setRmiRegistryPort(Integer.parseInt(value));
                case "serviceName" -> config.setRmiServiceName(value);
                case "savedGamesDirectory" -> config.setSavedGamesDirectory(value);
                case "locale" -> config.setLocale(value);
                case "enableChat" -> config.setEnableChat(Boolean.parseBoolean(value));
                default -> {
                }
            }
        }
    }
}
