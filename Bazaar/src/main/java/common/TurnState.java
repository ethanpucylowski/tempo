package common;

import common.Cards.PebbleColor;
import common.PlayerState;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the information the referee sends to the player at the
 * start of their turn. This is a snapshot i.e. players cannot see the full game state
 *
 * Data representation:
 *   - bank:         current pebble counts in the bank
 *   - activePlayer: the active player's own state
 *   - otherScores:  scores of all other active players
 *   - visibleCards: the current cards available for purchase
 */
public class TurnState {

    private final Map<PebbleColor, Integer> bank;
    private final PlayerState activePlayer;
    private final List<Integer> otherScores;
    private final List<Cards> visibleCards;

    /**
     * Creates a TurnState snapshot.
     *
     * @param bank         the bank's current pebble counts
     * @param activePlayer the active player's state
     * @param otherScores  scores of other active players
     * @param visibleCards the currently visible cards
     */
    public TurnState(
            Map<PebbleColor, Integer> bank,
            PlayerState activePlayer,
            List<Integer> otherScores,
            List<Cards> visibleCards) {
        if (bank == null)         throw new IllegalArgumentException("Bank must not be null.");
        if (activePlayer == null) throw new IllegalArgumentException("Active player must not be null.");
        if (otherScores == null)  throw new IllegalArgumentException("Other scores must not be null.");
        if (visibleCards == null) throw new IllegalArgumentException("Visible cards must not be null.");

        this.bank         = Collections.unmodifiableMap(new EnumMap<>(bank));
        this.activePlayer = activePlayer;
        this.otherScores  = Collections.unmodifiableList(otherScores);
        this.visibleCards = Collections.unmodifiableList(visibleCards);
    }

    /**
     * Renders a human-readable summary of the turn state to be shown to the player.
     *
     * Example output:
     *   === Turn State ===
     *   Your wallet: RED=2 BLUE=1
     *   Your score:  7
     *   Bank:        RED=3 WHITE=5 BLUE=2 GREEN=4 YELLOW=1
     *   Other scores: [4, 6]
     *   Visible Cards:
     *     [ R W B G Y ]
     *     [ R R W B G ]  ★
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Turn State ===\n");

        sb.append("Your wallet: ").append(WalletManager.toString(activePlayer.getWallet())).append("\n");
        sb.append("Your score:  ").append(activePlayer.getScore()).append("\n");
        sb.append("Bank:        ").append(WalletManager.toString(bank)).append("\n");
        sb.append("Other scores: ").append(otherScores).append("\n");

        sb.append("Visible Cards:\n");
        if (visibleCards.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (Cards c : visibleCards) {
                sb.append("  ").append(c.render()).append("\n");
            }
        }

        return sb.toString();
    }

    // Accessors
    public Map<PebbleColor, Integer> getBank()   { return bank; }
    public PlayerState getActivePlayer()          { return activePlayer; }
    public List<Integer> getOtherScores()         { return otherScores; }
    public List<Cards> getVisibleCards()          { return visibleCards; }

    @Override
    public String toString() {
        return render();
    }
}