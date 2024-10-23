package com.captureait.captureait.model;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Represents and manages information about a game.
 */
public class Game {

    /** The unique code identifying the game. */
    private String code;

    /** The list of players participating in the game. */
    private ArrayList<Player> playersArrayList;

    /** Indicates if the game has finished. */
    private boolean finish;

    /** Array of detections related to the game. */
    private String[] detections;

    /** The total duration of the game in seconds. */
    private long seconds = 0;

    /**
     * Constructs a new Game object with specified attributes.
     *
     * @param code The unique code identifying the game.
     * @param playersArrayList The list of players participating in the game.
     * @param finish Indicates if the game has finished.
     * @param detections Array of detections related to the game.
     */
    public Game(String code, ArrayList<Player> playersArrayList, boolean finish, String[] detections) {
        this.code = code;
        this.playersArrayList = playersArrayList;
        this.finish = finish;
        this.detections = detections;
        resort();
    }

    /**
     * Converts the total seconds the game lasted into a string.
     *
     * @return A string representing the duration of the game.
     */
    public String getTime(){
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder duration = new StringBuilder();

        if (days > 0) {
            duration.append(days).append(" días, ");
        }
        if (hours > 0) {
            duration.append(hours).append(" horas, ");
        }
        if (minutes > 0) {
            duration.append(minutes).append(" minutos, ");
        }
        if (seconds > 0 || duration.length() == 0) {
            duration.append(seconds).append(" segundos");
        }

        if (duration.length() > 2 && duration.charAt(duration.length() - 2) == ',') {
            duration.setLength(duration.length() - 2);
        }

        return duration.toString();
    }

    /**
     * Returns the unique code identifying the game.
     *
     * @return The code identifying the game.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the unique code identifying the game.
     *
     * @param code The new code identifying the game.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Checks if the game has finished.
     *
     * @return True if the game has finished, false otherwise.
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * Sets whether the game has finished and updates the duration of the game.
     *
     * @param finish True if the game has finished, false otherwise.
     * @param time The total duration of the game in seconds.
     */
    public void setFinish(boolean finish, long time) {
        this.finish = finish;
        this.seconds = time;
    }

    /**
     * Returns the array of detections related to the game.
     *
     * @return The array of detections.
     */
    public String[] getDetections() {
        return detections;
    }

    /**
     * Returns the list of players participating in the game.
     *
     * @return The list of players.
     */
    public ArrayList<Player> getPlayersArrayList() {
        return playersArrayList;
    }

    /**
     * Checks if a player's name has changed and updates it if found.
     *
     * @param newUserName The new name of the player.
     * @param oldUserName The old name of the player to be replaced.
     * @return True if the player's name was successfully updated, false otherwise.
     */
    public boolean nameHasChange(String newUserName, String oldUserName){
        for(int i=0; i<playersArrayList.size(); i++){
            if(oldUserName.equals(playersArrayList.get(i).getName())){
                playersArrayList.get(i).setName(newUserName);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates a player's points in the game.
     *
     * @param userName The name of the player whose points are to be updated.
     * @param points The new points of the player.
     */
    public void updateGame(String userName, String points){
        for(int i=0; i<playersArrayList.size(); i++){
            if(userName.equals(playersArrayList.get(i).getName())){
                playersArrayList.get(i).setPoints(points);
                resort();
                break;
            }
        }
    }

    /**
     * Deletes a player from the game.
     *
     * @param userName The name of the player to be deleted.
     */
    public void deletePlayer(String userName){
        for(int i=0; i<playersArrayList.size(); i++){
            if(userName.equals(playersArrayList.get(i).getName())){
                playersArrayList.remove(i);
                break;
            }
        }
    }

    /**
     * Resorts the list of players based on their points in descending order.
     */
    public void resort(){
        this.playersArrayList.sort(pointsComparator);
    }

    /**
     * Comparator for sorting players by their points in descending order.
     */
    Comparator<Player> pointsComparator = new Comparator<Player>() {
        @Override
        public int compare(Player player1, Player player2) {
            return Integer.compare(Integer.parseInt(player1.points), Integer.parseInt(player2.points)) * -1;
        }
    };
}
