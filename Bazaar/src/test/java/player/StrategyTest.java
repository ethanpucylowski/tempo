package player;
 
import common.Cards;
import common.Cards.PebbleColor;
import common.Equation;
import common.Equations;
import common.TurnState;
import common.Player;
import org.junit.Before;
import org.junit.Test;
 
import java.util.*;
 
import static org.junit.Assert.*;
 
public class StrategyTest {
 
    private Map<PebbleColor, Integer> wallet;
    private Map<PebbleColor, Integer> bank;
    private List<Cards> visibleCards;
    private Equations equations;
    private TurnState turnState;
 
    @Before
    public void setUp() {
        wallet = new EnumMap<>(PebbleColor.class);
        wallet.put(PebbleColor.RED, 3);
        wallet.put(PebbleColor.BLUE, 2);
 
        bank = new EnumMap<>(PebbleColor.class);
        bank.put(PebbleColor.RED, 10);
        bank.put(PebbleColor.BLUE, 10);
        bank.put(PebbleColor.GREEN, 10);
 
        // One affordable card (costs RED, RED) and one unaffordable (costs GREEN x3)
        Cards affordable = new Cards(Arrays.asList(PebbleColor.RED, PebbleColor.RED), false);
        Cards unaffordable = new Cards(
                Arrays.asList(PebbleColor.GREEN, PebbleColor.GREEN, PebbleColor.GREEN), false);
        visibleCards = Arrays.asList(affordable, unaffordable);
 
        // Equation: give 1 RED, receive 1 GREEN
        Equation redForGreen = new Equation(
                Arrays.asList(PebbleColor.RED),
                Arrays.asList(PebbleColor.GREEN));
        equations = new Equations(Arrays.asList(redForGreen));
 
        Player player = new Player("testPlayer", wallet);
        turnState = new TurnState(player, bank, visibleCards);
    }
 
    // --- constructor / getters ---
 
    @Test
    public void testDefaultMaxDepthIsTen() {
        Strategy s = new Strategy(true);
        assertEquals(10, s.getMaxDepth());
    }
 
    @Test
    public void testCustomMaxDepth() {
        Strategy s = new Strategy(false, 5);
        assertEquals(5, s.getMaxDepth());
    }
 
    @Test
    public void testIsMaximizePoints() {
        assertTrue(new Strategy(true).isMaximizePoints());
        assertFalse(new Strategy(false).isMaximizePoints());
    }
 
    // --- chooseTurn: null guards ---
 
    @Test(expected = IllegalArgumentException.class)
    public void testChooseTurnThrowsOnNullTurnState() {
        new Strategy(true).chooseTurn(null, equations);
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void testChooseTurnThrowsOnNullEquations() {
        new Strategy(true).chooseTurn(turnState, null);
    }
 
    // --- chooseTurn: result structure ---
 
    @Test
    public void testChooseTurnReturnsRequiredKeys() {
        Map<String, Object> result = new Strategy(true).chooseTurn(turnState, equations);
        assertTrue(result.containsKey("exchanges"));
        assertTrue(result.containsKey("cards"));
        assertTrue(result.containsKey("wallet"));
        assertTrue(result.containsKey("points"));
    }
 
    // --- chooseTurn: no affordable cards, no useful equations ---
 
    @Test
    public void testChooseTurnWithNoAffordableCardsAndNoEquations() {
        Map<PebbleColor, Integer> emptyWallet = new EnumMap<>(PebbleColor.class);
        Player broke = new Player("broke", emptyWallet);
        TurnState state = new TurnState(broke, bank, visibleCards);
        Equations noEquations = new Equations(new ArrayList<>());
 
        Map<String, Object> result = new Strategy(true).chooseTurn(state, noEquations);
 
        // Should return empty turn
        assertTrue(((List<?>) result.get("exchanges")).isEmpty());
        assertTrue(((List<?>) result.get("cards")).isEmpty());
        assertEquals(0, result.get("points"));
    }
 
    // --- chooseTurn: can purchase without any exchange ---
 
    @Test
    public void testChooseTurnPurchasesAffordableCard() {
        Map<String, Object> result = new Strategy(true).chooseTurn(turnState, equations);
        List<Cards> purchased = (List<Cards>) result.get("cards");
        // At minimum the affordable RED,RED card should be bought
        assertFalse(purchased.isEmpty());
    }
 
    // --- chooseTurn: exchange enables additional purchase ---
 
    @Test
    public void testChooseTurnUsesExchangeToEnableMorePurchases() {
        // Wallet: RED=1, so can't buy RED,RED card directly.
        // But equation RED->GREEN lets player accumulate GREEN to buy GREEN,GREEN,GREEN card.
        Map<PebbleColor, Integer> limitedWallet = new EnumMap<>(PebbleColor.class);
        limitedWallet.put(PebbleColor.RED, 4); // 3 exchanges -> 1 RED + 3 GREEN
 
        Cards greenCard = new Cards(
                Arrays.asList(PebbleColor.GREEN, PebbleColor.GREEN, PebbleColor.GREEN), false);
        Cards redCard = new Cards(Arrays.asList(PebbleColor.RED), false);
 
        Equation redForGreen = new Equation(
                Arrays.asList(PebbleColor.RED), Arrays.asList(PebbleColor.GREEN));
        Equations eq = new Equations(Arrays.asList(redForGreen));
 
        Player player = new Player("p", limitedWallet);
        TurnState state = new TurnState(player, bank, Arrays.asList(greenCard, redCard));
 
        Map<String, Object> result = new Strategy(false).chooseTurn(state, eq);
        List<Cards> purchased = (List<Cards>) result.get("cards");
 
        // Should be able to buy both cards after trading
        assertEquals(2, purchased.size());
    }
 
    // --- maximizePoints vs maximizeCards produces different results ---
 
    @Test
    public void testMaximizePointsVsMaximizeCardsCanDiffer() {
        // Star card gives more points but costs more pebbles (might mean fewer cards)
        Cards starCard = new Cards(Arrays.asList(PebbleColor.RED, PebbleColor.RED,
                PebbleColor.BLUE, PebbleColor.BLUE), true);
        Cards cheapCard = new Cards(Arrays.asList(PebbleColor.RED), false);
 
        Player player = new Player("p", wallet);
        TurnState state = new TurnState(player, bank, Arrays.asList(starCard, cheapCard));
        Equations noEq = new Equations(new ArrayList<>());
 
        Map<String, Object> byPoints = new Strategy(true).chooseTurn(state, noEq);
        Map<String, Object> byCards = new Strategy(false).chooseTurn(state, noEq);
 
        // Both should be valid turns; their card lists may differ
        assertNotNull(byPoints.get("cards"));
        assertNotNull(byCards.get("cards"));
    }
}
 