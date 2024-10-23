package com.captureait.captureait.model;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * Represents a user in the system.
 */
public class User {

    /** The Firebase user associated with this application user. */
    FirebaseUser firebaseUser;

    /** The unique ID of the user. */
    String id;

    /** The email address of the user. */
    String email;

    /** The authentication provider (e.g., Google, Facebook) of the user. */
    String provider;

    /** The username chosen by the user. */
    String userName;

    /** The list of friends associated with the user. */
    private ArrayList<String> friends = new ArrayList<>();

    /**
     * Constructs a new User object.
     *
     * @param firebaseUser The Firebase user object.
     * @param id The unique ID of the user.
     * @param email The email address of the user.
     * @param provider The authentication provider of the user.
     * @param userName The username chosen by the user.
     * @param friends The initial list of friends associated with the user.
     */
    public User(FirebaseUser firebaseUser, String id, String email, String provider, String userName, ArrayList<String> friends) {
        this.firebaseUser = firebaseUser;
        this.id = id;
        this.email = email;
        this.provider = provider;
        this.userName = userName;
        this.friends = friends;
    }

    /**
     * Adds a friend to the user's list of friends.
     *
     * @param friend The username of the friend to be added.
     */
    public void addFriend(String friend){
        synchronized (friends){
            friends.add(friend);
        }
    }

    /**
     * Removes a friend from the user's list of friends.
     *
     * @param friend The username of the friend to be removed.
     */
    public void removeFriend(String friend){
        synchronized (friends){
            friends.remove(friend);
        }
    }

    /**
     * Returns the Firebase user associated with this User object.
     *
     * @return The Firebase user object.
     */
    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    /**
     * Returns the unique ID of the user.
     *
     * @return The unique ID of the user.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the email address of the user.
     *
     * @return The email address of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the authentication provider of the user.
     *
     * @return The authentication provider of the user.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the username chosen by the user.
     *
     * @return The username chosen by the user.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the list of friends associated with the user.
     *
     * @return The list of friends associated with the user.
     */
    public ArrayList<String> getFriends() {
        synchronized (friends){
            return friends;
        }
    }

    /**
     * Sets the username chosen by the user.
     *
     * @param userName The new username to be set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
