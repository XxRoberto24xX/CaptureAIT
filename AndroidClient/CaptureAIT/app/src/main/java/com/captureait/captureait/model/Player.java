package com.captureait.captureait.model;

/**
 * Represents a player in the system.
 */
public class Player {

    /** The name of the player. */
    public String name;

    /** The points scored by the player. */
    public String points;

    /**
     * Constructs a new Player object with a specified name and points.
     *
     * @param name The name of the player.
     * @param points The points scored by the player.
     */
    public Player(String name, String points) {
        this.name = name;
        this.points = points;
    }

    /**
     * Returns the name of the player.
     *
     * @return The name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the player.
     *
     * @param name The new name to set for the player.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the points scored by the player.
     *
     * @return The points scored by the player.
     */
    public String getPoints() {
        return points;
    }

    /**
     * Sets the points scored by the player.
     *
     * @param points The new points to set for the player.
     */
    public void setPoints(String points) {
        this.points = points;
    }
}
