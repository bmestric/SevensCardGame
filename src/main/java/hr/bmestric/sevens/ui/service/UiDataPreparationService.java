package hr.bmestric.sevens.ui.service;

import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.ui.model.UIViewModel;
import hr.bmestric.sevens.util.ThreadingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class UiDataPreparationService {
    private static final Logger logger = LoggerFactory.getLogger(UiDataPreparationService.class);

    private final ExecutorService uiPrepExecutor;

    public UiDataPreparationService() {
        this(ThreadingHelper.createSingleThreadExecutor("UI-Prep"));
    }

    public UiDataPreparationService(ExecutorService executor) {
        this.uiPrepExecutor = executor;
        logger.info("UiDataPreparationService initialized");
    }

    /**
     * Prepare complete UI view model from game state.
     * Runs in background thread to avoid blocking UI.
     */
    public CompletableFuture<UIViewModel> prepareViewModelAsync(GameState gameState, String localPlayerId) {
        logger.debug("Preparing UI view model asynchronously");

        return CompletableFuture.supplyAsync(() -> {
            if (gameState == null) {
                logger.warn("Cannot prepare view model: game state is null");
                return new UIViewModel();
            }

            UIViewModel model = new UIViewModel(gameState);

            try {
                // Prepare status text
                model.setStatusText(gameState.getGameStatus().toString());

                // Prepare deck info
                model.setDeckInfo("Deck: " + gameState.getDeck().remaining());

                // Prepare current player info
                String currentPlayerId = gameState.getCurrentTurnPlayerId();
                gameState.getPlayerById(currentPlayerId).ifPresent(player ->
                    model.setCurrentPlayerInfo(player.getDisplayName() + "'s turn")
                );

                // Prepare player hands
                gameState.getPlayerById(localPlayerId).ifPresent(localPlayer -> {
                    model.setLocalPlayerHand(localPlayer.getHand().getCards());
                    model.setLocalPlayerTurn(currentPlayerId.equals(localPlayerId));
                });

                // Prepare opponent hand
                gameState.getOpponent(localPlayerId).ifPresent(opponent ->
                    model.setOpponentHand(opponent.getHand().getCards())
                );

                // Prepare trick cards
                model.setTrickCards(gameState.getTrick().getCards());

                // Prepare scores
                Map<String, Integer> scores = new HashMap<>();
                for (Player player : gameState.getPlayers()) {
                    scores.put(player.getDisplayName(), player.getScore());
                }
                model.setPlayerScores(scores);

                // Game over status
                model.setGameOver(gameState.isGameOver());

                logger.debug("UI view model prepared successfully");
                return model;

            } catch (Exception e) {
                logger.error("Error preparing UI view model", e);
                throw new UiPreparationException("Failed to prepare UI data", e);
            }

        }, uiPrepExecutor);
    }

    /**
     * Prepare hand display data (for expensive card formatting).
     */
    public CompletableFuture<List<String>> prepareHandDisplayAsync(List<Card> cards) {
        return CompletableFuture.supplyAsync(() -> {
            return cards.stream()
                .map(Card::toString)
                .collect(Collectors.toList());
        }, uiPrepExecutor);
    }

    /**
     * Shutdown the executor service gracefully.
     */
    public void shutdown() {
        logger.info("Shutting down UiDataPreparationService");
        ThreadingHelper.shutdownGracefully(uiPrepExecutor, 3);
    }

    /**
     * Runtime exception for UI preparation errors.
     */
    public static class UiPreparationException extends RuntimeException {
        public UiPreparationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

