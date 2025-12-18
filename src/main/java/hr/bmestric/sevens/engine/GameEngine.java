package hr.bmestric.sevens.engine;

import hr.bmestric.sevens.engine.interfaces.IGameEngine;
import hr.bmestric.sevens.engine.interfaces.IMoveValidator;
import hr.bmestric.sevens.engine.interfaces.ITrickResolver;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.model.Trick;
import hr.bmestric.sevens.model.enums.GameStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameEngine implements IGameEngine {
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);
    private static final int INITIAL_HAND_SIZE = 4;
    private static final int WINNING_SCORE = 4;

    private final IMoveValidator moveValidator;
    private final ITrickResolver trickResolver;
    private final Lock stateLock;

    public GameEngine(IMoveValidator moveValidator, ITrickResolver trickResolver) {
        this.moveValidator = moveValidator;
        this.trickResolver = trickResolver;
        this.stateLock = new ReentrantLock();
    }
    public GameEngine() {
        this(new MoveValidator(), new TrickResolver());
    }

    private GameState gameState;

    @Override
    public void startNewGame(List<Player> players) {
        stateLock.lock();
        try {
            if (players == null || players.size() != 2) {
                throw new IllegalArgumentException("Game requires exactly 2 players");
            }

            logger.info("Starting new game with players: {} and {}",
                    players.get(0).getDisplayName(), players.get(1).getDisplayName());

            // Create new game state
            gameState = new GameState(players);

            // Shuffle deck
            gameState.getDeck().shuffle();

            // Deal initial cards to each player
            for (Player player : gameState.getPlayers()) {
                player.getHand().clear();
                player.setScore(0);
                List<Card> initialCards = gameState.getDeck().draw(INITIAL_HAND_SIZE);
                player.getHand().addCards(initialCards);
                logger.debug("Dealt {} cards to player {}", initialCards.size(), player.getDisplayName());
            }

            // Set first player's turn (player 1)
            gameState.setCurrentTurnPlayerId(players.get(0).getId());
            gameState.setGameStatus(GameStatus.IN_PROGRESS);

            logger.info("Game started. Deck has {} cards remaining", gameState.getDeck().remaining());
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public GameState getState() {
        stateLock.lock();
        try {
            return gameState;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public GameState playCards(String playerId, List<Card> cards) throws InvalidMoveException {
        stateLock.lock();
        try {
            if (gameState == null) {
                throw new InvalidMoveException("No active game");
            }

            if (gameState.isGameOver()) {
                throw new InvalidMoveException("Game is already over");
            }

            // Validate the move
            MoveValidationResult validation = moveValidator.validate(gameState, playerId, cards);
            if (!validation.isValid()) {
                throw new InvalidMoveException(validation.getReason(), playerId, cards.toString());
            }

            logger.info("Player {} plays: {}", playerId, cards);

            // Get the player
            Player player = gameState.getPlayerById(playerId)
                    .orElseThrow(() -> new InvalidMoveException("Player not found: " + playerId));

            // Remove cards from player's hand and add to trick
            for (Card card : cards) {
                player.getHand().removeCard(card);
                gameState.getTrick().addCard(card, playerId);
            }

            // Check if trick is complete
            if (trickResolver.isTrickComplete(gameState.getTrick(), gameState)) {
                resolveTrick();
            } else {
                // Switch turn to other player
                switchTurn();
            }

            gameState.updateLastModified();
            return gameState;
        } finally {
            stateLock.unlock();
        }
    }

    private void switchTurn() {
        Trick currentTrick = gameState.getTrick();

        // Determine whose turn it should be based on the trick state
        if (!currentTrick.hasCards()) {
            // No cards in trick - just switch normally
            String currentPlayerId = gameState.getCurrentTurnPlayerId();
            String nextPlayerId = gameState.getOpponent(currentPlayerId)
                    .map(Player::getId)
                    .orElseThrow(() -> new IllegalStateException("Cannot find opponent"));
            gameState.setCurrentTurnPlayerId(nextPlayerId);
            return;
        }

        // During a trick, determine whose turn based on card counts
        String leadingPlayerId = currentTrick.getLeadingPlayerId();
        int leadingCount = currentTrick.getLeadingPlayerCardCount();
        int totalCards = currentTrick.getCards().size();
        int respondingCount = totalCards - leadingCount;

        // If responder has played fewer cards than leader, it's responder's turn
        if (respondingCount < leadingCount) {
            String respondingPlayerId = gameState.getOpponent(leadingPlayerId)
                    .map(Player::getId)
                    .orElseThrow(() -> new IllegalStateException("Cannot find opponent"));
            gameState.setCurrentTurnPlayerId(respondingPlayerId);
        } else {
            // Equal cards played - it's leader's turn to continue or pass
            gameState.setCurrentTurnPlayerId(leadingPlayerId);
        }
    }

    private void resolveTrick() {
        Trick trick = gameState.getTrick();
        List<Player> players = gameState.getPlayers();

        // Determine winner
        String winnerId = trickResolver.determineTrickWinner(
                trick, players.get(0).getId(), players.get(1).getId());

        Player winner = gameState.getPlayerById(winnerId)
                .orElseThrow(() -> new IllegalStateException("Winner not found"));

        // Calculate and award points
        int points = trickResolver.caluclateTrickPoints(trick);
        winner.addScore(points);

        logger.info("Trick won by {} for {} points. Score: {} - {}",
                winner.getDisplayName(), points,
                players.get(0).getScore(), players.get(1).getScore());

        // Archive the trick
        gameState.setLastTrickWinnerId(winnerId);
        gameState.archiveCurrentTrick();

        // Winner draws first to refill hand to 4 cards
        refillHands(winnerId);

        // Check for game end condition
        checkGameEnd();

        // Winner gets next turn
        gameState.setCurrentTurnPlayerId(winnerId);
    }

    private void checkGameEnd() {
        List<Player> players = gameState.getPlayers();
        Player player1 = players.get(0);
        Player player2 = players.get(1);

        // Check if any player has more than 4 points
        if (player1.getScore() > WINNING_SCORE && player2.getScore() <= WINNING_SCORE) {
            endGame(false);
            logger.info("Game over! {} wins with {} points", player1.getDisplayName(), player1.getScore());
            return;
        }

        if (player2.getScore() > WINNING_SCORE && player1.getScore() <= WINNING_SCORE) {
            endGame(false);
            logger.info("Game over! {} wins with {} points", player2.getDisplayName(), player2.getScore());
            return;
        }

        // Check for tie at 4-4 (winner is last trick winner)
        if (player1.getScore() == WINNING_SCORE &&
                player2.getScore() == WINNING_SCORE) {
            endGame(false);
            String lastWinnerId = gameState.getLastTrickWinnerId();
            Player tieWinner = gameState.getPlayerById(lastWinnerId).orElse(player1);
            logger.info("Game over! Tied at 4-4, {} wins by last trick", tieWinner.getDisplayName());
            return;
        }

        // Check if deck is exhausted and both hands are empty
        if (gameState.getDeck().isEmpty() &&
                player1.getHand().isEmpty() &&
                player2.getHand().isEmpty()) {
            endGame(false);
            logger.info("Game over! Deck exhausted. Final scores: {} - {}",
                    player1.getScore(), player2.getScore());
        }
    }

    private void refillHands(String winnerId) {
        Player winner = gameState.getPlayerById(winnerId).orElseThrow();
        Player opponent = gameState.getOpponent(winnerId).orElseThrow();

        // Winner draws first
        refillPlayerHand(winner);

        // Then opponent draws
        refillPlayerHand(opponent);
    }

    private void refillPlayerHand(Player player) {
        int cardsNeeded = INITIAL_HAND_SIZE - player.getHand().size();
        if (cardsNeeded > 0 && !gameState.getDeck().isEmpty()) {
            List<Card> drawnCards = gameState.getDeck().draw(cardsNeeded);
            player.getHand().addCards(drawnCards);
            logger.debug("Player {} drew {} cards (hand now: {})",
                    player.getDisplayName(), drawnCards.size(), player.getHand().size());
        }
    }

    @Override
    public boolean canPlayCards(String playerId, List<Card> cards) {
        stateLock.lock();
        try {
            if (gameState == null || gameState.isGameOver()) {
                return false;
            }

            MoveValidationResult result = moveValidator.validate(gameState, playerId, cards);
            return result.isValid();
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void passTurn(String playerId) throws InvalidMoveException {
        stateLock.lock();
        try {
            if (gameState == null) {
                throw new InvalidMoveException("No active game");
            }

            if (gameState.isGameOver()) {
                throw new InvalidMoveException("Game is already over");
            }

            if (!playerId.equals(gameState.getCurrentTurnPlayerId())) {
                throw new InvalidMoveException("It's not your turn");
            }

            Trick currentTrick = gameState.getTrick();
            if (!currentTrick.hasCards()) {
                throw new InvalidMoveException("Cannot pass on first play of trick");
            }

            // Validate that both players have played equal number of cards
            int leadingCount = currentTrick.getLeadingPlayerCardCount();
            int totalCards = currentTrick.getCards().size();
            int respondingCount = totalCards - leadingCount;

            String leadingPlayerId = currentTrick.getLeadingPlayerId();
            boolean isLeadingPlayer = playerId.equals(leadingPlayerId);

            // If responding player hasn't matched the leading count yet, they cannot pass
            if (!isLeadingPlayer && respondingCount < leadingCount) {
                throw new InvalidMoveException("You must play " + (leadingCount - respondingCount) +
                        " more card(s) to match opponent's count");
            }

            logger.info("Player {} passes turn - trick will be resolved", playerId);

            // Player is passing - resolve the trick immediately
            resolveTrick();

            gameState.updateLastModified();
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void endGame(boolean cancelled) {
        stateLock.lock();
        try {
            if (gameState != null) {
                gameState.setGameStatus(cancelled ? GameStatus.CANCELLED : GameStatus.COMPLETED);
                logger.info("Game ended. Status: {}", gameState.getGameStatus());
            }
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void resetGame() {
        stateLock.lock();
        try {
            gameState = null;
            logger.info("Game engine reset");
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public void restoreState(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("Cannot restore null game state");
        }

        stateLock.lock();
        try {
            this.gameState = state;
            logger.info("Game state restored: {} players, status: {}",
                    state.getPlayers().size(), state.getGameStatus());
        } finally {
            stateLock.unlock();
        }
    }
}
