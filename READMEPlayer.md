# player

This package contains the player’s decision-making logic for Bazaar. It computes a single turn by exploring possible exchanges and card purchases, then selecting the best outcome according to a greedy strategy and tie-breaking rules.

## Files

### `Strategy.java`
Entry point for computing a player's turn.

Given a `TurnState` and available `Equations`, this class:
- explores possible exchange sequences (bounded depth)
- explores all valid card purchase sequences from each reachable state
- collects all candidate turns
- selects the best turn using `CandidateSelector`

Uses breadth-first search over exchanges and recursive exploration of purchases.

---

### `SearchNode.java`
Represents a state in the search space during turn computation.

Each node contains:
- current wallet (pebbles owned)
- sequence of exchanges taken to reach the state
- remaining cards available for purchase
- depth (number of exchanges performed)

Includes:
- `createInitial(...)` — constructs the starting node
- `canPurchase(card)` — checks if a card is affordable
- `afterPurchase(card)` — returns a new node after buying a card
- `canApplyExchange(eq, dir, bank)` — checks if an exchange is valid
- `afterExchange(eq, dir, bank)` — returns a new node after applying an exchange

---

### `CandidateSelector.java`
Selects the best turn from a set of candidates.

Each candidate includes:
- exchange sequence
- purchased cards
- resulting wallet
- total points

Selection process:
- maximize primary goal (points or number of cards)
- break ties using:
  - points (if maximizing cards)
  - remaining pebbles
  - wallet ordering
  - card sequence ordering
  - number of exchanges
  - exchange sequence ordering

Returns a single deterministic best turn.

---

### `WalletManager.java`
Utility class for wallet (pebble) operations.

Includes:
- `addPebbles(wallet, pebbles)`
- `removePebbles(wallet, pebbles)`
- `hasPebbles(wallet, required)`
- `copyWallet(wallet)`
- `merge(wallet1, wallet2)`
- `getTotalPebbles(wallet)`
- `compareLexicographically(wallet1, wallet2)`

Used throughout the search to validate and update wallet states.

---

## Summary

The player computes its turn by:
1. exploring reachable states via exchanges (BFS),
2. exploring all purchase sequences from each state,
3. evaluating all candidate turns,
4. selecting the best one using tie-breaking rules.

This design separates:
- search (`Strategy`, `SearchNode`)
- evaluation (`CandidateSelector`)
- data manipulation (`WalletManager`)

resulting in a modular and deterministic player implementation.