package referee;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import common.Cards.PebbleColor;

@DisplayName("PlayerState Unit Tests")
public class PlayerStateTest {

    private Map<PebbleColor, Integer> standardWallet;
    private String standardName;
    private int standardScore;
    private boolean standardActive;

    @BeforeEach
    public void setUp() {
        standardName = "TestPlayer";
        
        standardWallet = new EnumMap<>(PebbleColor.class);
        standardWallet.put(PebbleColor.RED, 2);
        standardWallet.put(PebbleColor.BLUE, 1);
        standardWallet.put(PebbleColor.GREEN, 3);
        
        standardScore = 7;
        standardActive = true;
    }

    // Constructor Tests

    @Test
    @DisplayName("Constructor succeeds with valid inputs")
    public void testConstructor_ValidInputs() {
        PlayerState player = new PlayerState(standardName, standardWallet, standardScore, standardActive);

        assertNotNull(player);
        assertEquals(standardName, player.getName());
        assertEquals(standardScore, player.getScore());
        assertTrue(player.isActive());
        
        // Verify wallet contents
        Map<PebbleColor, Integer> wallet = player.getWallet();
        assertEquals(2, wallet.get(PebbleColor.RED));
        assertEquals(1, wallet.get(PebbleColor.BLUE));
        assertEquals(3, wallet.get(PebbleColor.GREEN));
    }

    @Test
    @DisplayName("Constructor accepts empty wallet")
    public void testConstructor_EmptyWallet() {
        Map<PebbleColor, Integer> emptyWallet = new EnumMap<>(PebbleColor.class);
        
        PlayerState player = new PlayerState("BrokePlayer", emptyWallet, 0, true);

        assertNotNull(player);
        assertTrue(player.getWallet().isEmpty());
        assertEquals(0, player.getScore());
    }

    @Test
    @DisplayName("Constructor accepts inactive player")
    public void testConstructor_InactivePlayer() {
        Map<PebbleColor, Integer> wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 5);
        
        PlayerState player = new PlayerState("EliminatedPlayer", wallet, 15, false);

