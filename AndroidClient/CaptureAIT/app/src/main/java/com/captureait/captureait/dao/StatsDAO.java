package com.captureait.captureait.dao;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * DAO class which takes the user stats information from the database and saves it as cache
 */
public class StatsDAO {
    /** Total points the player has made as he played. */
    private int totalPoints;

    /** Total time the player has been playing games. */
    private long totalTime;

    /** Total number of games the user finished. */
    private int totalFinishedGames;

    /** Total wins the player has. */
    private int totalWins;

    /** Rate of games the user has won. */
    private float victoryPercentage;

    /** Player id user to make the database interaction. */
    public String userId;

    /** FireStore database reference. */
    private FirebaseFirestore fStore;

    /**
     * Constructs a new StatsDAO object with specified attributes.
     *
     * @param userId The user ID.
     * @param callback The callback for success or error handling.
     */
    public StatsDAO(String userId, StatsCallback callback) {
        // Prepare the database reference
        fStore = FirebaseFirestore.getInstance();

        this.userId = userId;

        // Access the database
        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot docSnap = task.getResult();
                    if (docSnap.exists()) {
                        // Save the information from the document
                        totalPoints = docSnap.getLong("totalPoints").intValue();
                        totalTime = docSnap.getLong("totalTime");
                        totalFinishedGames = docSnap.getLong("totalGames").intValue();
                        totalWins = docSnap.getLong("totalWins").intValue();

                        // Calculate the win rate
                        if (totalFinishedGames == 0) {
                            victoryPercentage = 0;
                        } else {
                            victoryPercentage = ((float) totalWins / (float) totalFinishedGames) * 100;
                        }

                        // Callback successfully
                        callback.onSuccess();
                    } else {
                        callback.onError("DATABASE_ERROR");
                    }
                } else {
                    callback.onError("DATABASE_ERROR");
                }
            }
        });
    }

    /**
     * Returns the total points.
     *
     * @return The total points.
     */
    public int getTotalPoints() {
        return totalPoints;
    }

    /**
     * Returns the total time as a string.
     *
     * @return The total time as a string.
     */
    public String getTotalTime() {
        long days = totalTime / (24 * 3600);
        long resto = totalTime % (24 * 3600);
        long hours = resto / 3600;
        resto %= 3600;
        long minutes = resto / 60;
        resto %= 60;

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
        if (resto > 0 || duration.length() == 0) {
            duration.append(resto).append(" segundos");
        }

        if (duration.length() > 2 && duration.charAt(duration.length() - 2) == ',') {
            duration.setLength(duration.length() - 2);
        }

        return duration.toString();
    }

    /**
     * Returns the total number of finished games.
     *
     * @return The total finished games.
     */
    public int getTotalFinishedGames() {
        return totalFinishedGames;
    }

    /**
     * Returns the total number of wins.
     *
     * @return The total wins.
     */
    public int getTotalWins() {
        return totalWins;
    }

    /**
     * Returns the victory percentage.
     *
     * @return The victory percentage.
     */
    public float getVictoryPercentage() {
        return victoryPercentage;
    }

    /**
     * Updates the stats with new game data.
     *
     * @param points The points scored in the game.
     * @param time The time taken in the game.
     * @param win Whether the game was won.
     * @param callback The callback for success or error handling.
     */
    public void updateStats(int points, long time, boolean win, StatsCallback callback) {
        // First update the local information
        totalPoints += points;
        totalTime += time;
        totalFinishedGames += 1;
        if (win) {
            totalWins += 1;
        }

        DocumentReference docRef = fStore.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("totalPoints", this.totalPoints);
        updates.put("totalTime", this.totalTime);
        updates.put("totalGames", this.totalFinishedGames);
        updates.put("totalWins", this.totalWins);

        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Calculate the win rate
                    victoryPercentage = ((float) totalWins / (float) totalFinishedGames) * 100;

                    // Callback Successfully
                    callback.onSuccess();
                } else {
                    // Undo the changes
                    totalPoints -= points;
                    totalTime -= time;
                    totalFinishedGames -= 1;
                    if (win) {
                        totalWins -= 1;
                    }

                    callback.onError("DATABASE_ERROR");
                }
            }
        });
    }
}
