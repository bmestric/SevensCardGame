package hr.bmestric.sevens.events.interfaces;

import hr.bmestric.sevens.events.GameEvent;

public interface IGameEventListener<T extends GameEvent> {
    void onEvent(T event);
}
