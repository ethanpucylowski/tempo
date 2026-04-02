package harness;

import com.google.gson.*;
import common.Cards;
import common.Cards.PebbleColor;
import common.Equation;
import common.Equations;
import common.PlayerState;
import common.TurnState;
import player.Strategy;

import java.io.InputStreamReader;
import java.util.*;

/**
 * XTurn test harness.
 *
 * Reads a Game JSON from STDIN:
 *   { "bank": *Pebbles, "visibles": *Cards, "cards": *Cards, "players": *Players }
 *
 * Outputs a Turn JSON to STDOUT:
 *   { "bank": *Pebbles, "cards": *Cards, "active": Player, "scores": *Naturals }
 *
 * The Turn is the information the referee shares with the active (first) player.
 * This is essentially TurnState extraction — not the player's chosen action.
 *
 * *Pebbles  = ["red","blue",...]       (array of pebble color strings)
 * *Cards    = [Card, ...]              where Card = { "pebbles": *Pebbles, "face?": Boolean }
 * *Players  = [Player, ...]            where Player = { "wallet": *Pebbles, "score": Natural }
 * *Naturals = [score, ...]
 */
public class XTurn {

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().create();
        JsonObject game = gson.fromJson(new InputStreamReader(System.in), JsonObject.class);

        // Parse bank
        Map<PebbleColor, Integer> bank = parsePebbles(game.getAsJsonArray("bank"));

        // Parse visible cards
        List<Cards> visibles = parseCards(game.getAsJsonArray("visibles"));

        // Parse players
        JsonArray playersJson = game.getAsJsonArray("players");
        List<PlayerState> players = new ArrayList<>();
        for (JsonElement pe : playersJson) {
            players.add(parsePlayer(pe.getAsJsonObject()));
        }

        // Active player is the first
        PlayerState active = players.get(0);

        // Other scores
        List<Integer> otherScores = new ArrayList<>();
        for (int i = 1; i < players.size(); i++) {
            otherScores.add(players.get(i).getScore());
        }

        // Build Turn output
        JsonObject turn = new JsonObject();

        // "bank"
        turn.add("bank", pebblesToJson(bank));

        // "cards" = visibles
        turn.add("cards", cardsToJson(visibles));

        // "active"
        turn.add("active", playerToJson(active));

        // "scores"
        JsonArray scores = new JsonArray();
        for (int s : otherScores) {
            scores.add(s);
        }
        turn.add("scores", scores);

        System.out.println(gson.toJson(turn));
    }

    // Parsing helpers

    static Map<PebbleColor, Integer> parsePebbles(JsonArray arr) {
        Map<PebbleColor, Integer> map = new EnumMap<>(PebbleColor.class);
        for (JsonElement e : arr) {
            PebbleColor c = PebbleColor.fromString(e.getAsString());
            map.put(c, map.getOrDefault(c, 0) + 1);
        }
        return map;
    }

    static List<Cards> parseCards(JsonArray arr) {
        List<Cards> list = new ArrayList<>();
        for (JsonElement e : arr) {
            JsonObject obj = e.getAsJsonObject();
            List<PebbleColor> pebbles = new ArrayList<>();
            for (JsonElement pe : obj.getAsJsonArray("pebbles")) {
                pebbles.add(PebbleColor.fromString(pe.getAsString()));
            }
            boolean face = obj.has("face?") && obj.get("face?").getAsBoolean();
            list.add(new Cards(pebbles, face));
        }
        return list;
    }

    static PlayerState parsePlayer(JsonObject obj) {
        Map<PebbleColor, Integer> wallet = parsePebbles(obj.getAsJsonArray("wallet"));
        int score = obj.get("score").getAsInt();
        return new PlayerState(wallet, score);
    }

    // Serialization helpers

    static JsonArray pebblesToJson(Map<PebbleColor, Integer> wallet) {
        JsonArray arr = new JsonArray();
        for (PebbleColor color : PebbleColor.values()) {
            int count = wallet.getOrDefault(color, 0);
            for (int i = 0; i < count; i++) {
                arr.add(color.toJsonString());
            }
        }
        return arr;
    }

    static JsonArray cardsToJson(List<Cards> cards) {
        JsonArray arr = new JsonArray();
        for (Cards c : cards) {
            arr.add(cardToJson(c));
        }
        return arr;
    }

    static JsonObject cardToJson(Cards c) {
        JsonObject obj = new JsonObject();
        JsonArray pebbles = new JsonArray();
        for (PebbleColor color : c.getPebbles()) {
            pebbles.add(color.toJsonString());
        }
        obj.add("pebbles", pebbles);
        obj.addProperty("face?", c.hasStar());
        return obj;
    }

    static JsonObject playerToJson(PlayerState p) {
        JsonObject obj = new JsonObject();
        obj.add("wallet", pebblesToJson(p.getWallet()));
        obj.addProperty("score", p.getScore());
        return obj;
    }
}