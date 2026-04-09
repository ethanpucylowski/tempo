package player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import common.Cards;
import common.Cards.PebbleColor;
import common.Equation;
import common.Equations;
import common.TurnState;
import common.WalletManager;

/**
 * Implements the Bazaar player strategy for making turn decisions.
 * 
 * The strategy explores all possible sequences of exchanges and card purchases
 * to find the optimal turn according to either:
 *   - Maximize points earned, or
 *   - Maximize cards purchased
 * 
 * Uses breadth-first search with tie-breaking rules to select the best turn.
 */
public class Strategy {
    
    private final boolean maximizePoints;
    private final int maxDepth;
    
    /**
     * Creates a new Strategy.
     *
     * @param maximizePoints true to maximize points, false to maximize cards
     */
    public Strategy(boolean maximizePoints) {
        this(maximizePoints, 10); // Default max depth of 10 exchanges
    }
    
    /**
     * Creates a new Strategy with specified max depth.
     *
     * @param maximizePoints true to maximize points, false to maximize cards
     * @param maxDepth maximum number of exchanges to explore
     */
    public Strategy(boolean maximizePoints, int maxDepth) {
        this.maximizePoints = maximizePoints;
        this.maxDepth = maxDepth;
    }
    
    /**
     * Chooses the best turn given the current game state.
     *
     * @param turnState the current turn state
     * @param equations the available equations
     * @return map representing the chosen turn with keys:
     *         "exchanges", "cards", "wallet", "points"
     */
    public Map<String, Object> chooseTurn(TurnState turnState, Equations equations) {
        if (turnState == null || equations == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        }
        
        // Extract initial state
        Map<PebbleColor, Integer> initialWallet = new EnumMap<>(
            turnState.getActivePlayer().getWallet()
        );
        Map<PebbleColor, Integer> bank = new EnumMap<>(turnState.getBank());
        List<Cards> visibleCards = new ArrayList<>(turnState.getVisibleCards());
        List<Equation> equationList = equations.getEquations();
        
        // Find all possible turns
        Set<Map<String, Object>> candidates = exploreTurns(
            initialWallet,
            bank,
            visibleCards,
            equationList
        );
        
        // If no valid turns found, return empty turn
        if (candidates.isEmpty()) {
            return createEmptyTurn(initialWallet);
        }
        
        // Select the best turn using tie-breaking rules
        CandidateSelector selector = new CandidateSelector(maximizePoints);
        return selector.selectBest(candidates);
    }
    
    /**
     * Explores all possible turns using breadth-first search.
     *
     * @param initialWallet the starting wallet
     * @param bank the bank's pebble state
     * @param visibleCards the cards available for purchase
     * @param equations the available equations
     * @return set of all valid turn candidates
     */
    private Set<Map<String, Object>> exploreTurns(
            Map<PebbleColor, Integer> initialWallet,
            Map<PebbleColor, Integer> bank,
            List<Cards> visibleCards,
            List<Equation> equations) {
        
        Set<Map<String, Object>> allTurns = new HashSet<>();
        
        // Start with initial node
        SearchNode initialNode = SearchNode.createInitial(initialWallet, visibleCards);
        
        // BFS queue
        Queue<SearchNode> queue = new LinkedList<>();
        queue.add(initialNode);
        
        // Track visited states to avoid duplicates
        Set<String> visited = new HashSet<>();
        visited.add(nodeStateKey(initialNode));
        
        while (!queue.isEmpty()) {
            SearchNode current = queue.poll();
            
            // Try to purchase cards from this state
            List<Map<String, Object>> purchaseTurns = explorePurchases(
                current,
                bank,
                new ArrayList<>(),
                new HashSet<>()
            );
            allTurns.addAll(purchaseTurns);
            
            // If we haven't exceeded max depth, try exchanges
            if (current.getDepth() < maxDepth) {
                for (Equation equation : equations) {
                    // Try left-to-right
                    if (current.canApplyExchange(equation, true, bank)) {
                        SearchNode nextNode = current.afterExchange(equation, true, bank);
                        String stateKey = nodeStateKey(nextNode);
                        
                        if (!visited.contains(stateKey)) {
                            visited.add(stateKey);
                            queue.add(nextNode);
                        }
                    }
                    
                    // Try right-to-left
                    if (current.canApplyExchange(equation, false, bank)) {
                        SearchNode nextNode = current.afterExchange(equation, false, bank);
                        String stateKey = nodeStateKey(nextNode);
                        
                        if (!visited.contains(stateKey)) {
                            visited.add(stateKey);
                            queue.add(nextNode);
                        }
                    }
                }
            }
        }
        
        return allTurns;
    }
    
