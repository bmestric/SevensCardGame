package hr.bmestric.sevens.events;

import java.time.Instant;
import java.util.UUID;

public abstract class GameEvent {
    private final String eventId;
    private final Instant timestamp;
    private final String eventType;

    protected GameEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.eventType = eventType;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', timestamp=%s}",
                getClass().getSimpleName(), eventId, timestamp);
    }
}
