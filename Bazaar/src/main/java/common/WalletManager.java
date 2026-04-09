package common;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import common.Cards.PebbleColor;

/**
 * Utility class for managing wallet operations (adding, removing, comparing pebbles).
 */
public class WalletManager {

    /**
     * Adds pebbles to a wallet.
     *
     * @param wallet the wallet to modify
     * @param pebbles the pebbles to add
     */
    public static void addPebbles(Map<PebbleColor, Integer> wallet, List<PebbleColor> pebbles) {
        for (PebbleColor color : pebbles) {
            wallet.put(color, wallet.getOrDefault(color, 0) + 1);
        }
    }

    /**
     * Removes pebbles from a wallet.
     *
     * @param wallet the wallet to modify
     * @param pebbles the pebbles to remove
     * @throws IllegalArgumentException if wallet doesn't have enough pebbles
     */
    public static void removePebbles(Map<PebbleColor, Integer> wallet, List<PebbleColor> pebbles) {
        // First check if we have enough
        Map<PebbleColor, Integer> required = new EnumMap<>(PebbleColor.class);
        for (PebbleColor color : pebbles) {
            required.put(color, required.getOrDefault(color, 0) + 1);
        }

        for (PebbleColor color : required.keySet()) {
            if (wallet.getOrDefault(color, 0) < required.get(color)) {
                throw new IllegalArgumentException(
                    "Insufficient pebbles: need " + required.get(color) + 
                    " " + color + ", have " + wallet.getOrDefault(color, 0)
                );
            }
        }

        // Now remove
        for (PebbleColor color : required.keySet()) {
            int currentCount = wallet.get(color);
            int newCount = currentCount - required.get(color);
            
            if (newCount > 0) {
                wallet.put(color, newCount);
            } else {
                wallet.remove(color);
            }
        }
    }

    /**
     * Checks if a wallet has at least the specified pebbles.
     *
     * @param wallet the wallet to check
     * @param required the required pebbles
     * @return true if wallet has enough pebbles
     */
    public static boolean hasPebbles(Map<PebbleColor, Integer> wallet, List<PebbleColor> required) {
        Map<PebbleColor, Integer> requiredCount = new EnumMap<>(PebbleColor.class);
        for (PebbleColor color : required) {
            requiredCount.put(color, requiredCount.getOrDefault(color, 0) + 1);
        }

        for (PebbleColor color : requiredCount.keySet()) {
            if (wallet.getOrDefault(color, 0) < requiredCount.get(color)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Counts the total number of pebbles in a wallet.
     *
     * @param wallet the wallet
     * @return total pebble count
     */
    public static int getTotalPebbles(Map<PebbleColor, Integer> wallet) {
        return wallet.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Creates a copy of a wallet.
     *
     * @param wallet the wallet to copy
     * @return a new wallet with the same contents
     */
    public static Map<PebbleColor, Integer> copyWallet(Map<PebbleColor, Integer> wallet) {
        return new EnumMap<>(wallet);
    }

    /**
     * Converts a list of pebbles to a count map.
     *
     * @param pebbles the pebbles
     * @return map of color to count
     */
    public static Map<PebbleColor, Integer> toCountMap(List<PebbleColor> pebbles) {
        Map<PebbleColor, Integer> counts = new EnumMap<>(PebbleColor.class);
        for (PebbleColor color : pebbles) {
            counts.put(color, counts.getOrDefault(color, 0) + 1);
        }
        return counts;
    }

    /**
     * Creates an empty wallet.
     *
     * @return empty EnumMap wallet
     */
    public static Map<PebbleColor, Integer> createEmpty() {
        return new EnumMap<>(PebbleColor.class);
    }

    /**
     * Merges two wallets (adds counts together).
     *
     * @param wallet1 first wallet
     * @param wallet2 second wallet
     * @return new wallet with combined counts
     */
    public static Map<PebbleColor, Integer> merge(
            Map<PebbleColor, Integer> wallet1,
            Map<PebbleColor, Integer> wallet2) {
        
        Map<PebbleColor, Integer> result = new EnumMap<>(wallet1);
        
        for (PebbleColor color : wallet2.keySet()) {
            result.put(color, result.getOrDefault(color, 0) + wallet2.get(color));
        }
        
        return result;
    }

    /**
     * Compares two wallets lexicographically by color order.
     *
     * @param wallet1 first wallet
     * @param wallet2 second wallet
     * @return negative if wallet1 < wallet2, 0 if equal, positive if wallet1 > wallet2
     */
    public static int compareLexicographically(
            Map<PebbleColor, Integer> wallet1,
            Map<PebbleColor, Integer> wallet2) {
        
        for (PebbleColor color : PebbleColor.values()) {
            int count1 = wallet1.getOrDefault(color, 0);
            int count2 = wallet2.getOrDefault(color, 0);
            
            if (count1 != count2) {
                return Integer.compare(count1, count2);
            }
        }
        
        return 0;
    }

    /**
     * Converts wallet to a readable string.
     *
     * @param wallet the wallet
     * @return string representation (e.g., "RED=2 BLUE=1")
     */
    public static String toString(Map<PebbleColor, Integer> wallet) {
        if (wallet.isEmpty()) {
            return "(empty)";
        }

        StringBuilder sb = new StringBuilder();
        for (PebbleColor color : PebbleColor.values()) {
            int count = wallet.getOrDefault(color, 0);
            if (count > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(color.name()).append("=").append(count);
            }
        }
        
        return sb.toString();
    }
}