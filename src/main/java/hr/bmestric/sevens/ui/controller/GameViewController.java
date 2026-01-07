package hr.bmestric.sevens.ui.controller;

import hr.bmestric.sevens.AppContext;
import hr.bmestric.sevens.engine.InvalidMoveException;
import hr.bmestric.sevens.engine.interfaces.IGameEngine;
import hr.bmestric.sevens.events.GameEvent;
import hr.bmestric.sevens.events.interfaces.IGameEventListener;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.model.Trick;
import hr.bmestric.sevens.model.enums.PlayerType;
import hr.bmestric.sevens.model.enums.Rank;
import hr.bmestric.sevens.model.enums.Suit;
import hr.bmestric.sevens.network.chat.interfaces.IChatService;
import hr.bmestric.sevens.network.rmi.interfaces.IRemoteGameEngine;
import hr.bmestric.sevens.persistence.AsyncStorageService;
import hr.bmestric.sevens.persistence.ObjectStorageService;
import hr.bmestric.sevens.session.interfaces.IChatSession;
import hr.bmestric.sevens.session.interfaces.IGameSession;
import hr.bmestric.sevens.ui.service.FxDialogService;
import hr.bmestric.sevens.ui.service.GameFileDialogService;
import hr.bmestric.sevens.ui.service.ISessionFactory;
import hr.bmestric.sevens.ui.service.UiServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;

public class GameViewController implements IGameEventListener<GameEvent> {
    private static final Logger logger = LoggerFactory.getLogger(GameViewController.class);
    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 120;

    private static final String NETWORK_ERROR_PREFIX = "Network error: ";
    private static final String NETWORK_ERROR_LOG = "Network error";
    private static final String UI_FONT_FAMILY = "Arial";

    @FXML
    private Label statusLabel;
    @FXML private Label deckSizeLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label opponentNameLabel;
    @FXML private Label opponentScoreLabel;
    @FXML private FlowPane opponentHandPane;
    @FXML private Label playerNameLabel;
    @FXML private Label playerScoreLabel;
    @FXML private FlowPane playerHandPane;
    @FXML private FlowPane trickPilePane;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private Label gameInfoLabel;
    @FXML private Button newGameButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Button passButton;
    @FXML private Button endButton;

    private final AppContext appContext;
    private final IGameEngine gameEngine;

    private IRemoteGameEngine remoteEngine;
    private boolean isRmiMode = false;

    private IChatSession chatSession;

    private Player localPlayer;
    private Player opponentPlayer;

    private IGameSession session;

    private FxDialogService dialogService = new FxDialogService();
    private GameFileDialogService fileDialogService = new GameFileDialogService();


    private ISessionFactory sessionFactory;

    private AsyncStorageService storageService = new AsyncStorageService(new ObjectStorageService());

    public GameViewController() {
        this.appContext = AppContext.getInstance();
        this.gameEngine = appContext.getGameEngine();
        this.sessionFactory = new hr.bmestric.sevens.ui.service.DefaultSessionFactory();
        this.session = sessionFactory.createLocalGameSession(this.gameEngine);
        this.session.setStateListener(state -> Platform.runLater(this::updateUI));
    }

    public void setUiServices(UiServices services) {
        if (services == null) {
            return;
        }
        this.dialogService = services.dialogs();
        this.fileDialogService = services.fileDialogs();
        this.storageService = services.storage();
        this.sessionFactory = services.sessions();
    }

    public void setLocalPlayer(Player player) {
        this.localPlayer = player;
        if (playerNameLabel != null) {
            updatePlayerInfo();
        }
    }

    public void setRemoteEngine(IRemoteGameEngine remoteEngine, String clientId) {
        this.remoteEngine = remoteEngine;
        this.isRmiMode = true;

        IGameSession newSession = null;
        try {
            newSession = sessionFactory.createRmiGameSession(remoteEngine, clientId);
            newSession.setStateListener(state -> Platform.runLater(this::updateUI));

            if (newSession instanceof hr.bmestric.sevens.session.RmiGameSession rmiSession) {
                rmiSession.connect();
            }

            closeQuietly(this.session);
            this.session = newSession;
            logger.info("RMI session initialized for client: {}", clientId);
        } catch (Exception e) {
            closeQuietly(newSession);
            logger.error("Failed to initialize RMI session", e);
        }
    }

