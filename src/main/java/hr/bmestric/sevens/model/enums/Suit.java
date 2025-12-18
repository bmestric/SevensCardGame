package hr.bmestric.sevens.model.enums;

public enum Suit {
    HEARTS("♥", "Hearts"),
    DIAMONDS("♦", "Diamonds"),
    CLUBS("♣", "Clubs"),
    SPADES("♠", "Spades");

    private final String symbol;
    private final String name;

    Suit(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
