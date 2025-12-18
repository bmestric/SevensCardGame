package hr.bmestric.sevens.events;

public class TrickWonEvent extends GameEvent {
    private final String winnerId;
    private final String winnerName;
    private final int pointsWon;
    private final int totalCards;


    public TrickWonEvent(String winnerId, String winnerName, int pointsWon, int totalCards) {
        super("TRICK_WON");
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.pointsWon = pointsWon;
        this.totalCards = totalCards;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public int getPointsWon() {
        return pointsWon;
    }

    public int getTotalCards() {
        return totalCards;
    }

    @Override
    public String toString() {
        return String.format("TrickWonEvent{winner='%s', points=%d, cards=%d, timestamp=%s}",
                winnerName, pointsWon, totalCards, getTimestamp());
    }
}
