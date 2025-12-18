package hr.bmestric.sevens.events;

import hr.bmestric.sevens.model.Card;

import java.util.Collections;
import java.util.List;

public class CardPlayedEvent extends GameEvent {
    private final String playerId;
    private final List<Card> cards;


    public CardPlayedEvent(String playerId, List<Card> cards) {
        super("CARD_PLAYED");
        this.playerId = playerId;
        this.cards = Collections.unmodifiableList(cards);
    }

    public String getPlayerId() {
        return playerId;
    }

    public List<Card> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        return String.format("CardPlayedEvent{playerId='%s', cards=%s, timestamp=%s}",
                playerId, cards, getTimestamp());
    }
}