        assertNotNull(player);
        assertFalse(player.isActive());
        assertEquals(15, player.getScore());
    }

    @Test
    @DisplayName("Constructor throws when name is null")
    public void testConstructor_NullName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PlayerState(null, standardWallet, standardScore, standardActive)
        );
        assertEquals("Player name must not be blank.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor throws when name is blank")
    public void testConstructor_BlankName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PlayerState("   ", standardWallet, standardScore, standardActive)
        );
        assertEquals("Player name must not be blank.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor throws when name is empty string")
    public void testConstructor_EmptyName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PlayerState("", standardWallet, standardScore, standardActive)
        );
        assertEquals("Player name must not be blank.", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor throws when wallet is null")
    public void testConstructor_NullWallet() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PlayerState(standardName, null, standardScore, standardActive)
        );
        assertEquals("Wallet must not be null.", exception.getMessage());
    }

    // Accessor Tests

    @Test
    @DisplayName("getName returns correct name")
    public void testGetName() {
        PlayerState player = new PlayerState("AlicePlayer", standardWallet, standardScore, standardActive);

        assertEquals("AlicePlayer", player.getName());
    }

    @Test
    @DisplayName("getWallet returns correct wallet contents")
    public void testGetWallet() {
        Map<PebbleColor, Integer> wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 3);
        wallet.put(PebbleColor.WHITE, 2);
        wallet.put(PebbleColor.BLUE, 1);
        
        PlayerState player = new PlayerState(standardName, wallet, 5, true);

        Map<PebbleColor, Integer> retrievedWallet = player.getWallet();
        
        assertEquals(3, retrievedWallet.get(PebbleColor.RED));
        assertEquals(2, retrievedWallet.get(PebbleColor.WHITE));
        assertEquals(1, retrievedWallet.get(PebbleColor.BLUE));
        assertEquals(3, retrievedWallet.size());
    }

    @Test
    @DisplayName("getScore returns correct score")
    public void testGetScore() {
        PlayerState player = new PlayerState(standardName, standardWallet, 25, true);

        assertEquals(25, player.getScore());
    }

    @Test
    @DisplayName("isActive returns true when player is active")
    public void testIsActive_True() {
        PlayerState player = new PlayerState(standardName, standardWallet, 5, true);

        assertTrue(player.isActive());
    }

    @Test
    @DisplayName("isActive returns false when player is inactive")
    public void testIsActive_False() {
        PlayerState player = new PlayerState(standardName, standardWallet, 5, false);

        assertFalse(player.isActive());
    }

    // Mutator Tests

    @Test
    @DisplayName("setScore updates score correctly")
    public void testSetScore() {
        PlayerState player = new PlayerState(standardName, standardWallet, 5, true);

        assertEquals(5, player.getScore());

        player.setScore(15);

        assertEquals(15, player.getScore());
    }

    @Test
    @DisplayName("setScore can set score to zero")
    public void testSetScore_ToZero() {
        PlayerState player = new PlayerState(standardName, standardWallet, 20, true);

        player.setScore(0);

        assertEquals(0, player.getScore());
    }

    @Test
    @DisplayName("setScore can be called multiple times")
    public void testSetScore_MultipleTimes() {
        PlayerState player = new PlayerState(standardName, standardWallet, 0, true);

        player.setScore(5);
        assertEquals(5, player.getScore());

        player.setScore(10);
        assertEquals(10, player.getScore());

        player.setScore(15);
        assertEquals(15, player.getScore());
    }

    @Test
    @DisplayName("setActive can deactivate player")
    public void testSetActive_ToFalse() {
        PlayerState player = new PlayerState(standardName, standardWallet, 10, true);

        assertTrue(player.isActive());

        player.setActive(false);

        assertFalse(player.isActive());
    }

    @Test
    @DisplayName("setActive can reactivate player")
    public void testSetActive_ToTrue() {
        PlayerState player = new PlayerState(standardName, standardWallet, 10, false);

        assertFalse(player.isActive());

        player.setActive(true);

        assertTrue(player.isActive());
    }

    @Test
    @DisplayName("setActive can toggle multiple times")
    public void testSetActive_MultipleTimes() {
        PlayerState player = new PlayerState(standardName, standardWallet, 10, true);

        player.setActive(false);
        assertFalse(player.isActive());

        player.setActive(true);
        assertTrue(player.isActive());

        player.setActive(false);
        assertFalse(player.isActive());
    }

    // Immutability Tests

    @Test
    @DisplayName("Wallet returned by getWallet is immutable")
    public void testImmutability_WalletModification() {
        PlayerState player = new PlayerState(standardName, standardWallet, standardScore, standardActive);

        Map<PebbleColor, Integer> wallet = player.getWallet();

        assertThrows(UnsupportedOperationException.class, () -> {
            wallet.put(PebbleColor.YELLOW, 10);
        });
    }

    @Test
    @DisplayName("Modifications to original wallet don't affect PlayerState")
    public void testImmutability_OriginalWalletModification() {
        Map<PebbleColor, Integer> mutableWallet = new EnumMap<>(standardWallet);
        
        PlayerState player = new PlayerState(standardName, mutableWallet, standardScore, standardActive);

        // Modify original wallet
        mutableWallet.put(PebbleColor.YELLOW, 100);
        mutableWallet.put(PebbleColor.RED, 999);

        // PlayerState should be unaffected
        Map<PebbleColor, Integer> playerWallet = player.getWallet();
        assertEquals(2, playerWallet.get(PebbleColor.RED));
        assertNull(playerWallet.get(PebbleColor.YELLOW));
    }

    @Test
    @DisplayName("Clearing original wallet doesn't affect PlayerState")
    public void testImmutability_ClearOriginalWallet() {
        Map<PebbleColor, Integer> mutableWallet = new EnumMap<>(standardWallet);
        
        PlayerState player = new PlayerState(standardName, mutableWallet, standardScore, standardActive);

        mutableWallet.clear();

        // PlayerState wallet should still have original contents
        Map<PebbleColor, Integer> playerWallet = player.getWallet();
        assertEquals(3, playerWallet.size());
        assertEquals(2, playerWallet.get(PebbleColor.RED));
    }

    // toString Tests

    @Test
    @DisplayName("toString includes name")
    public void testToString_ContainsName() {
        PlayerState player = new PlayerState("TestPlayer", standardWallet, 7, true);

        String toString = player.toString();

        assertTrue(toString.contains("TestPlayer"));
    }

    @Test
    @DisplayName("toString includes score")
    public void testToString_ContainsScore() {
        PlayerState player = new PlayerState(standardName, standardWallet, 7, true);

        String toString = player.toString();

        assertTrue(toString.contains("score=7"));
    }

    @Test
    @DisplayName("toString includes active status when true")
    public void testToString_ContainsActiveTrue() {
        PlayerState player = new PlayerState(standardName, standardWallet, 7, true);

        String toString = player.toString();

        assertTrue(toString.contains("active=true"));
    }

    @Test
    @DisplayName("toString includes active status when false")
    public void testToString_ContainsActiveFalse() {
        PlayerState player = new PlayerState(standardName, standardWallet, 7, false);

        String toString = player.toString();

        assertTrue(toString.contains("active=false"));
    }

    @Test
    @DisplayName("toString includes wallet")
    public void testToString_ContainsWallet() {
        PlayerState player = new PlayerState(standardName, standardWallet, 7, true);

        String toString = player.toString();

        assertTrue(toString.contains("wallet="));
    }

    @Test
    @DisplayName("toString with empty wallet shows empty map")
    public void testToString_EmptyWallet() {
        Map<PebbleColor, Integer> emptyWallet = new EnumMap<>(PebbleColor.class);
        PlayerState player = new PlayerState("BrokePlayer", emptyWallet, 0, true);

        String toString = player.toString();

        assertTrue(toString.contains("BrokePlayer"));
        assertTrue(toString.contains("score=0"));
        assertTrue(toString.contains("active=true"));
        assertTrue(toString.contains("wallet={}"));
    }

    @Test
    @DisplayName("toString format follows expected pattern")
    public void testToString_Format() {
        PlayerState player = new PlayerState("Player1", standardWallet, 10, true);

        String toString = player.toString();

        // Should follow pattern: name | score=X | active=Y | wallet=Z
        assertTrue(toString.contains(" | "));
        assertTrue(toString.contains("Player1"));
        assertTrue(toString.contains("score="));
        assertTrue(toString.contains("active="));
        assertTrue(toString.contains("wallet="));
    }

    // Edge Case Tests

    @Test
    @DisplayName("Handles zero score")
    public void testEdgeCase_ZeroScore() {
        PlayerState player = new PlayerState(standardName, standardWallet, 0, true);

        assertEquals(0, player.getScore());
        assertTrue(player.toString().contains("score=0"));
    }

    @Test
    @DisplayName("Handles negative score")
    public void testEdgeCase_NegativeScore() {
        // PlayerState doesn't validate score >= 0, so negative scores should work
        PlayerState player = new PlayerState(standardName, standardWallet, -5, true);

        assertEquals(-5, player.getScore());
    }

    @Test
    @DisplayName("Handles high score values")
    public void testEdgeCase_HighScore() {
        PlayerState player = new PlayerState(standardName, standardWallet, 999, true);

        assertEquals(999, player.getScore());
    }

    @Test
    @DisplayName("Handles single pebble color in wallet")
    public void testEdgeCase_SingleColor() {
        Map<PebbleColor, Integer> wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 5);
        
        PlayerState player = new PlayerState(standardName, wallet, 5, true);

        Map<PebbleColor, Integer> retrievedWallet = player.getWallet();
        assertEquals(1, retrievedWallet.size());
        assertEquals(5, retrievedWallet.get(PebbleColor.RED));
    }

    @Test
    @DisplayName("Handles all five colors in wallet")
    public void testEdgeCase_AllColors() {
        Map<PebbleColor, Integer> wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 1);
        wallet.put(PebbleColor.WHITE, 2);
        wallet.put(PebbleColor.BLUE, 3);
        wallet.put(PebbleColor.GREEN, 4);
        wallet.put(PebbleColor.YELLOW, 5);
        
        PlayerState player = new PlayerState(standardName, wallet, 20, true);

        Map<PebbleColor, Integer> retrievedWallet = player.getWallet();
        assertEquals(5, retrievedWallet.size());
        assertEquals(1, retrievedWallet.get(PebbleColor.RED));
        assertEquals(2, retrievedWallet.get(PebbleColor.WHITE));
        assertEquals(3, retrievedWallet.get(PebbleColor.BLUE));
        assertEquals(4, retrievedWallet.get(PebbleColor.GREEN));
        assertEquals(5, retrievedWallet.get(PebbleColor.YELLOW));
    }

    @Test
    @DisplayName("Handles large pebble counts")
    public void testEdgeCase_LargePebbleCounts() {
        Map<PebbleColor, Integer> wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 100);
        wallet.put(PebbleColor.BLUE, 50);
        
        PlayerState player = new PlayerState(standardName, wallet, 75, true);

        Map<PebbleColor, Integer> retrievedWallet = player.getWallet();
        assertEquals(100, retrievedWallet.get(PebbleColor.RED));
        assertEquals(50, retrievedWallet.get(PebbleColor.BLUE));
    }

    // Integration Tests (combining multiple operations)

    @Test
    @DisplayName("Can update both score and active status")
    public void testIntegration_UpdateScoreAndActive() {
        PlayerState player = new PlayerState(standardName, standardWallet, 5, true);

        assertEquals(5, player.getScore());
        assertTrue(player.isActive());

        player.setScore(10);
        player.setActive(false);

        assertEquals(10, player.getScore());
        assertFalse(player.isActive());
    }

    @Test
    @DisplayName("Mutators don't affect wallet")
    public void testIntegration_MutatorsDoNotAffectWallet() {
        PlayerState player = new PlayerState(standardName, standardWallet, 5, true);

        Map<PebbleColor, Integer> walletBefore = new EnumMap<>(player.getWallet());

        player.setScore(20);
        player.setActive(false);

        Map<PebbleColor, Integer> walletAfter = player.getWallet();

        assertEquals(walletBefore, walletAfter);
    }

    @Test
    @DisplayName("Mutators don't affect name")
    public void testIntegration_MutatorsDoNotAffectName() {
        PlayerState player = new PlayerState("OriginalName", standardWallet, 5, true);

        player.setScore(20);
        player.setActive(false);

        assertEquals("OriginalName", player.getName());
    }
}