package hr.bmestric.sevens.model;

import hr.bmestric.sevens.model.enums.Rank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_HAND_SIZE = 4;

    private final List<Card> cards;

    public Hand () {
        this.cards = new ArrayList<>(MAX_HAND_SIZE);
    }

    public void addCard(Card card) {
        if(card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        if(cards.size() >= MAX_HAND_SIZE) {
            throw new IllegalStateException("Hand is full");
        }
        cards.add(card);
    }

    public void addCards(List<Card> cardsToAdd) {
        if (cards.size() + cardsToAdd.size() > MAX_HAND_SIZE) {
            throw new IllegalStateException(
                    String.format("Cannot add %d cards to hand with %d cards (max %d)",
                            cardsToAdd.size(), cards.size(), MAX_HAND_SIZE)
            );
        }
        cards.addAll(cardsToAdd);
    }

    public boolean removeCard(Card card) {
        if(card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        return cards.remove(card);
    }

    public boolean contains(Card card) {
        if(card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        return cards.contains(card);
    }

    public boolean containsRank(Rank rank) {
        if(rank == null) {
            throw new IllegalArgumentException("Rank cannot be null");
        }
        return cards.stream().anyMatch(card -> card.getRank() == rank);
    }

    public List<Card> getCardsWithRank(Rank rank) {
        return cards.stream()
                .filter(card -> card.getRank() == rank)
                .toList();
    }

    public boolean hasTrump() {
        return cards.stream().anyMatch(Card::isTrump);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public boolean isFull() {
        return cards.size() >= MAX_HAND_SIZE;
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public void clear() {
        cards.clear();
    }
    @Override
    public String toString() {
        return "Hand" + cards;
    }
}
