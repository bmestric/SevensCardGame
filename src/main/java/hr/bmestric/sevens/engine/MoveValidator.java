package hr.bmestric.sevens.engine;

import hr.bmestric.sevens.engine.interfaces.IMoveValidator;
import hr.bmestric.sevens.model.*;
import hr.bmestric.sevens.model.enums.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MoveValidator implements IMoveValidator {
    private static final Logger logger = LoggerFactory.getLogger(MoveValidator.class);
    @Override
    public MoveValidationResult validate(GameState gameState, String playerId, List<Card> cards) {
        if(cards == null || cards.isEmpty()) {
            return MoveValidationResult.invalid("No cards played");
        }

        if(!playerId.equals(gameState.getCurrentTurnPlayerId())) {
            return MoveValidationResult.invalid("It's not your turn");
        }

        Player player = gameState.getPlayerById(playerId).orElseThrow(null);
        if(player == null) {
            return MoveValidationResult.invalid("Player not found");
        }

        for(Card card : cards) {
            if (!player.getHand().contains(card)) {
                return MoveValidationResult.invalid("Player does not have the card: " + card);
            }
        }
            Trick currentTrick = gameState.getTrick();
            // If this is the first play in the trick (leading)
            if (!currentTrick.hasCards()) {
                return validateLeadingPlay(cards);
            }
            return validateResponsePlay(gameState, player, cards, currentTrick);
    }

    private MoveValidationResult validateResponsePlay(
            GameState gameState, Player player, List<Card> cards, Trick currentTrick) {
        List<Card> trickCards = currentTrick.getCards();
        Rank leadingRank = trickCards.get(0).getRank();
        int leadingCount = currentTrick.getLeadingPlayerCardCount();
        int totalCards = trickCards.size();
        int respondingCount = totalCards - leadingCount;

        String leadingPlayerId = currentTrick.getLeadingPlayerId();
        boolean isLeadingPlayer = player.getId().equals(leadingPlayerId);

        Rank responseRank = cards.get(0).getRank();
        for (Card card : cards) {
            if(card.getRank() != responseRank) {
                return MoveValidationResult.invalid("All cards must be the same rank when playing multiple cards");
            }
        }

        if (isLeadingPlayer && respondingCount >= leadingCount) {
            // Can ONLY play matching rank OR trump (7)
            if (responseRank != leadingRank && responseRank != Rank.SEVEN) {
                return MoveValidationResult.invalid(
                        "When continuing a trick, you must play " + leadingRank + " or 7 (trump), or use Pass/Skip button");
            }
            // Valid continuation
            return MoveValidationResult.valid();
        }

        if (responseRank == leadingRank) {
            // Must play the same number of cards OR all cards of that rank they have
            List<Card> matchingCardsInHand = player.getHand().getCardsWithRank(leadingRank);

            if (cards.size() != leadingCount) {
                // Allow playing fewer if that's all they have
                if (cards.size() == matchingCardsInHand.size()) {
                    logger.debug("Player playing all {} cards of rank {} (less than required {})",
                            cards.size(), leadingRank, leadingCount);
                    return MoveValidationResult.valid();
                }
                return MoveValidationResult.invalid(
                        String.format("Must play %d cards of rank %s to match opponent", leadingCount, leadingRank));
            }
            return MoveValidationResult.valid();
        }

        // If not matching rank, can only play a single card
        if (cards.size() > 1) {
            return MoveValidationResult.invalid("Can only play multiple cards if matching opponent's rank");
        }

        return MoveValidationResult.valid();
    }

    private MoveValidationResult validateLeadingPlay(List<Card> cards) {
        if(cards.size() > Hand.getMaxHandSize()) {
            return MoveValidationResult.invalid("Cannot play more than " + Hand.getMaxHandSize() + " cards");
        }

        Rank firstRank = cards.get(0).getRank();
        for (Card card : cards) {
            if (card.getRank() != firstRank) {
                return MoveValidationResult.invalid("All cards must be the same rank when playing multiple cards");
            }
        }

        return MoveValidationResult.valid();
    }

    @Override
    public boolean mustRespondToSequence(GameState gameState, String playerId) {
        Trick currentTrick = gameState.getTrick();
        if (!currentTrick.hasCards()) {
            return false;
        }

        // Get player's hand
        Player player = gameState.getPlayerById(playerId).orElse(null);
        if (player == null) {
            return false;
        }

        // Check if opponent played multiple cards
        List<Card> leadingCards = currentTrick.getCards();
        if (leadingCards.size() <= 1) {
            return false;
        }

        // Check if player has matching rank
        Rank leadingRank = leadingCards.get(0).getRank();
        return player.getHand().containsRank(leadingRank);
    }
}
