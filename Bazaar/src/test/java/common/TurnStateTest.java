package common;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import common.Cards.PebbleColor;
import referee.PlayerState;

@DisplayName("TurnState Unit Tests")
public class TurnStateTest {

    private Map<PebbleColor, Integer> standardBank;
    private PlayerState standardPlayer;
    private List<Integer> standardOtherScores;
    private List<Cards> standardVisibleCards;

    @BeforeEach
    public void setUp() {
        // Standard bank with mixed pebbles
        standardBank = new EnumMap<>(PebbleColor.class);
        standardBank.put(PebbleColor.RED, 3);
        standardBank.put(PebbleColor.WHITE, 5);
        standardBank.put(PebbleColor.BLUE, 2);
        standardBank.put(PebbleColor.GREEN, 4);
        standardBank.put(PebbleColor.YELLOW, 1);

        // Standard player with wallet and score
        Map<PebbleColor, Integer> playerWallet = new EnumMap<>(PebbleColor.class);
        playerWallet.put(PebbleColor.RED, 2);
        playerWallet.put(PebbleColor.BLUE, 1);
        standardPlayer = new PlayerState("TestPlayer", playerWallet, 7, true);

        // Standard other scores
        standardOtherScores = List.of(4, 6);

        // Standard visible cards
        standardVisibleCards = List.of(
            new Cards(List.of(PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE, 
                            PebbleColor.GREEN, PebbleColor.YELLOW), false),
            new Cards(List.of(PebbleColor.RED, PebbleColor.RED, PebbleColor.WHITE, 
                            PebbleColor.BLUE, PebbleColor.GREEN), true)
        );
    }

    // Constructor Tests