    public void setChatService(IChatService chatService, String playerId) {
        IChatSession newChat = null;
        try {
            if (localPlayer == null) {
                localPlayer = new Player(playerId, playerId, PlayerType.LOCAL);
            }

            newChat = sessionFactory.createRmiChatSession(chatService, localPlayer);
            newChat.setMessageListener((fromPlayerName, message, timestamp) -> {
                if (localPlayer != null && fromPlayerName.equals(localPlayer.getDisplayName())) {
                    return;
                }
                Platform.runLater(() -> {
                    String formattedMessage = String.format("[%s] %s: %s%n", timestamp, fromPlayerName, message);
                    chatArea.appendText(formattedMessage);
                });
            });

            if (newChat instanceof hr.bmestric.sevens.session.RmiChatSession rmiChat) {
                rmiChat.connect();
            }

            closeQuietly(this.chatSession);
            this.chatSession = newChat;
        } catch (RemoteException | RuntimeException e) {
            closeQuietly(newChat);
            logger.error("Failed to initialize chat session", e);
            this.chatSession = null;
        }
    }

    @FXML
    private void initialize() {
        appContext.getEventBus().subscribe(GameEvent.class, this);
        updateUI();
    }

    @FXML
    private void onNewGame() {
        try {
            if (session != null) {
                session.resetAndRestartIfPossible();
                dialogService.showInfo("New Game", "Game restarted!");
                if (!isRmiMode) {
                    updateUI();
                }
            }
        } catch (RemoteException e) {
            logger.error("RMI error starting new game", e);
            dialogService.showError(NETWORK_ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            logger.error("Error starting new game", e);
            dialogService.showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveGame() {
        try {
            GameState state = (session != null) ? session.getState() : null;
            if (state == null) {
                dialogService.showError("No game to save");
                return;
            }

            FileChooser fileChooser = fileDialogService.createSaveChooser();
            File file = fileChooser.showSaveDialog(playerHandPane.getScene().getWindow());
            if (file == null) {
                return;
            }

            Path path = file.toPath();
            storageService.saveGameAsync(state, path)
                    .thenRun(() -> dialogService.showInfo("Game Saved", "Game saved successfully to: " + file.getName()))
                    .exceptionally(ex -> {
                        dialogService.showError("Failed to save game: " + rootMessage(ex));
                        return null;
                    });

        } catch (RemoteException e) {
            logger.error("RMI error saving game", e);
            dialogService.showError(NETWORK_ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            logger.error("Error saving game", e);
            dialogService.showError("Failed to save game: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadGame() {
        try {
            FileChooser fileChooser = fileDialogService.createLoadChooser();
            File file = fileChooser.showOpenDialog(playerHandPane.getScene().getWindow());
            if (file == null) {
                return;
            }

            Path path = file.toPath();
            storageService.loadGameAsync(path)
                    .thenAccept(loadedState -> Platform.runLater(() -> applyLoadedState(loadedState, file)))
                    .exceptionally(ex -> {
                        dialogService.showError("Failed to load game: " + rootMessage(ex));
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Error opening load dialog", e);
            dialogService.showError("Failed to load game: " + e.getMessage());
        }
    }

    private void applyLoadedState(GameState loadedState, File file) {
        try {
            session.restoreState(loadedState);

            if (localPlayer != null) {
                localPlayer = loadedState.getPlayerById(localPlayer.getId()).orElse(localPlayer);
            }

            dialogService.showInfo("Game Loaded", "Game loaded successfully from: " + file.getName());
            updateUI();
        } catch (Exception ex) {
            dialogService.showError("Failed to apply loaded game: " + rootMessage(ex));
        }
    }

    @FXML
    private void onPassTurn() {
        if (localPlayer == null) return;

        try {
            if (session != null) {
                session.passTurn(localPlayer.getId());
            }
        } catch (InvalidMoveException e) {
            showError("Cannot pass: " + e.getMessage());
        } catch (RemoteException e) {
            logger.error(NETWORK_ERROR_LOG, e);
            showError(NETWORK_ERROR_PREFIX + e.getMessage());
        }
    }

    @FXML
    private void onEndGame() {
        try {
            session.endGame(true);
        } catch (RemoteException e) {
            logger.error(NETWORK_ERROR_LOG, e);
            showError(NETWORK_ERROR_PREFIX + e.getMessage());
        }
    }

    @FXML
    private void onSendChat() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        if (localPlayer == null) {
            return;
        }

        try {
            if (chatSession != null && opponentPlayer != null) {
                chatSession.sendMessage(localPlayer.getId(), localPlayer.getDisplayName(), message);
                logger.info("Chat message sent via RMI: {}", message);
            }

            String timestamp = java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String formattedMessage = String.format("[%s] %s: %s%n",
                    timestamp, localPlayer.getDisplayName(), message);
            chatArea.appendText(formattedMessage);

            chatInput.clear();
        } catch (RemoteException e) {
            logger.error("Failed to send chat message", e);
            showError("Failed to send message: " + e.getMessage());
        }
    }

    private void updateUI() {
        try {
            // Get state from appropriate source
            GameState state;
            if (session != null) {
                state = session.getState();
            } else if (isRmiMode && remoteEngine != null) {
                state = remoteEngine.getGameState();
            } else {
                state = gameEngine.getState();
            }

            if (state == null) {
                statusLabel.setText(isRmiMode ? "Waiting for game to start..." : "No active game");
                return;
            }

            if (localPlayer != null) {
                localPlayer = state.getPlayerById(localPlayer.getId()).orElse(localPlayer);
            }

            statusLabel.setText(state.getGameStatus().toString());
            deckSizeLabel.setText("Deck: " + state.getDeck().remaining());

            String currentPlayerId = state.getCurrentTurnPlayerId();
            state.getPlayerById(currentPlayerId).ifPresent(player ->
                    currentPlayerLabel.setText(player.getDisplayName() + "'s turn")
            );

            opponentPlayer = (localPlayer != null)
                    ? state.getOpponent(localPlayer.getId()).orElse(null)
                    : null;

            updatePlayerInfo();

            updateHands();

            updateTrickPile(state);

            updatePassButtonVisibility(state);

            updateGameInfo(state);
        } catch (RemoteException e) {
            logger.error("RMI error updating UI", e);
            statusLabel.setText("Connection error");
        } catch (Exception e) {
            logger.error("Error updating UI", e);
        }
    }

    private void updatePlayerInfo() {
        if (localPlayer != null) {
            playerNameLabel.setText(localPlayer.getDisplayName() + " (You)");
            playerScoreLabel.setText("Score: " + localPlayer.getScore());
        }

        if (opponentPlayer != null) {
            opponentNameLabel.setText(opponentPlayer.getDisplayName());
            opponentScoreLabel.setText("Score: " + opponentPlayer.getScore());
        }
    }

    private void updateHands() {
        playerHandPane.getChildren().clear();
        if (localPlayer != null) {
            for (Card card : localPlayer.getHand().getCards()) {
                Button cardButton = createCardButton(card);
                playerHandPane.getChildren().add(cardButton);
            }
        }

        opponentHandPane.getChildren().clear();
        if (opponentPlayer != null) {
            for (int i = 0; i < opponentPlayer.getHand().size(); i++) {
                Label cardBack = createCardBack();
                opponentHandPane.getChildren().add(cardBack);
            }
        }
    }

    private void updateTrickPile(GameState state) {
        trickPilePane.getChildren().clear();

        for (Card card : state.getTrick().getCards()) {
            Label cardLabel = createCardLabel(card);
            trickPilePane.getChildren().add(cardLabel);
        }
    }

    private void updatePassButtonVisibility(GameState state) {
        if (localPlayer == null || state == null) {
            setPassButtonVisible(false);
            return;
        }

        boolean shouldShow = canLocalPlayerPassOrContinue(state);
        setPassButtonVisible(shouldShow);
    }

    private boolean canLocalPlayerPassOrContinue(GameState state) {
        if (!localPlayer.getId().equals(state.getCurrentTurnPlayerId())) {
            return false;
        }

        Trick currentTrick = state.getTrick();
        if (!currentTrick.hasCards()) {
            return false;
        }

        List<Card> trickCards = currentTrick.getCards();
        if (trickCards.isEmpty()) {
            return false;
        }

        Rank leadingRank = trickCards.getFirst().getRank();
        return localPlayer.getHand().containsRank(leadingRank)
                || localPlayer.getHand().containsRank(Rank.SEVEN);
    }

    private void setPassButtonVisible(boolean visible) {
        passButton.setVisible(visible);
        passButton.setManaged(visible);
    }

    private void updateGameInfo(GameState state) {
        String info = "Turn: " + (localPlayer.getId().equals(state.getCurrentTurnPlayerId()) ? "YOUR TURN" : "Opponent's turn")
                + "\nCards in trick: " + state.getTrick().getCards().size();
        gameInfoLabel.setText(info);
    }

    private Button createCardButton(Card card) {
        Button button = new Button(formatCard(card));
        button.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        button.setFont(Font.font(UI_FONT_FAMILY, FontWeight.BOLD, 20));

        Color color = getSuitColor(card.getSuit());
        button.setTextFill(color);
        button.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 5;");

        button.setOnAction(e -> onCardClicked(card));
        return button;
    }

    private Label createCardBack() {
        Label label = new Label("🂠");
        label.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setFont(Font.font(UI_FONT_FAMILY, FontWeight.BOLD, 48));
        label.setStyle("-fx-background-color: #1565c0; -fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 5;");
        return label;
    }

    private Label createCardLabel(Card card) {
        Label label = new Label(formatCard(card));
        label.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setFont(Font.font(UI_FONT_FAMILY, FontWeight.BOLD, 24));
        label.setTextFill(getSuitColor(card.getSuit()));
        label.setStyle("-fx-background-color: white; -fx-border-color: gold; -fx-border-width: 3; -fx-border-radius: 5;");
        return label;
    }

    private void onCardClicked(Card card) {
        if (localPlayer == null) return;

        try {
            GameState state = session.getState();
            if (state == null) return;

            if (!localPlayer.getId().equals(state.getCurrentTurnPlayerId())) {
                showError("It's not your turn!");
                return;
            }

            session.playCard(localPlayer.getId(), card);

            if (!isRmiMode) {
                updateUI();
            }
        } catch (InvalidMoveException e) {
            showError("Invalid move: " + e.getMessage());
        } catch (RemoteException e) {
            logger.error(NETWORK_ERROR_LOG, e);
            showError(NETWORK_ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            logger.error("Error playing card", e);
            showError("Error: " + e.getMessage());
        }
    }

    private String formatCard(Card card) {
        return card.getRank().getSymbol() + "\n" + card.getSuit().getSymbol();
    }

    private Color getSuitColor(Suit suit) {
        return switch (suit) {
            case HEARTS, DIAMONDS -> Color.RED;
            case CLUBS, SPADES -> Color.BLACK;
        };
    }

    private void showError(String message) {
        dialogService.showError(message);
    }


    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage() != null ? cur.getMessage() : cur.toString();
    }

    @Override
    public void onEvent(GameEvent event) {
        Platform.runLater(this::updateUI);
    }

    public void shutdown() {
        closeQuietly(chatSession);
        closeQuietly(session);
        try {
            storageService.shutdown();
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // best-effort
        }
    }
}
