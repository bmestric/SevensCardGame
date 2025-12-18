package hr.bmestric.sevens.model.enums;

public enum Rank {
    SEVEN("7", 7, 0),
    EIGHT("8", 8, 0),
    NINE("9", 9, 0),
    TEN("10", 10, 1),      // Worth 1 point
    JACK("J", 11, 0),
    QUEEN("Q", 12, 0),
    KING("K", 13, 0),
    ACE("A", 14, 1);

    Rank(String symbol, int value, int points) {
        this.symbol = symbol;
        this.value = value;
        this.points = points;
    }

    private final String symbol;
    private final int value;
    private final int points;

    public int getValue() {
        return value;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPoints() {
        return points;
    }

    public boolean isTrump() {
        return this == SEVEN;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
