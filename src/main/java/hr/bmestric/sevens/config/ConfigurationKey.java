package hr.bmestric.sevens.config;

public enum ConfigurationKey {
    MAX_HAND_SIZE("game.maxHandSize"),
    DECK_SIZE("game.deckSize"),

    NETWORK_MODE("network.mode"),
    TCP_PORT("network.tcp.port"),
    UDP_PORT("network.udp.port"),
    CONNECTION_TIMEOUT("network.connectionTimeout"),

    RMI_REGISTRY_HOST("rmi.registry.host"),
    RMI_REGISTRY_PORT("rmi.registry.port"),
    RMI_SERVICE_NAME("rmi.service.name"),

    SAVED_GAMES_DIRECTORY("app.savedGamesDirectory"),
    LOCALE("app.locale"),
    ENABLE_CHAT("app.enableChat");

    private final String key;

    ConfigurationKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
