package hr.bmestric.sevens.model;

import hr.bmestric.sevens.model.enums.Rank;
import hr.bmestric.sevens.model.enums.Suit;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LinkedList<Card> cards;

    public Deck() {
        this.cards = new LinkedList<>();
        initDeck();
    }
    private Deck(List<Card> cards) {
        this.cards = new java.util.LinkedList<>(cards);
    }

    private void initDeck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public synchronized void shuffle() {
        Collections.shuffle(cards, ThreadLocalRandom.current());
    }

    public synchronized Optional<Card> draw() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(cards.removeFirst());
    }

    public synchronized List<Card> draw(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Cannot draw negative number of cards: " + count);
        }

        List<Card> drawnCards = new ArrayList<>(count);
        for (int i = 0; i < count && !cards.isEmpty(); i++) {
            drawnCards.add(cards.removeFirst());
        }
        return drawnCards;
    }

    public synchronized int remaining() {
        return cards.size();
    }
    public synchronized boolean isEmpty() {
        return cards.isEmpty();
    }
    public synchronized List<Card> getCards() {
        return Collections.unmodifiableList(new ArrayList<>(cards));
    }

    public synchronized Deck copy() {
        return new Deck(new ArrayList<>(this.cards));
    }

    @Override
    public synchronized String toString() {
        return "Deck{remaining=" + cards.size() + " cards}";
    }
}
