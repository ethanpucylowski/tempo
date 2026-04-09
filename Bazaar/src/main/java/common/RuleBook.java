package common;

import common.Cards.PebbleColor;

import java.util.List;
import java.util.Map;

/**
 * Encodes the legality rules of Bazaar.
 *
 * Placed in Common so both the referee and player can verify moves
 * without creating a circular dependency. This class must never
 * import from referee.* or player.*.
 *
 * A ExchangeRequest pairs an Equation with a direction:
 *   leftToRight = true  means player gives left, receives right
 *   leftToRight = false means player gives right, receives left
 */
public class RuleBook {

    /**
     * Represents a single directed application of an equation.
     * This is what the player returns in response to the first referee call.
     */
    public static class ExchangeRequest {
        private final Equation equation;
        private final boolean leftToRight;

        public ExchangeRequest(Equation equation, boolean leftToRight) {
            if (equation == null) throw new IllegalArgumentException("Equation must not be null.");
            this.equation    = equation;
            this.leftToRight = leftToRight;
        }

        public Equation getEquation()   { return equation; }
        public boolean isLeftToRight()  { return leftToRight; }

        /** The pebbles the player must give to the bank. */
        public List<PebbleColor> getCost() {
            return leftToRight ? equation.getLeft() : equation.getRight();
        }

        /** The pebbles the player receives from the bank. */
        public List<PebbleColor> getGain() {
            return leftToRight ? equation.getRight() : equation.getLeft();
        }
    }

    private final List<Equation> equations;

    public RuleBook(List<Equation> equations) {
        if (equations == null) throw new IllegalArgumentException("Equations must not be null.");
        this.equations = List.copyOf(equations);
    }

    // Call 1: referee asks player which exchanges they want to make.
    // Player returns a List<ExchangeRequest>.

    /**
     * Checks legality of the player's response to the first referee call.
     *
     * Each exchange in the sequence must:
     *   1. Use an equation from the global set
     *   2. Be applicable in the requested direction given the running wallet
     *   3. Be satisfiable by the bank at that point in the sequence
     *
     * @param state    the TurnState shared with the player
     * @param requests the ordered exchanges the player wishes to perform
     * @return true iff the entire sequence is legal
     */
    public boolean isLegalExchangeSequence(TurnState state,
                                           List<ExchangeRequest> requests) {
        Map<PebbleColor, Integer> wallet =
            WalletManager.copyWallet(state.getActivePlayer().getWallet());
        Map<PebbleColor, Integer> bank =
            WalletManager.copyWallet(state.getBank());

        for (ExchangeRequest request : requests) {
            // Must be a known equation
            if (!equations.contains(request.getEquation())) {
                return false;
            }
            // Must be applicable in the requested direction
            if (request.isLeftToRight()) {
                if (!request.getEquation().canApplyLeftToRight(wallet, bank)) {
                    return false;
                }
            } else {
                if (!request.getEquation().canApplyRightToLeft(wallet, bank)) {
                    return false;
                }
            }
            // Apply the exchange to the running simulation
            WalletManager.removePebbles(wallet, request.getCost());
            WalletManager.addPebbles(wallet, request.getGain());
        }
        return true;
    }

    // Call 2: referee asks player which card they want to buy.
    // Player returns a Cards (or null to pass).

    /**
     * Checks legality of the player's response to the second referee call.
     *
     * The card must:
     *   1. Be null (player passes) — always legal
     *   2. Be present in the visible cards tableau
     *   3. Be affordable with the wallet after exchanges are applied
     *
     * @param state     the TurnState shared with the player
     * @param exchanges the exchanges already validated by isLegalExchangeSequence
     * @param requested the card the player wishes to buy, or null to pass
     * @return true iff the purchase is legal
     */
    public boolean isLegalCardPurchase(TurnState state,
                                       List<ExchangeRequest> exchanges,
                                       Cards requested) {
        if (requested == null) {
            return true;
        }
        if (!state.getVisibleCards().contains(requested)) {
            return false;
        }
        Map<PebbleColor, Integer> wallet = applyExchanges(
            state.getActivePlayer().getWallet(), exchanges
        );
        return requested.canAcquire(wallet);
    }

    // Shared helpers

    /**
     * Simulates applying an exchange sequence to a wallet.
     * Does not mutate the original wallet.
     *
     * @param start   the wallet before exchanges
     * @param requests the exchange sequence to apply
     * @return a new wallet reflecting all exchanges
     */
    public Map<PebbleColor, Integer> applyExchanges(
            Map<PebbleColor, Integer> start,
            List<ExchangeRequest> requests) {
        Map<PebbleColor, Integer> result = WalletManager.copyWallet(start);
        for (ExchangeRequest request : requests) {
            WalletManager.removePebbles(result, request.getCost());
            WalletManager.addPebbles(result, request.getGain());
        }
        return result;
    }

    /**
     * Computes the points a player earns for purchasing a given card,
     * based on how many pebbles they have remaining after the purchase.
     *
     * Delegates to Cards.score() which already encodes the star/no-star
     * point table from the spec.
     *
     * @param card   the card being purchased
     * @param wallet the player's wallet after exchanges, before purchase
     * @return the points earned
     */
    public int computeCardPoints(Cards card,
                                 Map<PebbleColor, Integer> wallet) {
        Map<PebbleColor, Integer> afterPurchase = WalletManager.copyWallet(wallet);
        WalletManager.removePebbles(afterPurchase, card.getPebbles());
        int remaining = WalletManager.getTotalPebbles(afterPurchase);
        return card.score(remaining);
    }
}