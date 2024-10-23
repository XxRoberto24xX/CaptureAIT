package com.captureait.captureait.dao;

import android.util.Log;

import com.captureait.captureait.model.Game;
import com.captureait.captureait.model.Player;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class GamesDAO {

    /** The list of games the player participates in. */
    private ArrayList<Game> gamesArrayList;

    /**
     * Constructs a new GamesDAO object with specified attributes.
     *
     * @param codes JSON array of game codes.
     * @param names JSON array of player names.
     * @param points JSON array of player points.
     * @param finishes JSON array of game finish statuses.
     * @param detections JSON array of game detections.
     * @throws JSONException If there is an error parsing the JSON.
     */
    public GamesDAO(JSONArray codes, JSONArray names, JSONArray points, JSONArray finishes, JSONArray detections) throws JSONException {
        // Create the ArrayList instance
        gamesArrayList = new ArrayList<>();

        // Fill the Games arrayList
        for (int i = 0; i < codes.length(); i++) {
            // Prepare the players list
            ArrayList<Player> playersArrayList = new ArrayList<>();
            for (int j = 0; j < names.getJSONArray(i).length(); j++) {
                playersArrayList.add(new Player(names.getJSONArray(i).getString(j), points.getJSONArray(i).getString(j)));
            }

            // Prepare the detections
            String[] thisDetections = new String[4];
            for (int j = 0; j < detections.getJSONArray(i).length(); j++) {
                thisDetections[j] = detections.getJSONArray(i).getString(j);
            }

            // Make the new game and add it to the list
            gamesArrayList.add(new Game(codes.getString(i), playersArrayList, finishes.getBoolean(i), thisDetections));
        }
    }

    /**
     * Changes the name of a player of the game.
     *
     * @param newUserName The new user name.
     * @param oldUserName The old user name.
     * @return List of game codes where the name has changed.
     */
    public ArrayList<String> nameHasChange(String newUserName, String oldUserName) {
        ArrayList<String> codesChanged = new ArrayList<>();
        synchronized (gamesArrayList) {
            for (int i = 0; i < gamesArrayList.size(); i++) {
                if (gamesArrayList.get(i).nameHasChange(newUserName, oldUserName)) {
                    codesChanged.add(gamesArrayList.get(i).getCode());
                }
            }
        }
        return codesChanged;
    }

    /**
     * Updates the selected game information with the data given.
     *
     * @param code The game code.
     * @param userName The user name.
     * @param points The points of the user.
     * @param finish The finish status of the game.
     * @param time The time of the update.
     */
    public void updateGame(String code, String userName, String points, boolean finish, long time) {
        synchronized (gamesArrayList) {
            for (int i = 0; i < gamesArrayList.size(); i++) {
                if (gamesArrayList.get(i).getCode().equals(code)) {
                    gamesArrayList.get(i).setFinish(finish, time);
                    gamesArrayList.get(i).updateGame(userName, points);
                    break;
                }
            }
        }
    }

    /**
     * Deletes a user from the game.
     *
     * @param code The game code.
     * @param userName The user name.
     * @param finish The finish status of the game.
     * @param time The time of the update.
     */
    public void deletePlayer(String code, String userName, boolean finish, long time) {
        synchronized (gamesArrayList) {
            for (int i = 0; i < gamesArrayList.size(); i++) {
                if (code.equals(gamesArrayList.get(i).getCode())) {
                    gamesArrayList.get(i).setFinish(finish, time);
                    gamesArrayList.get(i).deletePlayer(userName);
                    break;
                }
            }
        }
    }

    /**
     * Creates a new blank game.
     *
     * @param code The game code.
     * @param names The names of the players.
     * @param detections The detections of the game.
     * @throws JSONException If there is an error parsing the JSON.
     */
    public void newBlankGame(String code, JSONArray names, JSONArray detections) throws JSONException {
        ArrayList<Player> playersArrayList = new ArrayList<>();
        for (int i = 0; i < names.length(); i++) {
            playersArrayList.add(new Player(names.getString(i), "0"));
        }

        // Prepare the detections
        String[] thisDetections = new String[4];
        for (int j = 0; j < detections.length(); j++) {
            thisDetections[j] = detections.getString(j);
        }

        synchronized (gamesArrayList) {
            gamesArrayList.add(new Game(code, playersArrayList, false, thisDetections));
        }
    }

    /**
     * Returns the information of one game.
     *
     * @param code The game code.
     * @return The game object, or null if not found.
     */
    public Game getGame(String code) {
        synchronized (gamesArrayList) {
            for (Game g : gamesArrayList) {
                if (g.getCode().equals(code)) {
                    g.resort();
                    return g;
                }
            }
            return null;
        }
    }

    /**
     * Deletes a game from the list.
     *
     * @param code The game code.
     */
    public void deleteGame(String code) {
        synchronized (gamesArrayList) {
            for (int i = 0; i < gamesArrayList.size(); i++) {
                if (gamesArrayList.get(i).getCode().equals(code)) {
                    gamesArrayList.remove(i);
                    return;
                }
            }
        }
    }

    /**
     * Returns the list of games.
     *
     * @return The games array list.
     */
    public ArrayList<Game> getGamesArrayList() {
        synchronized (gamesArrayList) {
            return gamesArrayList;
        }
    }
}
