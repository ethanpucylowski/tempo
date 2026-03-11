package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a card in the Bazaar game.
 *
 * A card displays five pebbles arranged in a circle, optionally decorated
 * with a star in the center. Players purchase cards to earn points.
 *
 * Data representation:
 *   - pebbles: an ordered list of exactly 5 PebbleColor values
 *   - hasStar: whether the card has a star (worth more points)
 */
public class Cards {

    // -------------------------------------------------------------------------
    // Data Definitions
    // -------------------------------------------------------------------------

    /**
     * The five pebble colors in the Bazaar game.
     */
    public enum PebbleColor {
        RED, WHITE, BLUE, GREEN, YELLOW;

        /** Returns a single-character abbreviation for rendering. */
        public String abbreviation() {
            return this.name().substring(0, 1);
        }

        /** Returns a hex color string for graphical rendering. */
        public String hexColor() {
            switch (this) {
                case RED:    return "#E74C3C";
                case WHITE:  return "#ECF0F1";
                case BLUE:   return "#3498DB";
                case GREEN:  return "#2ECC71";
                case YELLOW: return "#F1C40F";
                default:     throw new IllegalStateException();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final List<PebbleColor> pebbles; // exactly 5 pebbles
    private final boolean hasStar;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a card with the given pebbles and star status.
     *
     * @param pebbles a list of exactly 5 PebbleColor values
     * @param hasStar true if this card has a star
     * @throws IllegalArgumentException if pebbles does not have exactly 5 elements
     */
    public Cards(List<PebbleColor> pebbles, boolean hasStar) {
        if (pebbles == null || pebbles.size() != 5) {
            throw new IllegalArgumentException("A card must have exactly 5 pebbles.");
        }
        this.pebbles = Collections.unmodifiableList(new ArrayList<>(pebbles));
        this.hasStar = hasStar;
    }

    // -------------------------------------------------------------------------
    // Functionality (public API)
    // -------------------------------------------------------------------------

    /**
     * (5) Creates a card with specific pebbles and star status.
     * Static factory method for convenient construction.
     *
     * @param pebbles list of exactly 5 PebbleColor values
     * @param hasStar whether the card has a star
     * @return a new Cards instance
     */
    public static Cards createCard(List<PebbleColor> pebbles, boolean hasStar) {
        return new Cards(pebbles, hasStar);
    }

    /**
     * (6) Determines whether a player can acquire this card with its pebbles.
     *
     * A player can acquire a card if their pebble collection contains
     * at least the multiset of pebbles shown on the card.
     *
     * @param playerPebbles a map from PebbleColor to count representing the player's pebbles
     * @return true if the player has enough pebbles to purchase this card
     */
    public boolean canAcquire(Map<PebbleColor, Integer> playerPebbles) {
        Map<PebbleColor, Integer> required = toPebbleMap(this.pebbles);
        for (Map.Entry<PebbleColor, Integer> entry : required.entrySet()) {
            int have = playerPebbles.getOrDefault(entry.getKey(), 0);
            if (have < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * (7) Renders this card graphically as an ASCII/text representation.
     *
     * Displays the 5 pebbles in a circular arrangement and indicates
     * whether the card has a star.
     *
     * Example output:
     *   [ R  W  B  G  Y ]  ★
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (PebbleColor p : pebbles) {
            sb.append(" ").append(colorBlock(p));
        }
        sb.append(" ]");
        if (hasStar) {
            sb.append("  ★");
        }
        return sb.toString();
    }

    /**
     * Computes the score earned when purchasing this card, based on
     * how many pebbles the player has left after the purchase.
     *
     * @param pebblesRemaining number of pebbles the player has after buying
     * @return the points earned
     */
    public int score(int pebblesRemaining) {
        if (hasStar) {
            if (pebblesRemaining >= 3) return 2;
            if (pebblesRemaining == 2) return 3;
            if (pebblesRemaining == 1) return 5;
            return 8; // 0 pebbles
        } else {
            if (pebblesRemaining >= 3) return 1;
            if (pebblesRemaining == 2) return 2;
            if (pebblesRemaining == 1) return 3;
            return 5; // 0 pebbles
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public List<PebbleColor> getPebbles() {
        return pebbles;
    }

    public boolean hasStar() {
        return hasStar;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a list of pebbles into a color → count map.
     */
    private static Map<PebbleColor, Integer> toPebbleMap(List<PebbleColor> pebbles) {
        Map<PebbleColor, Integer> map = new EnumMap<>(PebbleColor.class);
        for (PebbleColor p : pebbles) {
            map.merge(p, 1, Integer::sum);
        }
        return map;
    }

    /**
     * Returns a colored block character for terminal rendering.
     * Falls back to a letter abbreviation in plain text contexts.
     */
    private static String colorBlock(PebbleColor color) {
        return color.abbreviation();
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return render();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cards)) return false;
        Cards other = (Cards) o;
        return hasStar == other.hasStar && pebbles.equals(other.pebbles);
    }

    @Override
    public int hashCode() {
        return 31 * pebbles.hashCode() + (hasStar ? 1 : 0);
    }
}
