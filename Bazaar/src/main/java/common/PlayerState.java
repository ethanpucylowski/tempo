package referee;

import common.Cards.PebbleColor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents the referee's view of a single player's state
 *
 * Data representation:
 *   - name:   a display name for the player
 *   - wallet: a map from PebbleColor to count representing the player's pebbles
 *   - score:  the player's current score (number of points from purchased cards)
 *   - active: whether the player is still in the game
 */
public class PlayerState {

    private final String name;
    private final Map<PebbleColor, Integer> wallet;
    private int score;
    private boolean active;

    /**
     * Creates a new PlayerState.
     *
     * @param name   the player's display name
     * @param wallet the player's initial pebble counts
     * @param score  the player's initial score
     * @param active whether the player starts as active
     */
    public PlayerState(String name, Map<PebbleColor, Integer> wallet, int score, boolean active) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Player name must not be blank.");
        if (wallet == null)
            throw new IllegalArgumentException("Wallet must not be null.");
        this.name   = name;
        this.wallet = new EnumMap<>(wallet);
        this.score  = score;
        this.active = active;
    }

    // Accessors
    public String getName()                          { return name; }
    public Map<PebbleColor, Integer> getWallet()     { return Collections.unmodifiableMap(wallet); }
    public int getScore()                            { return score; }
    public boolean isActive()                        { return active; }

    // Mutators (used by referee during game play)
    public void setScore(int score)                  { this.score = score; }
    public void setActive(boolean active)            { this.active = active; }

    @Override
    public String toString() {
        return name + " | score=" + score + " | active=" + active + " | wallet=" + wallet;
    }
}
