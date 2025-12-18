package hr.bmestric.sevens.engine;

import hr.bmestric.sevens.engine.interfaces.ITrickResolver;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.model.Trick;
import hr.bmestric.sevens.model.enums.Rank;

import java.util.List;

public class TrickResolver implements ITrickResolver {
    @Override
    public String determineTrickWinner(Trick trick, String player1Id, String player2Id) {
        List<Card> cards = trick.getCards();
        if(cards.isEmpty()) {
            throw new IllegalArgumentException("Cannot determine winner of an empty trick.");
        }

        String leadingPlayerId = trick.getLeadingPlayerId();
        String respondingPlayerId = leadingPlayerId.equals(player1Id) ? player2Id : player1Id;

        int leadingCardCount = trick.getLeadingPlayerCardCount();

        // If only leading player has played
        if (leadingCardCount == cards.size()) {
            return leadingPlayerId;
        }

        String currentWinner = leadingPlayerId;
        Rank initialRank = cards.getFirst().getRank();

        // Process all cards in the trick to find who played the last trump or matching rank
        for (int i = 1; i < cards.size(); i++) {
            Card card = cards.get(i);
            String playerId = (i < leadingCardCount) ? leadingPlayerId : respondingPlayerId;

            if (card.getRank().isTrump() || card.getRank() == initialRank) {
                currentWinner = playerId;
            }
        }

        return currentWinner;
    }

    @Override
    public int caluclateTrickPoints(Trick trick) {
        return trick.calculatePoints();
    }

    @Override
    public boolean isTrickComplete(Trick trick, GameState gameState) {
        if(!trick.hasCards()) {
            return false;
        }

        List<Card> cards = trick.getCards();

        if(cards.size() < 2) {
            return false;
        }

        int leadingCardCount = trick.getLeadingPlayerCardCount();
        String leadingPlayerId = trick.getLeadingPlayerId();

        int respondingCardCount = cards.size() - leadingCardCount;

        String respondingPlayerId = gameState.getPlayers().stream()
                .map(Player::getId)
                .filter(id -> !id.equals(leadingPlayerId))
                .findFirst()
                .orElse(null);

        if(respondingPlayerId == null) {
            return false;
        }

        Player leadingPlayer = gameState.getPlayerById(leadingPlayerId).orElse(null);
        Player respondingPlayer = gameState.getPlayerById(respondingPlayerId).orElse(null);

        if(leadingPlayer == null || respondingPlayer == null) {
            return false;
        }

        Rank leadingRank = cards.getFirst().getRank();

        if(respondingCardCount < leadingCardCount) {
            return false;
        }

        Card lastRespondingCard = cards.get(leadingCardCount + respondingCardCount - 1);

        //no match and no trump played
        if(lastRespondingCard.getRank() != leadingRank && lastRespondingCard.getRank() != Rank.SEVEN) {
            return true;
        }

        // matched, check if leading player can continue
        boolean leadingPlayerCanContinue = leadingPlayer.getHand().containsRank(leadingRank)
                || leadingPlayer.getHand().containsRank(Rank.SEVEN);

        return !leadingPlayerCanContinue;
    }
}