    @Test
    @DisplayName("Constructor succeeds with valid inputs")
    public void testConstructor_ValidInputs() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        assertNotNull(turnState);
        assertEquals(standardBank, turnState.getBank());
        assertEquals(standardPlayer, turnState.getActivePlayer());
        assertEquals(standardOtherScores, turnState.getOtherScores());
        assertEquals(standardVisibleCards, turnState.getVisibleCards());
    }

    @Test
    @DisplayName("Constructor throws when bank is null")
    public void testConstructor_NullBank() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TurnState(null, standardPlayer, standardOtherScores, standardVisibleCards)
        );
        assertEquals("Bank must not be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor throws when activePlayer is null")
    public void testConstructor_NullActivePlayer() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TurnState(standardBank, null, standardOtherScores, standardVisibleCards)
        );
        assertEquals("Active player must not be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor throws when otherScores is null")
    public void testConstructor_NullOtherScores() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TurnState(standardBank, standardPlayer, null, standardVisibleCards)
        );
        assertEquals("Other scores must not be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor throws when visibleCards is null")
    public void testConstructor_NullVisibleCards() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new TurnState(standardBank, standardPlayer, standardOtherScores, null)
        );
        assertEquals("Visible cards must not be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor accepts empty bank")
    public void testConstructor_EmptyBank() {
        Map<PebbleColor, Integer> emptyBank = new EnumMap<>(PebbleColor.class);
        
        TurnState turnState = new TurnState(
            emptyBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        assertNotNull(turnState);
        assertTrue(turnState.getBank().isEmpty());
    }

    @Test
    @DisplayName("Constructor accepts empty otherScores")
    public void testConstructor_EmptyOtherScores() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            List.of(), 
            standardVisibleCards
        );

        assertNotNull(turnState);
        assertTrue(turnState.getOtherScores().isEmpty());
    }

    @Test
    @DisplayName("Constructor accepts empty visibleCards")
    public void testConstructor_EmptyVisibleCards() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            List.of()
        );

        assertNotNull(turnState);
        assertTrue(turnState.getVisibleCards().isEmpty());
    }

    // Immutability Tests

    @Test
    @DisplayName("Bank map is immutable")
    public void testImmutability_Bank() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        Map<PebbleColor, Integer> bank = turnState.getBank();

        assertThrows(UnsupportedOperationException.class, () -> {
            bank.put(PebbleColor.YELLOW, 10);
        });
    }

    @Test
    @DisplayName("OtherScores list is immutable")
    public void testImmutability_OtherScores() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        List<Integer> scores = turnState.getOtherScores();

        assertThrows(UnsupportedOperationException.class, () -> {
            scores.add(10);
        });
    }

    @Test
    @DisplayName("VisibleCards list is immutable")
    public void testImmutability_VisibleCards() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        List<Cards> cards = turnState.getVisibleCards();

        assertThrows(UnsupportedOperationException.class, () -> {
            cards.add(new Cards(List.of(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED, 
                                       PebbleColor.RED, PebbleColor.RED), false));
        });
    }

    @Test
    @DisplayName("Modifications to original bank don't affect TurnState")
    public void testImmutability_OriginalBankModification() {
        Map<PebbleColor, Integer> mutableBank = new EnumMap<>(standardBank);
        
        TurnState turnState = new TurnState(
            mutableBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        // Modify original bank
        mutableBank.put(PebbleColor.YELLOW, 100);

        // TurnState should be unaffected
        assertEquals(1, turnState.getBank().get(PebbleColor.YELLOW));
    }

    @Test
    @DisplayName("Modifications to original otherScores don't affect TurnState")
    public void testImmutability_OriginalOtherScoresModification() {
        List<Integer> mutableScores = new ArrayList<>(standardOtherScores);
        
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            mutableScores, 
            standardVisibleCards
        );

        // Modify original list
        mutableScores.add(99);

        // TurnState should be unaffected
        assertEquals(2, turnState.getOtherScores().size());
        assertFalse(turnState.getOtherScores().contains(99));
    }

    @Test
    @DisplayName("Modifications to original visibleCards don't affect TurnState")
    public void testImmutability_OriginalVisibleCardsModification() {
        List<Cards> mutableCards = new ArrayList<>(standardVisibleCards);
        
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            mutableCards
        );

        // Modify original list
        Cards newCard = new Cards(List.of(PebbleColor.YELLOW, PebbleColor.YELLOW, 
                                         PebbleColor.YELLOW, PebbleColor.YELLOW, 
                                         PebbleColor.YELLOW), false);
        mutableCards.add(newCard);

        // TurnState should be unaffected
        assertEquals(2, turnState.getVisibleCards().size());
        assertFalse(turnState.getVisibleCards().contains(newCard));
    }

    // Accessor Tests

    @Test
    @DisplayName("getBank returns correct bank")
    public void testGetBank() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        Map<PebbleColor, Integer> bank = turnState.getBank();

        assertEquals(3, bank.get(PebbleColor.RED));
        assertEquals(5, bank.get(PebbleColor.WHITE));
        assertEquals(2, bank.get(PebbleColor.BLUE));
        assertEquals(4, bank.get(PebbleColor.GREEN));
        assertEquals(1, bank.get(PebbleColor.YELLOW));
    }

    @Test
    @DisplayName("getActivePlayer returns correct player")
    public void testGetActivePlayer() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        PlayerState player = turnState.getActivePlayer();

        assertEquals("TestPlayer", player.getName());
        assertEquals(7, player.getScore());
        assertEquals(2, player.getWallet().get(PebbleColor.RED));
        assertEquals(1, player.getWallet().get(PebbleColor.BLUE));
    }

    @Test
    @DisplayName("getOtherScores returns correct scores")
    public void testGetOtherScores() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        List<Integer> scores = turnState.getOtherScores();

        assertEquals(2, scores.size());
        assertEquals(4, scores.get(0));
        assertEquals(6, scores.get(1));
    }

    @Test
    @DisplayName("getVisibleCards returns correct cards")
    public void testGetVisibleCards() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        List<Cards> cards = turnState.getVisibleCards();

        assertEquals(2, cards.size());
        assertFalse(cards.get(0).hasStar());
        assertTrue(cards.get(1).hasStar());
    }

    // Render Tests

    @Test
    @DisplayName("render produces correct output for standard state")
    public void testRender_StandardState() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        String rendered = turnState.render();

        // Check header
        assertTrue(rendered.contains("=== Turn State ==="));
        
        // Check wallet
        assertTrue(rendered.contains("Your wallet:"));
        assertTrue(rendered.contains("RED=2"));
        assertTrue(rendered.contains("BLUE=1"));
        
        // Check score
        assertTrue(rendered.contains("Your score:  7"));
        
        // Check bank
        assertTrue(rendered.contains("Bank:"));
        assertTrue(rendered.contains("RED=3"));
        assertTrue(rendered.contains("WHITE=5"));
        assertTrue(rendered.contains("BLUE=2"));
        assertTrue(rendered.contains("GREEN=4"));
        assertTrue(rendered.contains("YELLOW=1"));
        
        // Check other scores
        assertTrue(rendered.contains("Other scores: [4, 6]"));
        
        // Check visible cards section
        assertTrue(rendered.contains("Visible Cards:"));
    }

    @Test
    @DisplayName("render handles empty bank")
    public void testRender_EmptyBank() {
        Map<PebbleColor, Integer> emptyBank = new EnumMap<>(PebbleColor.class);
        
        TurnState turnState = new TurnState(
            emptyBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        String rendered = turnState.render();

        assertTrue(rendered.contains("Bank:        (empty)"));
    }

    @Test
    @DisplayName("render handles empty wallet")
    public void testRender_EmptyWallet() {
        Map<PebbleColor, Integer> emptyWallet = new EnumMap<>(PebbleColor.class);
        PlayerState emptyWalletPlayer = new PlayerState("EmptyPlayer", emptyWallet, 0, true);
        
        TurnState turnState = new TurnState(
            standardBank, 
            emptyWalletPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        String rendered = turnState.render();

        assertTrue(rendered.contains("Your wallet: (empty)"));
    }

    @Test
    @DisplayName("render handles no visible cards")
    public void testRender_NoVisibleCards() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            List.of()
        );

        String rendered = turnState.render();

        assertTrue(rendered.contains("Visible Cards:"));
        assertTrue(rendered.contains("(none)"));
    }

    @Test
    @DisplayName("render handles no other scores")
    public void testRender_NoOtherScores() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            List.of(), 
            standardVisibleCards
        );

        String rendered = turnState.render();

        assertTrue(rendered.contains("Other scores: []"));
    }

    @Test
    @DisplayName("render handles single pebble color in bank")
    public void testRender_SingleColorBank() {
        Map<PebbleColor, Integer> singleColorBank = new EnumMap<>(PebbleColor.class);
        singleColorBank.put(PebbleColor.RED, 5);
        
        TurnState turnState = new TurnState(
            singleColorBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        String rendered = turnState.render();

        assertTrue(rendered.contains("Bank:        RED=5"));
        assertFalse(rendered.contains("WHITE"));
        assertFalse(rendered.contains("BLUE"));
    }

    @Test
    @DisplayName("render displays all five colors when present")
    public void testRender_AllColors() {
        Map<PebbleColor, Integer> allColorsBank = new EnumMap<>(PebbleColor.class);
        allColorsBank.put(PebbleColor.RED, 1);
        allColorsBank.put(PebbleColor.WHITE, 2);
        allColorsBank.put(PebbleColor.BLUE, 3);
        allColorsBank.put(PebbleColor.GREEN, 4);
        allColorsBank.put(PebbleColor.YELLOW, 5);
        
        TurnState turnState = new TurnState(
            allColorsBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        String rendered = turnState.render();

        assertTrue(rendered.contains("RED=1"));
        assertTrue(rendered.contains("WHITE=2"));
        assertTrue(rendered.contains("BLUE=3"));
        assertTrue(rendered.contains("GREEN=4"));
        assertTrue(rendered.contains("YELLOW=5"));
    }

    @Test
    @DisplayName("render displays star symbol for happy cards")
    public void testRender_HappyCards() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        String rendered = turnState.render();

        // The second card in standardVisibleCards has a star
        assertTrue(rendered.contains("★"));
    }

    @Test
    @DisplayName("render handles multiple other player scores")
    public void testRender_MultipleOtherScores() {
        List<Integer> manyScores = List.of(1, 2, 3, 4, 5);
        
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            manyScores, 
            standardVisibleCards
        );

        String rendered = turnState.render();

        assertTrue(rendered.contains("Other scores: [1, 2, 3, 4, 5]"));
    }

    // toString Tests

    @Test
    @DisplayName("toString delegates to render")
    public void testToString_DelegatesToRender() {
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        String toString = turnState.toString();
        String render = turnState.render();

        assertEquals(render, toString);
    }

    // Edge Case Tests

    @Test
    @DisplayName("Handles player with zero score")
    public void testEdgeCase_ZeroScore() {
        Map<PebbleColor, Integer> wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 1);
        PlayerState zeroScorePlayer = new PlayerState("NewPlayer", wallet, 0, true);
        
        TurnState turnState = new TurnState(
            standardBank, 
            zeroScorePlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        assertEquals(0, turnState.getActivePlayer().getScore());
        assertTrue(turnState.render().contains("Your score:  0"));
    }

    @Test
    @DisplayName("Handles high pebble counts")
    public void testEdgeCase_HighPebbleCounts() {
        Map<PebbleColor, Integer> largeBank = new EnumMap<>(PebbleColor.class);
        largeBank.put(PebbleColor.RED, 999);
        
        TurnState turnState = new TurnState(
            largeBank, 
            standardPlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        assertTrue(turnState.render().contains("RED=999"));
    }

    @Test
    @DisplayName("Handles many visible cards")
    public void testEdgeCase_ManyVisibleCards() {
        List<Cards> manyCards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            manyCards.add(new Cards(
                List.of(PebbleColor.RED, PebbleColor.WHITE, PebbleColor.BLUE, 
                       PebbleColor.GREEN, PebbleColor.YELLOW), 
                i % 2 == 0
            ));
        }
        
        TurnState turnState = new TurnState(
            standardBank, 
            standardPlayer, 
            standardOtherScores, 
            manyCards
        );

        assertEquals(10, turnState.getVisibleCards().size());
        String rendered = turnState.render();
        assertTrue(rendered.contains("Visible Cards:"));
    }

    @Test
    @DisplayName("Handles inactive player state")
    public void testEdgeCase_InactivePlayer() {
        Map<PebbleColor, Integer> wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 1);
        PlayerState inactivePlayer = new PlayerState("InactivePlayer", wallet, 5, false);
        
        TurnState turnState = new TurnState(
            standardBank, 
            inactivePlayer, 
            standardOtherScores, 
            standardVisibleCards
        );

        assertFalse(turnState.getActivePlayer().isActive());
    }
}