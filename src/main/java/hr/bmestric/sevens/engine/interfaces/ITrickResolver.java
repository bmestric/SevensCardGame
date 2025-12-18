package hr.bmestric.sevens.engine.interfaces;

import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Trick;

public interface ITrickResolver {
    String determineTrickWinner(Trick trick, String player1Id, String player2Id);
    int caluclateTrickPoints(Trick trick);
    boolean isTrickComplete(Trick trick, GameState gameState);
}
