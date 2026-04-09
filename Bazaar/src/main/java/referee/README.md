# referee

This package contains data representations that belong exclusively to the referee.

## Files

### `GameState.java`
The complete game state maintained by the referee throughout a Bazaar game. Fields:
- `equations` — the fixed set of up to 10 equations
- `bank` — current pebble counts held by the bank
- `visibleCards` — the up to 4 face-up cards available for purchase
- `deck` — the remaining cards not yet visible
- `players` — all `PlayerState` objects in turn order
- `activeIndex` — index of the player whose turn it currently is

Key functionality:
- `isGameOver()` — returns true when all cards are gone or no active players remain
- `extractTurnState()` — produces the limited `TurnState` snapshot to send to the active player
- `render()` — full human-readable display of the game state for debugging

### `PlayerState.java`
The referee's view of a single player. Fields:
- `name` — display name
- `wallet` — the player's current pebble counts
- `score` — points accumulated from purchased cards
- `active` — whether the player is still in the game 

Used internally by `GameState` and referenced (read-only) in `TurnState`.
