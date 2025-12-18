package hr.bmestric.sevens.engine.interfaces;

import hr.bmestric.sevens.engine.MoveValidationResult;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;

import java.util.List;

public interface IMoveValidator {
    MoveValidationResult validate(GameState gameState, String playerId, List<Card> cards);
    boolean mustRespondToSequence(GameState gameState, String playerId);
}
