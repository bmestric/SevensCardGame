package hr.bmestric.sevens.engine.interfaces;

import hr.bmestric.sevens.engine.InvalidMoveException;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;

import java.util.List;

public interface IGameEngine {
    void startNewGame(List<Player> players);
    GameState getState();
    GameState playCards(String playerId, List<Card> cards) throws InvalidMoveException;
    boolean canPlayCards(String playerId, List<Card> cards);
    void passTurn(String playerId) throws InvalidMoveException;
    void endGame(boolean cancelled);
    void resetGame();
    void restoreState(GameState state);
}
