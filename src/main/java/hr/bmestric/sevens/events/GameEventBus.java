package hr.bmestric.sevens.events;

import hr.bmestric.sevens.events.interfaces.IGameEventBus;
import hr.bmestric.sevens.events.interfaces.IGameEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class GameEventBus implements IGameEventBus {
    private static final Logger logger = LoggerFactory.getLogger(GameEventBus.class);

    private final Map<Class<? extends GameEvent>, List<IGameEventListener<? extends GameEvent>>> listeners;
    private final ExecutorService executorService;
    private final boolean asyncMode;

    public GameEventBus() {
        this(true);
    }

    public GameEventBus(boolean asyncMode) {
        this.listeners = new ConcurrentHashMap<>();
        this.asyncMode = asyncMode;
        this.executorService = asyncMode
                ? Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "EventBus-Worker");
            t.setDaemon(true);
            return t;
        })
                : null;

        logger.info("GameEventBus created in {} mode", asyncMode ? "async" : "sync");
    }

    @Override
    public <T extends GameEvent> void publish(T event) {
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return;
        }

        List<IGameEventListener<? extends GameEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null || eventListeners.isEmpty()) {
            logger.debug("No listeners registered for event type: {}", event.getClass().getSimpleName());
            return;
        }

        logger.debug("Publishing event: {}", event);

        if (asyncMode && executorService != null) {
            // Asynchronous delivery
            executorService.submit(() -> notifyListeners(event, eventListeners));
        } else {
            // Synchronous delivery
            notifyListeners(event, eventListeners);
        }
    }

    private <T extends GameEvent> void notifyListeners(
            T event,
            List<IGameEventListener<? extends GameEvent>> eventListeners) {
        for (IGameEventListener<? extends GameEvent> listener : eventListeners) {
            try {
                ((IGameEventListener<T>) listener).onEvent(event);
            } catch (Exception e) {
                logger.error("Error notifying listener {} of event {}",
                        listener.getClass().getSimpleName(), event.getEventType(), e);
            }
        }
    }

    @Override
    public <T extends GameEvent> void subscribe(Class<T> eventClass, IGameEventListener<T> listener) {
        if (eventClass == null || listener == null) {
            throw new IllegalArgumentException("Event class and listener must not be null");
        }

        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.debug("Listener {} subscribed to {}",
                listener.getClass().getSimpleName(), eventClass.getSimpleName());
    }

    @Override
    public <T extends GameEvent> void unsubscribe(Class<T> eventClass, IGameEventListener<T> listener) {
        if (eventClass == null || listener == null) {
            return;
        }

        List<IGameEventListener<? extends GameEvent>> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            logger.debug("Listener {} unsubscribed from {}",
                    listener.getClass().getSimpleName(), eventClass.getSimpleName());
        }
    }

    @Override
    public void clear() {
        listeners.clear();
        logger.info("All event listeners cleared");
    }

    @Override
    public void shutdown() {
        clear();

        if (executorService != null) {
            logger.info("Shutting down event bus executor service");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("GameEventBus shut down");
    }
}
