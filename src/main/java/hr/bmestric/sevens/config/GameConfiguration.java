package hr.bmestric.sevens.config;

import java.io.Serializable;

public class GameConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private int maxHandSize;
    private int deckSize;
    private String networkMode;
    private int tcpPort;
    private int udpPort;
    private String rmiRegistryHost;
    private int rmiRegistryPort;
    private String rmiServiceName;
    private String savedGamesDirectory;
    private String locale;
    private boolean enableChat;
    private int connectionTimeoutSeconds;


    public GameConfiguration() {
        loadFromConfiguration();
    }

    private void loadFromConfiguration() {
        this.maxHandSize = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.MAX_HAND_SIZE);
        this.deckSize = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.DECK_SIZE);
        this.networkMode = ConfigurationReader.getStringValueForKey(ConfigurationKey.NETWORK_MODE);
        this.tcpPort = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.TCP_PORT);
        this.udpPort = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.UDP_PORT);
        this.connectionTimeoutSeconds = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.CONNECTION_TIMEOUT);
        this.rmiRegistryHost = ConfigurationReader.getStringValueForKey(ConfigurationKey.RMI_REGISTRY_HOST);
        this.rmiRegistryPort = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.RMI_REGISTRY_PORT);
        this.rmiServiceName = ConfigurationReader.getStringValueForKey(ConfigurationKey.RMI_SERVICE_NAME);
        this.savedGamesDirectory = ConfigurationReader.getStringValueForKey(ConfigurationKey.SAVED_GAMES_DIRECTORY);
        this.locale = ConfigurationReader.getStringValueForKey(ConfigurationKey.LOCALE);
        this.enableChat = ConfigurationReader.getBooleanValueForKey(ConfigurationKey.ENABLE_CHAT);
    }

    public int getMaxHandSize() {
        return maxHandSize;
    }

    public void setMaxHandSize(int maxHandSize) {
        this.maxHandSize = maxHandSize;
    }

    public int getDeckSize() {
        return deckSize;
    }

    public void setDeckSize(int deckSize) {
        this.deckSize = deckSize;
    }

    public String getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public String getRmiRegistryHost() {
        return rmiRegistryHost;
    }

    public void setRmiRegistryHost(String rmiRegistryHost) {
        this.rmiRegistryHost = rmiRegistryHost;
    }

    public int getRmiRegistryPort() {
        return rmiRegistryPort;
    }

    public void setRmiRegistryPort(int rmiRegistryPort) {
        this.rmiRegistryPort = rmiRegistryPort;
    }

    public String getRmiServiceName() {
        return rmiServiceName;
    }

    public void setRmiServiceName(String rmiServiceName) {
        this.rmiServiceName = rmiServiceName;
    }

    public String getSavedGamesDirectory() {
        return savedGamesDirectory;
    }

    public void setSavedGamesDirectory(String savedGamesDirectory) {
        this.savedGamesDirectory = savedGamesDirectory;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isEnableChat() {
        return enableChat;
    }

    public void setEnableChat(boolean enableChat) {
        this.enableChat = enableChat;
    }

    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) {
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
    }

    @Override
    public String toString() {
        return "GameConfiguration{" +
                "maxHandSize=" + maxHandSize +
                ", deckSize=" + deckSize +
                ", networkMode='" + networkMode + '\'' +
                ", tcpPort=" + tcpPort +
                ", udpPort=" + udpPort +
                ", rmiRegistryHost='" + rmiRegistryHost + '\'' +
                ", rmiRegistryPort=" + rmiRegistryPort +
                ", rmiServiceName='" + rmiServiceName + '\'' +
                ", savedGamesDirectory='" + savedGamesDirectory + '\'' +
                ", locale='" + locale + '\'' +
                ", enableChat=" + enableChat +
                ", connectionTimeoutSeconds=" + connectionTimeoutSeconds +
                '}';
    }
}