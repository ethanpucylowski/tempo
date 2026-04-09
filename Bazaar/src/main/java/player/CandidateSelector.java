package player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import common.Cards;
import common.Cards.PebbleColor;
import common.Equation;
import common.WalletManager;

/**
 * Selects the best turn from a set of candidate turns based on the strategy's goals
 * and tie-breaking rules.
 * 
 * A turn is represented as a Map with keys:
 *   - "exchanges": List<Map<String, Object>> where each map has:
 *       - "equation": Equation
 *       - "leftToRight": Boolean
 *   - "cards": List<Cards> (cards purchased)
 *   - "wallet": Map<PebbleColor, Integer> (final wallet state)
 *   - "points": Integer (total points earned)
 */
public class CandidateSelector {
    private final boolean maximizePoints;
   
    public CandidateSelector(boolean maximizePoints) {
        this.maximizePoints = maximizePoints;
    }
   
    /**
     * Selects the best turn from the set of candidates by applying
     * filters in order until only one candidate remains.
     *
     * @param candidates the set of candidate turns
     * @return the best turn according to the strategy
     */
    public Map<String, Object> selectBest(Set<Map<String, Object>> candidates) {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Cannot select from empty candidates");
        }
        
        List<Map<String, Object>> filtered = new ArrayList<>(candidates);
       
        filtered = filterByPrimaryGoal(filtered);
        filtered = filterByPoints(filtered);
        filtered = filterByRemainingPebbles(filtered);
        filtered = sortByWallet(filtered);
        filtered = sortByCardSequence(filtered);
        filtered = filterByMinTrades(filtered);
        filtered = sortByExchangeSequence(filtered);
       
