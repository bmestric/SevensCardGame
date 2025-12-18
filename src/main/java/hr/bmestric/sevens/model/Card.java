package hr.bmestric.sevens.model;

import hr.bmestric.sevens.model.enums.Rank;
import hr.bmestric.sevens.model.enums.Suit;

import java.io.Serializable;
import java.util.Objects;

public final class Card implements Serializable,Comparable<Card> {
    private static final long serialVersionUID = 1L;

    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getPoints() {
        return rank.getPoints();
    }

    public boolean isTrump() {
        return rank.isTrump();
    }

    public boolean hasSameRank(Card other) {
        return this.rank == other.rank;
    }

    public boolean hasSameSuit(Card other) {
        return this.suit == other.suit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    @Override
    public int compareTo(Card o) {
        int rankComparison = Integer.compare(this.rank.getValue(), o.rank.getValue());
        if (rankComparison != 0) {
            return rankComparison;
        }
        return this.suit.compareTo(o.suit);
    }

    @Override
    public String toString() {
        return rank.getSymbol() + suit.getSymbol();
    }
}
