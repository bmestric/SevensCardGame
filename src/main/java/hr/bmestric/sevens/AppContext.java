package hr.bmestric.sevens;

import hr.bmestric.sevens.engine.GameEngine;
import hr.bmestric.sevens.engine.MoveValidator;
import hr.bmestric.sevens.engine.TrickResolver;
import hr.bmestric.sevens.engine.interfaces.IGameEngine;
import hr.bmestric.sevens.engine.interfaces.IMoveValidator;
import hr.bmestric.sevens.engine.interfaces.ITrickResolver;
import hr.bmestric.sevens.events.GameEventBus;
import hr.bmestric.sevens.events.interfaces.IGameEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.Registry;



public class AppContext {
    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);
    private static AppContext instance;

    private final IGameEngine gameEngine;
    private final IGameEventBus eventBus;
    private Registry rmiRegistry;

    private AppContext() {
        logger.info("Initializing application context");

        // Create event bus
        this.eventBus = new GameEventBus();

        // Create game engine components
        IMoveValidator moveValidator = new MoveValidator();
        ITrickResolver trickResolver = new TrickResolver();
        this.gameEngine = new GameEngine(moveValidator, trickResolver);

        logger.info("Application context initialized successfully");
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    // Getters for components

    public IGameEngine getGameEngine() {
        return gameEngine;
    }

    public IGameEventBus getEventBus() {
        return eventBus;
    }

    public Registry getRmiRegistry() {
        return rmiRegistry;
    }
}
