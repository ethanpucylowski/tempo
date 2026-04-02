# Player Protocol

## Overview

This document specifies the protocol between the referee and a player component in Bazaar.

The referee controls the game and owns the authoritative game state. A player does not
initiate communication. It only responds when the referee asks it to act.

This milestone introduces new player-side behavior: the player computes its turn using an
internal strategy that searches possible exchanges and card purchases, then applies the
required tie-breaking rules to choose a single best turn. The referee does not observe this
internal search process. It only sends turn information to the player and receives one turn
decision in return. :contentReference[oaicite:5]{index=5} :contentReference[oaicite:6]{index=6}

---

## Components

### Referee
The referee:
- maintains the official game state;
- determines whose turn it is;
- sends turn information to the active player;
- validates the player's response;
- applies the chosen action to the game state;
- ends the game when the ending condition is reached.

### Player
The player:
- waits for a request from the referee;
- receives the current turn state;
- computes one turn decision;
- returns that decision to the referee.

### Player Strategy (internal)
Inside the player, the new strategy logic searches through possible turn choices.

Conceptually, the player's internal behavior is:
- represent reachable turn states during search;
- explore exchange sequences;
- explore card-purchase sequences from each reachable state;
- collect candidate turns;
- select the best candidate according to the active goal and tie-breakers;
- return the selected turn to the referee.

These internal responsibilities correspond to the new player-side classes:
- `Strategy` drives the search for the best turn; :contentReference[oaicite:7]{index=7}
- `SearchNode` represents an intermediate search state; :contentReference[oaicite:8]{index=8}
- `CandidateSelector` applies the tie-breaking rules to candidate turns; :contentReference[oaicite:9]{index=9}
- `WalletManager` supports wallet and pebble operations used by the search. :contentReference[oaicite:10]{index=10}

These classes are internal to the player and are not separate protocol participants.

---

## Protocol Phases

The interaction between referee and player has three phases:

1. initialization
2. repeated turn-taking
3. termination

---

## 1. Initialization

Before play begins, the referee establishes the participants and the order in which they
will act.

At the end of initialization:
- the referee has a valid game state;
- each player is ready to receive turn requests;
- no player has acted yet.

### Sequence Diagram

```mermaid
sequenceDiagram
    participant R as Referee
    participant P as Player

    R->>P: initialize participation
    P-->>R: ready