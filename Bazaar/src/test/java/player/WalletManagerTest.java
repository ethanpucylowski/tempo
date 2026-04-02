package player;
 
import common.Cards.PebbleColor;
import org.junit.Before;
import org.junit.Test;
 
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
 
import static org.junit.Assert.*;
 
public class WalletManagerTest {
 
    private Map<PebbleColor, Integer> wallet;
 
    @Before
    public void setUp() {
        wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 3);
        wallet.put(PebbleColor.BLUE, 2);
    }
 
    // --- addPebbles ---
 
    @Test
    public void testAddPebblesIncreasesCount() {
        WalletManager.addPebbles(wallet, Arrays.asList(PebbleColor.RED, PebbleColor.GREEN));
        assertEquals(4, (int) wallet.get(PebbleColor.RED));
        assertEquals(1, (int) wallet.get(PebbleColor.GREEN));
    }
 
    @Test
    public void testAddPebblesNewColor() {
        WalletManager.addPebbles(wallet, Arrays.asList(PebbleColor.YELLOW));
        assertEquals(1, (int) wallet.get(PebbleColor.YELLOW));
    }
 
    @Test
    public void testAddPebblesEmptyList() {
        WalletManager.addPebbles(wallet, Arrays.asList());
        assertEquals(3, (int) wallet.get(PebbleColor.RED));
        assertEquals(2, (int) wallet.get(PebbleColor.BLUE));
    }
 
    // --- removePebbles ---
 
    @Test
    public void testRemovePebblesDecreasesCount() {
        WalletManager.removePebbles(wallet, Arrays.asList(PebbleColor.RED));
        assertEquals(2, (int) wallet.get(PebbleColor.RED));
    }
 
    @Test
    public void testRemovePebblesRemovesKeyWhenZero() {
        WalletManager.removePebbles(wallet, Arrays.asList(PebbleColor.BLUE, PebbleColor.BLUE));
        assertFalse(wallet.containsKey(PebbleColor.BLUE));
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testRemovePebblesThrowsWhenInsufficient() {
        WalletManager.removePebbles(wallet, Arrays.asList(PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED));
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testRemovePebblesThrowsWhenColorAbsent() {
        WalletManager.removePebbles(wallet, Arrays.asList(PebbleColor.GREEN));
    }
 
    // --- hasPebbles ---
 
    @Test
    public void testHasPebblesReturnsTrueWhenSufficient() {
        assertTrue(WalletManager.hasPebbles(wallet, Arrays.asList(PebbleColor.RED, PebbleColor.BLUE)));
    }
 
    @Test
    public void testHasPebblesReturnsFalseWhenInsufficient() {
        assertFalse(WalletManager.hasPebbles(wallet,
                Arrays.asList(PebbleColor.RED, PebbleColor.RED, PebbleColor.RED, PebbleColor.RED)));
    }
 
    @Test
    public void testHasPebblesReturnsFalseWhenColorAbsent() {
        assertFalse(WalletManager.hasPebbles(wallet, Arrays.asList(PebbleColor.GREEN)));
    }
 
    @Test
    public void testHasPebblesEmptyRequired() {
        assertTrue(WalletManager.hasPebbles(wallet, Arrays.asList()));
    }
 
    // --- getTotalPebbles ---
 
    @Test
    public void testGetTotalPebbles() {
        assertEquals(5, WalletManager.getTotalPebbles(wallet));
    }
 
    @Test
    public void testGetTotalPebblesEmpty() {
        assertEquals(0, WalletManager.getTotalPebbles(new EnumMap<>(PebbleColor.class)));
    }
 
    // --- copyWallet ---
 
    @Test
    public void testCopyWalletIsIndependent() {
        Map<PebbleColor, Integer> copy = WalletManager.copyWallet(wallet);
        copy.put(PebbleColor.RED, 99);
        assertEquals(3, (int) wallet.get(PebbleColor.RED));
    }
 
    @Test
    public void testCopyWalletHasSameContents() {
        Map<PebbleColor, Integer> copy = WalletManager.copyWallet(wallet);
        assertEquals(wallet, copy);
    }
 
    // --- merge ---
 
    @Test
    public void testMergeCombinesCounts() {
        Map<PebbleColor, Integer> other = new EnumMap<>(PebbleColor.class);
        other.put(PebbleColor.RED, 1);
        other.put(PebbleColor.GREEN, 4);
 
        Map<PebbleColor, Integer> merged = WalletManager.merge(wallet, other);
        assertEquals(4, (int) merged.get(PebbleColor.RED));
        assertEquals(2, (int) merged.get(PebbleColor.BLUE));
        assertEquals(4, (int) merged.get(PebbleColor.GREEN));
    }
 
    @Test
    public void testMergeDoesNotMutateInputs() {
        Map<PebbleColor, Integer> other = new EnumMap<>(PebbleColor.class);
        other.put(PebbleColor.RED, 1);
        WalletManager.merge(wallet, other);
        assertEquals(3, (int) wallet.get(PebbleColor.RED));
    }
 
    // --- compareLexicographically ---
 
    @Test
    public void testCompareEqualWallets() {
        Map<PebbleColor, Integer> copy = WalletManager.copyWallet(wallet);
        assertEquals(0, WalletManager.compareLexicographically(wallet, copy));
    }
 
    @Test
    public void testCompareWalletLess() {
        Map<PebbleColor, Integer> bigger = WalletManager.copyWallet(wallet);
        bigger.put(PebbleColor.RED, 5);
        assertTrue(WalletManager.compareLexicographically(wallet, bigger) < 0);
    }
 
    @Test
    public void testCompareWalletGreater() {
        Map<PebbleColor, Integer> smaller = new EnumMap<>(PebbleColor.class);
        smaller.put(PebbleColor.RED, 1);
        assertTrue(WalletManager.compareLexicographically(wallet, smaller) > 0);
    }
}