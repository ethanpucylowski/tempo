package player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import common.Cards;
import common.Cards.PebbleColor;
import common.Equation;
import common.WalletManager;

/**
 * Represents a state in the turn search space.
 * Each node contains:
 *   - current wallet state
 *   - sequence of exchanges taken to reach this state
 *   - cards that can still be purchased
 */
public class SearchNode {
    private final Map<PebbleColor, Integer> wallet;
    private final List<Map<String, Object>> exchangeSequence;
    private final Set<Cards> remainingCards;
    private final int depth;

    /**
     * Creates a new SearchNode.
     *
     * @param wallet           current wallet state
     * @param exchangeSequence sequence of exchanges taken so far
     * @param remainingCards   cards still available for purchase
     * @param depth            number of exchanges performed
     */
    public SearchNode(
            Map<PebbleColor, Integer> wallet,
            List<Map<String, Object>> exchangeSequence,
            Set<Cards> remainingCards,
            int depth) {
        
        if (wallet == null || exchangeSequence == null || remainingCards == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        }
        
        this.wallet = new EnumMap<>(wallet);
        this.exchangeSequence = new ArrayList<>(exchangeSequence);
        this.remainingCards = new HashSet<>(remainingCards);
        this.depth = depth;
    }

    /**
     * Creates the initial search node with starting wallet and all cards available.
     *
     * @param initialWallet  the player's starting wallet
     * @param visibleCards   all visible cards
     * @return initial search node
     */
    public static SearchNode createInitial(
            Map<PebbleColor, Integer> initialWallet,
            List<Cards> visibleCards) {
        
        return new SearchNode(
            initialWallet,
            new ArrayList<>(),
            new HashSet<>(visibleCards),
            0
        );
    }

    /**
     * Gets the current wallet state.
     *
     * @return wallet map
     */
    public Map<PebbleColor, Integer> getWallet() {
        return Collections.unmodifiableMap(wallet);
    }

    /**
     * Gets the mutable wallet (for internal use).
     *
     * @return mutable wallet map
     */
    public Map<PebbleColor, Integer> getMutableWallet() {
        return new EnumMap<>(wallet);
    }

    /**
     * Gets the sequence of exchanges taken to reach this node.
     *
     * @return list of exchange maps with "equation" and "leftToRight" keys
     */
    public List<Map<String, Object>> getExchangeSequence() {
        return Collections.unmodifiableList(exchangeSequence);
    }

    /**
     * Gets the mutable exchange sequence (for internal use).
     *
     * @return mutable list of exchanges
     */
    public List<Map<String, Object>> getMutableExchangeSequence() {
        return new ArrayList<>(exchangeSequence);
    }

    /**
     * Gets the cards still available for purchase.
     *
     * @return set of remaining cards
     */
    public Set<Cards> getRemainingCards() {
        return Collections.unmodifiableSet(remainingCards);
    }

    /**
     * Gets the mutable remaining cards (for internal use).
     *
     * @return mutable set of cards
     */
    public Set<Cards> getMutableRemainingCards() {
        return new HashSet<>(remainingCards);
    }

