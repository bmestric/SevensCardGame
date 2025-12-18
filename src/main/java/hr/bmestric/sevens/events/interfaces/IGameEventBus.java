package hr.bmestric.sevens.events.interfaces;

import hr.bmestric.sevens.events.GameEvent;

public interface IGameEventBus {
    <T extends GameEvent> void publish(T event);
    <T extends GameEvent> void subscribe(Class<T> eventClass, IGameEventListener<T> listener);
    <T extends GameEvent> void unsubscribe(Class<T> eventClass, IGameEventListener<T> listener);
    void clear();
    void shutdown();
}