        return filtered.get(0);
    }
   
    /**
     * Filter by primary goal: either max points or max cards.
     */
    private List<Map<String, Object>> filterByPrimaryGoal(List<Map<String, Object>> decisions) {
        if (maximizePoints) {
            return filterByMaxPoints(decisions);
        } else {
            return filterByMaxCards(decisions);
        }
    }
   
    /**
     * Keep only decisions with maximum points.
     */
    private List<Map<String, Object>> filterByMaxPoints(List<Map<String, Object>> decisions) {
        if (decisions.isEmpty()) return decisions;
        
        int maxPoints = decisions.stream()
            .mapToInt(d -> (Integer) d.get("points"))
            .max()
            .orElse(0);
        
        return decisions.stream()
            .filter(d -> (Integer) d.get("points") == maxPoints)
            .collect(Collectors.toList());
    }
   
    /**
     * Keep only decisions with maximum cards purchased.
     */
    private List<Map<String, Object>> filterByMaxCards(List<Map<String, Object>> decisions) {
        if (decisions.isEmpty()) return decisions;
        
        int maxCards = decisions.stream()
            .mapToInt(d -> ((List<Cards>) d.get("cards")).size())
            .max()
            .orElse(0);
        
        return decisions.stream()
            .filter(d -> ((List<Cards>) d.get("cards")).size() == maxCards)
            .collect(Collectors.toList());
    }
   
    /**
     * If primary goal was cards, apply points as secondary filter.
     * If primary goal was points, this is a no-op (already filtered).
     */
    private List<Map<String, Object>> filterByPoints(List<Map<String, Object>> decisions) {
        if (decisions.size() <= 1) return decisions;
        
        // Only apply if we filtered by cards first
        if (!maximizePoints) {
            return filterByMaxPoints(decisions);
        }
        
        return decisions;
    }
   
    /**
     * Keep only decisions that leave the minimum number of pebbles in the wallet.
     */
    private List<Map<String, Object>> filterByRemainingPebbles(List<Map<String, Object>> decisions) {
        if (decisions.size() <= 1) return decisions;
        
        int minPebbles = decisions.stream()
            .mapToInt(d -> {
                Map<PebbleColor, Integer> wallet = (Map<PebbleColor, Integer>) d.get("wallet");
                return wallet.values().stream().mapToInt(Integer::intValue).sum();
            })
            .min()
            .orElse(0);
        
        return decisions.stream()
            .filter(d -> {
                Map<PebbleColor, Integer> wallet = (Map<PebbleColor, Integer>) d.get("wallet");
                return wallet.values().stream().mapToInt(Integer::intValue).sum() == minPebbles;
            })
            .collect(Collectors.toList());
    }
   
    /**
     * Sort by wallet comparison (lexicographically by color order).
     * Keep only the decisions with the lexicographically smallest wallet.
     */
    private List<Map<String, Object>> sortByWallet(List<Map<String, Object>> decisions) {
        if (decisions.size() <= 1) return decisions;
        
        decisions.sort(Comparator.comparing(d -> 
            walletToComparableString((Map<PebbleColor, Integer>) d.get("wallet"))
        ));
        
        String bestWallet = walletToComparableString(
            (Map<PebbleColor, Integer>) decisions.get(0).get("wallet")
        );
        
        return decisions.stream()
            .filter(d -> walletToComparableString(
                (Map<PebbleColor, Integer>) d.get("wallet")
            ).equals(bestWallet))
            .collect(Collectors.toList());
    }
   
    /**
     * Convert wallet to a comparable string for lexicographic comparison.
     * Format: "R:count,W:count,B:count,G:count,Y:count"
     */
    private String walletToComparableString(Map<PebbleColor, Integer> wallet) {
        StringBuilder sb = new StringBuilder();
        for (PebbleColor color : PebbleColor.values()) {
            int count = wallet.getOrDefault(color, 0);
            if (sb.length() > 0) sb.append(",");
            sb.append(color.name().charAt(0)).append(":").append(count);
        }
        return sb.toString();
    }
   
    /**
     * Sort by card purchase sequence (lexicographically).
     * Keep only decisions with the lexicographically smallest card sequence.
     */
    private List<Map<String, Object>> sortByCardSequence(List<Map<String, Object>> decisions) {
        if (decisions.size() <= 1) return decisions;
        
        decisions.sort(Comparator.comparing(d -> 
            cardSequenceToComparableString((List<Cards>) d.get("cards"))
        ));
        
        String bestSequence = cardSequenceToComparableString(
            (List<Cards>) decisions.get(0).get("cards")
        );
        
        return decisions.stream()
            .filter(d -> cardSequenceToComparableString(
                (List<Cards>) d.get("cards")
            ).equals(bestSequence))
            .collect(Collectors.toList());
    }
   
    /**
     * Convert card purchase sequence to comparable string.
     * Each card is represented by its pebbles in order, then whether it has a star.
     */
    private String cardSequenceToComparableString(List<Cards> cards) {
        StringBuilder sb = new StringBuilder();
        for (Cards card : cards) {
            if (sb.length() > 0) sb.append("|");
            
            // Add pebbles in order
            for (PebbleColor color : card.getPebbles()) {
                sb.append(color.name().charAt(0));
            }
            
            // Add star indicator
            sb.append(card.hasStar() ? "*" : "-");
        }
        return sb.toString();
    }
   
    /**
     * Keep only decisions with minimum number of exchanges.
     */
    private List<Map<String, Object>> filterByMinTrades(List<Map<String, Object>> decisions) {
        if (decisions.size() <= 1) return decisions;
        
        int minTrades = decisions.stream()
            .mapToInt(d -> ((List<?>) d.get("exchanges")).size())
            .min()
            .orElse(0);
        
        return decisions.stream()
            .filter(d -> ((List<?>) d.get("exchanges")).size() == minTrades)
            .collect(Collectors.toList());
    }
   
    /**
     * Sort by exchange sequence (lexicographically).
     * Keep only decisions with the lexicographically smallest exchange sequence.
     */
    private List<Map<String, Object>> sortByExchangeSequence(List<Map<String, Object>> decisions) {
        if (decisions.size() <= 1) return decisions;
        
        decisions.sort(Comparator.comparing(d -> 
            exchangeSequenceToComparableString((List<Map<String, Object>>) d.get("exchanges"))
        ));
        
        String bestSequence = exchangeSequenceToComparableString(
            (List<Map<String, Object>>) decisions.get(0).get("exchanges")
        );
        
        return decisions.stream()
            .filter(d -> exchangeSequenceToComparableString(
                (List<Map<String, Object>>) d.get("exchanges")
            ).equals(bestSequence))
            .collect(Collectors.toList());
    }
   
    /**
     * Convert exchange sequence to comparable string.
     * Each exchange is represented by which equation was used and the direction.
     */
    private String exchangeSequenceToComparableString(List<Map<String, Object>> exchanges) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> exchange : exchanges) {
            if (sb.length() > 0) sb.append("|");
            
            Equation eq = (Equation) exchange.get("equation");
            boolean leftToRight = (Boolean) exchange.get("leftToRight");
            
            // Represent equation by its left and right sides
            sb.append(pebbleListToString(eq.getLeft()));
            sb.append("=");
            sb.append(pebbleListToString(eq.getRight()));
            sb.append(":");
            sb.append(leftToRight ? "LR" : "RL");
        }
        return sb.toString();
    }
   
    /**
     * Convert a list of pebbles to a string representation.
     */
    private String pebbleListToString(List<PebbleColor> pebbles) {
        StringBuilder sb = new StringBuilder();
        for (PebbleColor color : pebbles) {
            sb.append(color.name().charAt(0));
        }
        return sb.toString();
    }
}