    /**
     * Gets the depth (number of exchanges performed).
     *
     * @return depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Checks if a card can be purchased with the current wallet.
     *
     * @param card the card to check
     * @return true if purchasable
     */
    public boolean canPurchase(Cards card) {
        Map<PebbleColor, Integer> cost = getCost(card);
        
        for (PebbleColor color : cost.keySet()) {
            if (wallet.getOrDefault(color, 0) < cost.get(color)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Gets the pebble cost of a card.
     *
     * @param card the card
     * @return map of color to count
     */
    private Map<PebbleColor, Integer> getCost(Cards card) {
        Map<PebbleColor, Integer> cost = new EnumMap<>(PebbleColor.class);
        
        for (PebbleColor color : card.getPebbles()) {
            cost.put(color, cost.getOrDefault(color, 0) + 1);
        }
        
        return cost;
    }

    /**
     * Creates a new node after purchasing a card.
     *
     * @param card the card to purchase
     * @return new node with updated wallet and remaining cards
     */
    public SearchNode afterPurchase(Cards card) {
        if (!canPurchase(card)) {
            throw new IllegalArgumentException("Cannot purchase card with current wallet");
        }

        Map<PebbleColor, Integer> newWallet = new EnumMap<>(wallet);
        Map<PebbleColor, Integer> cost = getCost(card);
        
        // Deduct cost from wallet
        for (PebbleColor color : cost.keySet()) {
            int currentCount = newWallet.getOrDefault(color, 0);
            int newCount = currentCount - cost.get(color);
            
            if (newCount > 0) {
                newWallet.put(color, newCount);
            } else {
                newWallet.remove(color);
            }
        }

        Set<Cards> newRemaining = new HashSet<>(remainingCards);
        newRemaining.remove(card);

        return new SearchNode(newWallet, exchangeSequence, newRemaining, depth);
    }

    /**
     * Creates a new node after applying an exchange.
     *
     * @param equation    the equation to apply
     * @param leftToRight true for left-to-right, false for right-to-left
     * @param bank        the current bank state
     * @return new node with updated wallet and exchange sequence
     */
    public SearchNode afterExchange(
            Equation equation,
            boolean leftToRight,
            Map<PebbleColor, Integer> bank) {
        
        List<PebbleColor> give = leftToRight ? equation.getLeft() : equation.getRight();
        List<PebbleColor> receive = leftToRight ? equation.getRight() : equation.getLeft();

        // Check if player has required pebbles
        Map<PebbleColor, Integer> giveCount = new EnumMap<>(PebbleColor.class);
        for (PebbleColor color : give) {
            giveCount.put(color, giveCount.getOrDefault(color, 0) + 1);
        }
        
        for (PebbleColor color : giveCount.keySet()) {
            if (wallet.getOrDefault(color, 0) < giveCount.get(color)) {
                throw new IllegalArgumentException("Insufficient pebbles for exchange");
            }
        }

        // Check if bank has required pebbles
        Map<PebbleColor, Integer> receiveCount = new EnumMap<>(PebbleColor.class);
        for (PebbleColor color : receive) {
            receiveCount.put(color, receiveCount.getOrDefault(color, 0) + 1);
        }
        
        for (PebbleColor color : receiveCount.keySet()) {
            if (bank.getOrDefault(color, 0) < receiveCount.get(color)) {
                throw new IllegalArgumentException("Bank has insufficient pebbles");
            }
        }

        // Apply exchange
        Map<PebbleColor, Integer> newWallet = new EnumMap<>(wallet);
        
        // Remove given pebbles
        for (PebbleColor color : giveCount.keySet()) {
            int currentCount = newWallet.getOrDefault(color, 0);
            int newCount = currentCount - giveCount.get(color);
            
            if (newCount > 0) {
                newWallet.put(color, newCount);
            } else {
                newWallet.remove(color);
            }
        }
        
        // Add received pebbles
        for (PebbleColor color : receiveCount.keySet()) {
            newWallet.put(color, newWallet.getOrDefault(color, 0) + receiveCount.get(color));
        }

        // Create exchange record
        Map<String, Object> exchange = new HashMap<>();
        exchange.put("equation", equation);
        exchange.put("leftToRight", leftToRight);

        List<Map<String, Object>> newSequence = new ArrayList<>(exchangeSequence);
        newSequence.add(exchange);

        return new SearchNode(newWallet, newSequence, remainingCards, depth + 1);
    }

    /**
     * Checks if this node can apply an exchange.
     *
     * @param equation    the equation
     * @param leftToRight the direction
     * @param bank        the bank state
     * @return true if exchange is possible
     */
    public boolean canApplyExchange(
            Equation equation,
            boolean leftToRight,
            Map<PebbleColor, Integer> bank) {
        
        List<PebbleColor> give = leftToRight ? equation.getLeft() : equation.getRight();
        List<PebbleColor> receive = leftToRight ? equation.getRight() : equation.getLeft();

        // Check player has pebbles to give
        Map<PebbleColor, Integer> giveCount = new EnumMap<>(PebbleColor.class);
        for (PebbleColor color : give) {
            giveCount.put(color, giveCount.getOrDefault(color, 0) + 1);
        }
        
        for (PebbleColor color : giveCount.keySet()) {
            if (wallet.getOrDefault(color, 0) < giveCount.get(color)) {
                return false;
            }
        }

        // Check bank has pebbles to give
        Map<PebbleColor, Integer> receiveCount = new EnumMap<>(PebbleColor.class);
        for (PebbleColor color : receive) {
            receiveCount.put(color, receiveCount.getOrDefault(color, 0) + 1);
        }
        
        for (PebbleColor color : receiveCount.keySet()) {
            if (bank.getOrDefault(color, 0) < receiveCount.get(color)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchNode that = (SearchNode) o;
        return depth == that.depth &&
                wallet.equals(that.wallet) &&
                exchangeSequence.equals(that.exchangeSequence) &&
                remainingCards.equals(that.remainingCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wallet, exchangeSequence, remainingCards, depth);
    }

    @Override
    public String toString() {
        return "SearchNode{depth=" + depth + 
               ", wallet=" + wallet + 
               ", exchanges=" + exchangeSequence.size() + 
               ", remainingCards=" + remainingCards.size() + "}";
    }
}