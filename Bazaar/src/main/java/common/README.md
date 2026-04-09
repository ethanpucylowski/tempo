# common

This package contains data representations shared across the Bazaar game system — used by both the referee and any player components.

## Files

### `Cards.java`
Represents a single purchasable card. A card holds exactly 5 pebbles and an optional star flag. Includes:
- `createCard(List<PebbleColor>, boolean)` — static factory
- `canAcquire(Map<PebbleColor, Integer>)` — checks if a player can afford the card
- `score(int pebblesRemaining)` — computes points earned on purchase
- `render()` — ASCII display, e.g. `[ R W B G Y ]  ★`

Also defines the `PebbleColor` enum (`RED`, `WHITE`, `BLUE`, `GREEN`, `YELLOW`) used throughout the project.

### `Equation.java`
Represents a single bidirectional trade between two pebble collections (1–4 pebbles per side). Includes:
- `canApplyLeftToRight(playerPebbles, bankPebbles)`
- `canApplyRightToLeft(playerPebbles, bankPebbles)`
- `canApply(playerPebbles, bankPebbles)` — either direction
- `render()` — e.g. `"R W = B G"`
- Symmetric `equals()` — `R W = B G` and `B G = R W` are the same equation

### `Equations.java`
Represents the table of up to 10 equations fixed for an entire game. Includes:
- `createTable(List<Equation>)` — static default table
- `createRandomTable()` — generates 10 valid random equations
- `filterApplicable(playerPebbles, bankPebbles)` — returns equations usable in at least one direction
- `render()` — =formatted display

### `TurnState.java`
A read-only snapshot the refere sends to the active player at the start of their turn. Contains only what the player is permitted to know:
- The bank's current pebble counts
- The active player's own wallet and score
- The scores of all other active players 
- The currently visible cards

Includes `render()` for display.
