package player;
 
import common.Cards;
import common.Cards.PebbleColor;
import common.Equation;
import org.junit.Before;
import org.junit.Test;
 
import java.util.*;
 
import static org.junit.Assert.*;
 
public class SearchNodeTest {
 
    private Map<PebbleColor, Integer> wallet;
    private Map<PebbleColor, Integer> bank;
    private List<Cards> visibleCards;
    private Equation redForBlue; // RED -> BLUE
 
    @Before
    public void setUp() {
        wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 3);
        wallet.put(PebbleColor.BLUE, 1);
 
        bank = new EnumMap<>(PebbleColor.class);
        bank.put(PebbleColor.BLUE, 5);
        bank.put(PebbleColor.RED, 5);
 
        // A card that costs RED, RED
        Cards twoRedCard = new Cards(Arrays.asList(PebbleColor.RED, PebbleColor.RED), false);
        visibleCards = new ArrayList<>(Arrays.asList(twoRedCard));
 
        // Equation: give 1 RED, receive 1 BLUE
        redForBlue = new Equation(
                Arrays.asList(PebbleColor.RED),
                Arrays.asList(PebbleColor.BLUE)
        );
    }
 
    // --- createInitial ---
 
    @Test
    public void testCreateInitialDepthIsZero() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertEquals(0, node.getDepth());
    }
 
    @Test
    public void testCreateInitialExchangeSequenceIsEmpty() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertTrue(node.getExchangeSequence().isEmpty());
    }
 
    @Test
    public void testCreateInitialRemainingCardsMatchesInput() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertEquals(visibleCards.size(), node.getRemainingCards().size());
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullWallet() {
        new SearchNode(null, new ArrayList<>(), new HashSet<>(), 0);
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullExchangeSequence() {
        new SearchNode(wallet, null, new HashSet<>(), 0);
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNullRemainingCards() {
        new SearchNode(wallet, new ArrayList<>(), null, 0);
    }
 
    // --- canPurchase ---
 
    @Test
    public void testCanPurchaseReturnsTrueWhenAffordable() {
        Cards card = new Cards(Arrays.asList(PebbleColor.RED, PebbleColor.RED), false);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertTrue(node.canPurchase(card));
    }
 
    @Test
    public void testCanPurchaseReturnsFalseWhenNotAffordable() {
        Cards expensive = new Cards(Arrays.asList(PebbleColor.RED, PebbleColor.RED,
                PebbleColor.RED, PebbleColor.RED), false);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertFalse(node.canPurchase(expensive));
    }
 
    @Test
    public void testCanPurchaseReturnsFalseWhenColorMissing() {
        Cards greenCard = new Cards(Arrays.asList(PebbleColor.GREEN), false);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertFalse(node.canPurchase(greenCard));
    }
 
    // --- afterPurchase ---
 
    @Test
    public void testAfterPurchaseDeductsWallet() {
        Cards card = new Cards(Arrays.asList(PebbleColor.RED, PebbleColor.RED), false);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        SearchNode after = node.afterPurchase(card);
        assertEquals(1, (int) after.getWallet().getOrDefault(PebbleColor.RED, 0));
    }
 
    @Test
    public void testAfterPurchaseRemovesCardFromRemaining() {
        Cards card = visibleCards.get(0);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        SearchNode after = node.afterPurchase(card);
        assertFalse(after.getRemainingCards().contains(card));
    }
 
    @Test
    public void testAfterPurchaseDoesNotChangeDepth() {
        Cards card = visibleCards.get(0);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        SearchNode after = node.afterPurchase(card);
        assertEquals(0, after.getDepth());
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testAfterPurchaseThrowsWhenNotAffordable() {
        Cards expensive = new Cards(Arrays.asList(PebbleColor.GREEN, PebbleColor.GREEN), false);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        node.afterPurchase(expensive);
    }
 
    // --- canApplyExchange ---
 
    @Test
    public void testCanApplyExchangeReturnsTrueWhenValid() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertTrue(node.canApplyExchange(redForBlue, true, bank));
    }
 
    @Test
    public void testCanApplyExchangeReturnsFalseWhenPlayerLacksPebbles() {
        Map<PebbleColor, Integer> emptyWallet = new EnumMap<>(PebbleColor.class);
        SearchNode node = new SearchNode(emptyWallet, new ArrayList<>(), new HashSet<>(), 0);
        assertFalse(node.canApplyExchange(redForBlue, true, bank));
    }
 
    @Test
    public void testCanApplyExchangeReturnsFalseWhenBankLacksPebbles() {
        Map<PebbleColor, Integer> emptyBank = new EnumMap<>(PebbleColor.class);
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        assertFalse(node.canApplyExchange(redForBlue, true, emptyBank));
    }
 
    // --- afterExchange ---
 
    @Test
    public void testAfterExchangeUpdatesWalletCorrectly() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        SearchNode after = node.afterExchange(redForBlue, true, bank);
        assertEquals(2, (int) after.getWallet().getOrDefault(PebbleColor.RED, 0));
        assertEquals(2, (int) after.getWallet().getOrDefault(PebbleColor.BLUE, 0));
    }
 
    @Test
    public void testAfterExchangeIncrementsDepth() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        SearchNode after = node.afterExchange(redForBlue, true, bank);
        assertEquals(1, after.getDepth());
    }
 
    @Test
    public void testAfterExchangeAppendsToExchangeSequence() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        SearchNode after = node.afterExchange(redForBlue, true, bank);
        assertEquals(1, after.getExchangeSequence().size());
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testAfterExchangeThrowsWhenPlayerLacksPebbles() {
        Map<PebbleColor, Integer> emptyWallet = new EnumMap<>(PebbleColor.class);
        SearchNode node = new SearchNode(emptyWallet, new ArrayList<>(), new HashSet<>(), 0);
        node.afterExchange(redForBlue, true, bank);
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testAfterExchangeThrowsWhenBankLacksPebbles() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        node.afterExchange(redForBlue, true, new EnumMap<>(PebbleColor.class));
    }
 
    // --- immutability of returned views ---
 
    @Test(expected = UnsupportedOperationException.class)
    public void testGetWalletIsUnmodifiable() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        node.getWallet().put(PebbleColor.RED, 99);
    }
 
    @Test(expected = UnsupportedOperationException.class)
    public void testGetExchangeSequenceIsUnmodifiable() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        node.getExchangeSequence().add(new HashMap<>());
    }
 
    @Test(expected = UnsupportedOperationException.class)
    public void testGetRemainingCardsIsUnmodifiable() {
        SearchNode node = SearchNode.createInitial(wallet, visibleCards);
        node.getRemainingCards().clear();
    }
 
    // --- equals / hashCode ---
 
    @Test
    public void testEqualNodesAreEqual() {
        SearchNode a = SearchNode.createInitial(wallet, visibleCards);
        SearchNode b = SearchNode.createInitial(wallet, visibleCards);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
 
    @Test
    public void testDifferentDepthNodesAreNotEqual() {
        SearchNode a = SearchNode.createInitial(wallet, visibleCards);
        SearchNode b = a.afterExchange(redForBlue, true, bank);
        assertNotEquals(a, b);
    }
}