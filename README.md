# Bazaar — Milestone 4 - Ethan Pucylowski and Caleb Pucylowski

This milestone introduces a player protocol (`Planning/player-protocol.md`)
describing how the referee interacts with player components during gameplay.
---

## Source Files

### `Cards.java`
Represents a single card: an immutable list of exactly 5 pebbles and a boolean star flag. Key methods:

- `createCard(List<PebbleColor>, boolean)` — static factory
- `canAcquire(Map<PebbleColor, Integer>)` — checks whether a player's pebble counts cover the card's cost
- `score(int pebblesRemaining)` — returns a point value from the scoring table (plain: 1/2/3/5; star: 2/3/5/8 for ≥3/2/1/0 pebbles remaining)
- `render()` — e.g. `[ R W B G Y ]  ★`

Also contains `PebbleColor` as a nested enum (with `abbreviation()` and `hexColor()`).

### `Equation.java`
A single bidirectional equation between two disjoint pebble collections (1–4 pebbles per side, no shared colors). Key methods:

- `canApplyLeftToRight(playerPebbles, bankPebbles)`
- `canApplyRightToLeft(playerPebbles, bankPebbles)`
- `canApply(playerPebbles, bankPebbles)` — either direction
- `render()` — e.g. `"R W = B G"`
- `equals()` is symmetric: `R W = B G` equals `B G = R W`

### `Equations.java`
A table of up to 10 equations. Key methods:

- `createTable(List<Equation>)` — static factory
- `createRandomTable()` — generates exactly 10 valid random equations
- `filterApplicable(playerPebbles, bankPebbles)` — returns a new table with only the equations the player can currently use (in either direction)
- `render()` — numbered multi-line display

## Test Files

### Unit Tests

| File | Tests | Coverage |
|---|---|---|
| `CardsTest.java` | 32 | PebbleColor, construction, canAcquire, score, render, equals/hashCode |
| `EquationTest.java` | 34 | construction, canApplyLeftToRight, canApplyRightToLeft, canApply, render, equals/hashCode |
| `EquationsTest.java` | 38 | construction, createRandomTable, filterApplicable, render, size/getEquations |

### File-Driven Tests

Each class with file-driven tests reads numbered JSON pairs from `src/Tests/<ClassName>/in/` and `src/Tests/<ClassName>/out/`. The input file describes the operation and inputs; the output file contains the expected result.

**Cards** (20 pairs): `canAcquire` (exact match, surplus, missing color, insufficient duplicates, star variants), `score` (all 8 scoring table cells), and `render`.

**Equation** (20 pairs): `canApplyLeftToRight`, `canApplyRightToLeft`, `canApply` (both directions, neither, each direction only), and `render`.

**Equations** (20 pairs): `filterApplicable` (all/none/subset, bank constraint, reverse direction, multiplicity, empty table), `size`, and `renderContains`.

## Design Memo

`Planning/game-state.md` describes the data a referee needs to manage a game in progress and lists the 10 functions it must provide: setup, grant turn, draw pebble, validate/apply exchange, validate/apply card purchase, replenish visible cards, eliminate player, query scores, detect end conditions, and announce outcome.
---

## Player Components

This milestone introduces a player module that computes turn decisions using
a bounded search over exchanges and card purchases.

### `Strategy.java`
Entry point for computing a player's turn.

- Takes a `TurnState` and `Equations`
- Explores possible exchange sequences (BFS)
- Explores all valid card purchase sequences
- Generates candidate turns
- Selects the best turn using tie-breaking rules

---

### `SearchNode.java`
Represents a state in the search space.

- Stores wallet, exchange sequence, remaining cards, and depth
- Supports applying exchanges and purchases to generate new states
- Ensures validity of transitions (sufficient pebbles, valid bank state)

---

### `CandidateSelector.java`
Selects the best turn from a set of candidates.

- Maximizes either points or number of cards
- Applies tie-breaking rules:
  - points (if maximizing cards)
  - remaining pebbles
  - wallet ordering
  - card sequence ordering
  - number of exchanges
  - exchange sequence ordering

---

### `WalletManager.java`
Utility class for managing pebble wallets.

- Add/remove pebbles
- Check affordability
- Copy and merge wallets
- Count pebbles
- Compare wallets lexicographically

## Running Tests

```bash
cd Bazzar
mvn test
```

