package common;

import common.Cards.PebbleColor;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Cards and PebbleColor.
 *
 * Test organization mirrors the public API:
 *   1. PebbleColor enum
 *   2. Card construction (createCard / constructor)
 *   3. canAcquire
 *   4. score
 *   5. render
 *   6. equals / hashCode
 */
class CardsTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Builds a player pebble map from alternating (color, count) varargs. */
    private static Map<PebbleColor, Integer> pebbles(Object... pairs) {
        Map<PebbleColor, Integer> map = new EnumMap<>(PebbleColor.class);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((PebbleColor) pairs[i], (Integer) pairs[i + 1]);
        }
        return map;
    }

    /** Convenience: plain (no-star) card from varargs colors. */
    private static Cards plainCard(PebbleColor... colors) {
        return Cards.createCard(List.of(colors), false);
    }

    /** Convenience: star card from varargs colors. */
    private static Cards starCard(PebbleColor... colors) {
        return Cards.createCard(List.of(colors), true);
    }

    // -------------------------------------------------------------------------
    // 1. PebbleColor
    // -------------------------------------------------------------------------

    @Test
    void pebbleColor_allFiveColorsExist() {
        assertEquals(5, PebbleColor.values().length);
    }

    @Test
    void pebbleColor_abbreviationsAreDistinct() {
        long distinct = List.of(PebbleColor.values()).stream()
                .map(PebbleColor::abbreviation)
                .distinct()
                .count();
        assertEquals(5, distinct, "Each color must have a unique abbreviation");
    }

    @Test
    void pebbleColor_abbreviationsAreSingleCharacter() {
        for (PebbleColor c : PebbleColor.values()) {
            assertEquals(1, c.abbreviation().length(),
                    c + " abbreviation must be exactly one character");
        }
    }

    @Test
    void pebbleColor_hexColorsMatchRgbFormat() {
        for (PebbleColor c : PebbleColor.values()) {
            assertTrue(c.hexColor().matches("#[0-9A-Fa-f]{6}"),
                    c + " hex color should be #RRGGBB format, got: " + c.hexColor());
        }
    }

    @Test
    void pebbleColor_knownHexValues() {
        assertEquals("#E74C3C", PebbleColor.RED.hexColor());
        assertEquals("#ECF0F1", PebbleColor.WHITE.hexColor());
        assertEquals("#3498DB", PebbleColor.BLUE.hexColor());
        assertEquals("#2ECC71", PebbleColor.GREEN.hexColor());
        assertEquals("#F1C40F", PebbleColor.YELLOW.hexColor());
    }

    // -------------------------------------------------------------------------
    // 2. Card construction
    // -------------------------------------------------------------------------

    @Test
    void construction_succeedsWithExactlyFivePebbles() {
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        assertNotNull(card);
        assertEquals(5, card.getPebbles().size());
    }

    @Test
    void construction_allowsDuplicateColors() {
        // A card may repeat the same color across its five slots
        assertDoesNotThrow(() -> plainCard(
                PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED));
    }

    @Test
    void construction_rejectsFourPebbles() {
        assertThrows(IllegalArgumentException.class, () ->
                Cards.createCard(List.of(
                        PebbleColor.RED, PebbleColor.BLUE,
                        PebbleColor.GREEN, PebbleColor.WHITE), false));
    }

    @Test
    void construction_rejectsSixPebbles() {
        assertThrows(IllegalArgumentException.class, () ->
                Cards.createCard(List.of(
                        PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                        PebbleColor.RED, PebbleColor.RED, PebbleColor.BLUE), false));
    }

    @Test
    void construction_rejectsNullList() {
        assertThrows(IllegalArgumentException.class, () ->
                Cards.createCard(null, false));
    }

    @Test
    void construction_getPebblesIsImmutable() {
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        assertThrows(UnsupportedOperationException.class, () ->
                card.getPebbles().add(PebbleColor.RED));
    }

    @Test
    void construction_hasStarReflectsArgument() {
        assertFalse(plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED).hasStar());
        assertTrue(starCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED).hasStar());
    }

    @Test
    void construction_factoryMatchesConstructor() {
        List<PebbleColor> pebs = List.of(
                PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        assertEquals(new Cards(pebs, true), Cards.createCard(pebs, true));
    }

    // -------------------------------------------------------------------------
    // 3. canAcquire
    // -------------------------------------------------------------------------

    @Test
    void canAcquire_trueWhenPlayerHasExactPebbles() {
        // Card: R R B G Y — player has exactly those
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.RED, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        assertTrue(card.canAcquire(pebbles(
                PebbleColor.RED, 2, PebbleColor.BLUE, 1,
                PebbleColor.GREEN, 1, PebbleColor.YELLOW, 1)));
    }

    @Test
    void canAcquire_trueWhenPlayerHasSurplus() {
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        assertTrue(card.canAcquire(pebbles(
                PebbleColor.RED, 5, PebbleColor.WHITE, 5, PebbleColor.BLUE, 5,
                PebbleColor.GREEN, 5, PebbleColor.YELLOW, 5)));
    }

    @Test
    void canAcquire_falseWhenMissingOneColor() {
        // Card needs BLUE; player has none
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        assertFalse(card.canAcquire(pebbles(
                PebbleColor.RED, 3, PebbleColor.WHITE, 3,
                PebbleColor.GREEN, 3, PebbleColor.YELLOW, 3)));
    }

    @Test
    void canAcquire_falseWhenInsufficientDuplicates() {
        // Card needs 3 RED; player only has 2
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.BLUE, PebbleColor.GREEN);
        assertFalse(card.canAcquire(pebbles(
                PebbleColor.RED, 2, PebbleColor.BLUE, 1, PebbleColor.GREEN, 1)));
    }

    @Test
    void canAcquire_falseForEmptyPlayerMap() {
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertFalse(card.canAcquire(Map.of()));
    }

    @Test
    void canAcquire_missingKeyTreatedAsZero() {
        // All-RED card; player map has no RED key at all
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertFalse(card.canAcquire(pebbles(PebbleColor.BLUE, 10)));
    }

    // -------------------------------------------------------------------------
    // 4. score — every cell of the scoring table
    // -------------------------------------------------------------------------

    @Test
    void score_plainCard_threeOrMorePebbles() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(1, card.score(3));
        assertEquals(1, card.score(10)); // well above threshold
    }

    @Test
    void score_plainCard_twoPebbles() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(2, card.score(2));
    }

    @Test
    void score_plainCard_onePebble() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(3, card.score(1));
    }

    @Test
    void score_plainCard_zeroPebbles() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(5, card.score(0));
    }

    @Test
    void score_starCard_threeOrMorePebbles() {
        Cards card = starCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(2, card.score(3));
        assertEquals(2, card.score(7));
    }

    @Test
    void score_starCard_twoPebbles() {
        Cards card = starCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(3, card.score(2));
    }

    @Test
    void score_starCard_onePebble() {
        Cards card = starCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(5, card.score(1));
    }

    @Test
    void score_starCard_zeroPebbles() {
        Cards card = starCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertEquals(8, card.score(0));
    }

    @Test
    void score_starAlwaysHigherThanPlainAtSameRemainder() {
        Cards plain = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        Cards star  = starCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        for (int r = 0; r <= 5; r++) {
            assertTrue(star.score(r) > plain.score(r),
                    "Star should score higher at " + r + " remaining pebbles");
        }
    }

    // -------------------------------------------------------------------------
    // 5. render
    // -------------------------------------------------------------------------

    @Test
    void render_containsAllFiveAbbreviations() {
        Cards card = plainCard(
                PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        String r = card.render();
        assertTrue(r.contains("R"), "Render should contain R");
        assertTrue(r.contains("W"), "Render should contain W");
        assertTrue(r.contains("B"), "Render should contain B");
        assertTrue(r.contains("G"), "Render should contain G");
        assertTrue(r.contains("Y"), "Render should contain Y");
    }

    @Test
    void render_wrappedInSquareBrackets() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        String r = card.render();
        assertTrue(r.startsWith("["), "Render should start with [");
        assertTrue(r.contains("]"),   "Render should contain ]");
    }

    @Test
    void render_plainCardHasNoStar() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertFalse(card.render().contains("★"),
                "Plain card render should not contain a star");
    }

    @Test
    void render_starCardContainsStar() {
        Cards card = starCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertTrue(card.render().contains("★"),
                "Star card render should contain ★");
    }

    @Test
    void render_toStringDelegatesToRender() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE,
                PebbleColor.GREEN, PebbleColor.YELLOW);
        assertEquals(card.render(), card.toString());
    }

    // -------------------------------------------------------------------------
    // 6. equals / hashCode
    // -------------------------------------------------------------------------

    @Test
    void equals_samePebblesAndStarStatusAreEqual() {
        Cards a = plainCard(PebbleColor.RED, PebbleColor.BLUE, PebbleColor.GREEN,
                PebbleColor.WHITE, PebbleColor.YELLOW);
        Cards b = plainCard(PebbleColor.RED, PebbleColor.BLUE, PebbleColor.GREEN,
                PebbleColor.WHITE, PebbleColor.YELLOW);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentStarStatusNotEqual() {
        Cards plain = plainCard(PebbleColor.RED, PebbleColor.BLUE, PebbleColor.GREEN,
                PebbleColor.WHITE, PebbleColor.YELLOW);
        Cards star  = starCard(PebbleColor.RED, PebbleColor.BLUE, PebbleColor.GREEN,
                PebbleColor.WHITE, PebbleColor.YELLOW);
        assertNotEquals(plain, star);
    }

    @Test
    void equals_differentPebblesNotEqual() {
        Cards a = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        Cards b = plainCard(PebbleColor.BLUE, PebbleColor.BLUE, PebbleColor.BLUE,
                PebbleColor.BLUE, PebbleColor.BLUE);
        assertNotEquals(a, b);
    }

    @Test
    void equals_reflexive() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.BLUE, PebbleColor.GREEN,
                PebbleColor.WHITE, PebbleColor.YELLOW);
        assertEquals(card, card);
    }

    @Test
    void equals_notEqualToNull() {
        Cards card = plainCard(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED);
        assertNotEquals(null, card);
    }

    @Test
    void equals_orderOfPebblesMatters() {
        // R B G W Y  ≠  B R G W Y  (order is preserved in the list)
        Cards a = plainCard(PebbleColor.RED,  PebbleColor.BLUE, PebbleColor.GREEN,
                PebbleColor.WHITE, PebbleColor.YELLOW);
        Cards b = plainCard(PebbleColor.BLUE, PebbleColor.RED,  PebbleColor.GREEN,
                PebbleColor.WHITE, PebbleColor.YELLOW);
        assertNotEquals(a, b);
    }
}