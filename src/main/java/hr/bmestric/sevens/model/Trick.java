package hr.bmestric.sevens.model;

import hr.bmestric.sevens.model.enums.Rank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Trick implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Card> cards;
    private String leadingPlayerId;
    private String lastPlayerId;
    private String lastMatchingOrTrumpPlayerId;

    public Trick() {
        this.cards = new ArrayList<>();
    }

    public void addCard(Card card, String playerId) {
        if(card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        if(playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }

        cards.add(card);
        if(leadingPlayerId == null) {
            leadingPlayerId = playerId;
        }
        lastPlayerId = playerId;

        if (!cards.isEmpty()) {
            Rank openingRank = cards.get(0).getRank();
            if (card.getRank().isTrump() || card.getRank() == openingRank) {
                lastMatchingOrTrumpPlayerId = playerId;
            }
        }
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public boolean hasCards() {
        return !cards.isEmpty();
    }

    public String getLastPlayerId() {
        return lastPlayerId;
    }

    public String getLeadingPlayerId() {
        return leadingPlayerId;
    }

    public String getLastMatchingOrTrumpPlayerId() {
        return lastMatchingOrTrumpPlayerId;
    }

    public Optional<Card> getLastCard() {
        return cards.isEmpty() ? Optional.empty() : Optional.of(cards.get(cards.size() - 1));
    }

    public int calculatePoints() {
        return cards.stream()
                .mapToInt(Card::getPoints)
                .sum();
    }
    public int getLeadingPlayerCardCount() {
        if (cards.isEmpty() || leadingPlayerId == null) {
            return 0;
        }
        return (cards.size() + 1) / 2;
    }

    public void clear() {
        cards.clear();
        leadingPlayerId = null;
        lastPlayerId = null;
        lastMatchingOrTrumpPlayerId = null;
    }

    @Override
    public String toString() {
        return "Stih{" +
                "cards=" + cards +
                ", points=" + calculatePoints() +
                '}';
    }
}
