package player;
 
import common.Cards;
import common.Cards.PebbleColor;
import common.Equation;
import org.junit.Test;
 
import java.util.*;
 
import static org.junit.Assert.*;
 
public class CandidateSelectorTest {
 
    // --- helpers ---
 
    private Map<String, Object> makeTurn(int points, int cardCount, int pebbleCount,
                                          Map<PebbleColor, Integer> wallet,
                                          List<Map<String, Object>> exchanges,
                                          List<Cards> cards) {
        Map<String, Object> turn = new HashMap<>();
        turn.put("points", points);
        turn.put("wallet", wallet);
        turn.put("exchanges", exchanges);
        turn.put("cards", cards);
        return turn;
    }
 
    private Map<PebbleColor, Integer> wallet(int red, int blue) {
        Map<PebbleColor, Integer> w = new EnumMap<>(PebbleColor.class);
        if (red > 0) w.put(PebbleColor.RED, red);
        if (blue > 0) w.put(PebbleColor.BLUE, blue);
        return w;
    }
 
    private Cards card(boolean star, PebbleColor... colors) {
        return new Cards(Arrays.asList(colors), star);
    }
 
    private Map<String, Object> exchange(Equation eq, boolean leftToRight) {
        Map<String, Object> e = new HashMap<>();
        e.put("equation", eq);
        e.put("leftToRight", leftToRight);
        return e;
    }
 
    // --- selectBest: empty input ---
 
    @Test(expected = IllegalArgumentException.class)
    public void testSelectBestThrowsOnEmptyCandidates() {
        new CandidateSelector(true).selectBest(new HashSet<>());
    }
 
    // --- single candidate always wins ---
 
    @Test
    public void testSingleCandidateIsAlwaysSelected() {
        Map<String, Object> only = makeTurn(5, 1, 2, wallet(2, 0),
                new ArrayList<>(), Arrays.asList(card(false, PebbleColor.RED)));
 
        Map<String, Object> result = new CandidateSelector(true).selectBest(
                new HashSet<>(Arrays.asList(only)));
        assertSame(only, result);
    }
 
    // --- maximizePoints: primary filter ---
 
    @Test
    public void testSelectsHighestPoints() {
        Map<String, Object> low = makeTurn(1, 1, 3, wallet(3, 0),
                new ArrayList<>(), Arrays.asList(card(false, PebbleColor.RED)));
        Map<String, Object> high = makeTurn(5, 1, 3, wallet(3, 0),
                new ArrayList<>(), Arrays.asList(card(true, PebbleColor.RED)));
 
        Map<String, Object> result = new CandidateSelector(true).selectBest(
                new HashSet<>(Arrays.asList(low, high)));
        assertEquals(5, result.get("points"));
    }
 
    // --- maximizeCards: primary filter ---
 
    @Test
    public void testSelectsMostCards() {
        Map<String, Object> oneCard = makeTurn(2, 1, 4, wallet(2, 2),
                new ArrayList<>(), Arrays.asList(card(false, PebbleColor.RED)));
        Map<String, Object> twoCards = makeTurn(2, 2, 2, wallet(1, 1),
                new ArrayList<>(),
                Arrays.asList(card(false, PebbleColor.RED), card(false, PebbleColor.BLUE)));
 
        Map<String, Object> result = new CandidateSelector(false).selectBest(
                new HashSet<>(Arrays.asList(oneCard, twoCards)));
        assertEquals(2, ((List<?>) result.get("cards")).size());
    }
 
    // --- points as secondary tie-break when maximizing cards ---
 
    @Test
    public void testTieBreakByPointsWhenMaximizingCards() {
        List<Cards> sameCards = Arrays.asList(card(false, PebbleColor.RED));
 
        Map<String, Object> lowPoints = makeTurn(1, 1, 3, wallet(3, 0),
                new ArrayList<>(), sameCards);
        Map<String, Object> highPoints = makeTurn(4, 1, 3, wallet(3, 0),
                new ArrayList<>(), sameCards);
 
        Map<String, Object> result = new CandidateSelector(false).selectBest(
                new HashSet<>(Arrays.asList(lowPoints, highPoints)));
        assertEquals(4, result.get("points"));
    }
 
    // --- fewer remaining pebbles wins ---
 
    @Test
    public void testTieBreakByFewerRemainingPebbles() {
        List<Cards> sameCards = Arrays.asList(card(false, PebbleColor.RED));
 
        Map<String, Object> manyPebbles = makeTurn(3, 1, 0, wallet(5, 5),
                new ArrayList<>(), sameCards);
        Map<String, Object> fewPebbles = makeTurn(3, 1, 0, wallet(1, 0),
                new ArrayList<>(), sameCards);
 
        Map<String, Object> result = new CandidateSelector(true).selectBest(
                new HashSet<>(Arrays.asList(manyPebbles, fewPebbles)));
        assertEquals(wallet(1, 0), result.get("wallet"));
    }
 
    // --- fewest exchanges wins ---
 
    @Test
    public void testTieBreakByFewerExchanges() {
        Equation eq = new Equation(
                Arrays.asList(PebbleColor.RED), Arrays.asList(PebbleColor.BLUE));
 
        List<Cards> sameCards = Arrays.asList(card(false, PebbleColor.RED));
        Map<PebbleColor, Integer> sameWallet = wallet(1, 0);
 
        Map<String, Object> oneExchange = makeTurn(3, 1, 1, sameWallet,
                Arrays.asList(exchange(eq, true)), sameCards);
        Map<String, Object> noExchange = makeTurn(3, 1, 1, sameWallet,
                new ArrayList<>(), sameCards);
 
        Map<String, Object> result = new CandidateSelector(true).selectBest(
                new HashSet<>(Arrays.asList(oneExchange, noExchange)));
        assertEquals(0, ((List<?>) result.get("exchanges")).size());
    }
}
 