    /**
     * Explores all possible card purchase sequences from a given node.
     * Uses recursive backtracking to try all orderings.
     *
     * @param node the current search node
     * @param bank the bank state
     * @param purchasedCards cards purchased so far (in order)
     * @param usedCards cards already purchased (for fast lookup)
     * @return list of complete turns with different purchase sequences
     */
    private List<Map<String, Object>> explorePurchases(
            SearchNode node,
            Map<PebbleColor, Integer> bank,
            List<Cards> purchasedCards,
            Set<Cards> usedCards) {
        
        List<Map<String, Object>> turns = new ArrayList<>();
        
        // Get current wallet after previous purchases
        Map<PebbleColor, Integer> currentWallet = WalletManager.copyWallet(node.getWallet());
        for (Cards card : purchasedCards) {
            WalletManager.removePebbles(currentWallet, card.getPebbles());
        }
        
        // Try to purchase any remaining card
        boolean purchasedAny = false;
        for (Cards card : node.getRemainingCards()) {
            if (!usedCards.contains(card) && WalletManager.hasPebbles(currentWallet, card.getPebbles())) {
                purchasedAny = true;
                
                // Add this card to purchased list
                List<Cards> newPurchased = new ArrayList<>(purchasedCards);
                newPurchased.add(card);
                
                Set<Cards> newUsed = new HashSet<>(usedCards);
                newUsed.add(card);
                
                // Recursively try to purchase more cards
                turns.addAll(explorePurchases(node, bank, newPurchased, newUsed));
            }
        }
        
        // If we couldn't purchase any more cards, this is a complete turn
        if (!purchasedAny) {
            turns.add(createTurn(node, currentWallet, purchasedCards, bank));
        }
        
        return turns;
    }
    
    /**
     * Creates a turn representation from a node and purchase sequence.
     *
     * @param node the search node (contains exchange sequence)
     * @param finalWallet the wallet after all purchases
     * @param purchasedCards the cards purchased
     * @param bank the bank state (for calculating remaining pebbles in bank)
     * @return turn map
     */
    private Map<String, Object> createTurn(
            SearchNode node,
            Map<PebbleColor, Integer> finalWallet,
            List<Cards> purchasedCards,
            Map<PebbleColor, Integer> bank) {
        
        Map<String, Object> turn = new HashMap<>();
        
        turn.put("exchanges", node.getExchangeSequence());
        turn.put("cards", new ArrayList<>(purchasedCards));
        turn.put("wallet", finalWallet);
        
        // Calculate total points
        int totalPoints = purchasedCards.stream()
            .mapToInt(card -> calculateCardPoints(card, bank))
            .sum();
        turn.put("points", totalPoints);
        
        return turn;
    }
    
    /**
     * Calculates the points for a single card based on remaining bank pebbles.
     *
     * @param card the card
     * @param bank the bank state
     * @return points for this card
     */
    private int calculateCardPoints(Cards card, Map<PebbleColor, Integer> bank) {
        int remainingPebbles = WalletManager.getTotalPebbles(bank);
        
        if (card.hasStar()) {
            // Happy card scoring
            if (remainingPebbles >= 3) return 2;
            if (remainingPebbles == 2) return 3;
            if (remainingPebbles == 1) return 5;
            return 8; // 0 pebbles
        } else {
            // Regular card scoring
            if (remainingPebbles >= 3) return 1;
            if (remainingPebbles == 2) return 2;
            if (remainingPebbles == 1) return 3;
            return 5; // 0 pebbles
        }
    }
    
    /**
     * Creates an empty turn (no exchanges, no purchases).
     *
     * @param wallet the player's wallet
     * @return empty turn map
     */
    private Map<String, Object> createEmptyTurn(Map<PebbleColor, Integer> wallet) {
        Map<String, Object> turn = new HashMap<>();
        turn.put("exchanges", new ArrayList<>());
        turn.put("cards", new ArrayList<>());
        turn.put("wallet", wallet);
        turn.put("points", 0);
        return turn;
    }
    
    /**
     * Creates a unique key for a search node state to detect duplicates.
     * The key includes wallet state and remaining cards.
     *
     * @param node the search node
     * @return unique state key
     */
    private String nodeStateKey(SearchNode node) {
        StringBuilder sb = new StringBuilder();
        
        // Add wallet state
        Map<PebbleColor, Integer> wallet = node.getWallet();
        for (PebbleColor color : PebbleColor.values()) {
            sb.append(color.name().charAt(0))
              .append(":")
              .append(wallet.getOrDefault(color, 0))
              .append(",");
        }
        
        sb.append("|");
        
        // Add remaining cards (sorted for consistency)
        List<String> cardKeys = new ArrayList<>();
        for (Cards card : node.getRemainingCards()) {
            StringBuilder cardKey = new StringBuilder();
            for (PebbleColor color : card.getPebbles()) {
                cardKey.append(color.name().charAt(0));
            }
            cardKey.append(card.hasStar() ? "*" : "-");
            cardKeys.add(cardKey.toString());
        }
        Collections.sort(cardKeys);
        sb.append(String.join(",", cardKeys));
        
        return sb.toString();
    }
    
    /**
     * Gets whether this strategy maximizes points.
     *
     * @return true if maximizing points, false if maximizing cards
     */
    public boolean isMaximizePoints() {
        return maximizePoints;
    }
    
    /**
     * Gets the maximum search depth.
     *
     * @return max depth
     */
    public int getMaxDepth() {
        return maxDepth;
    }
}