package com.captureait.captureait.view.userData;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Shows the user his stats gathered as he played
 */
public class StatsActivity extends AppCompatActivity implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private ImageView btnBack;
    private TextView txtuserName, totalPoints, totalGamesPlayed, totalWins, winRate, totalTime;
    private CircleImageView userImg;

    /**
     * Called when the activity is first created. Initializes the activity, including setting up visual elements,
     * registering click listeners, and initializing necessary controllers.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);

        // Take the visual elements using their id
        btnBack = findViewById(R.id.btnBack);

        userImg = findViewById(R.id.userImagen);

        txtuserName = findViewById(R.id.userNombre);
        totalPoints = findViewById(R.id.totalPoints);
        totalGamesPlayed = findViewById(R.id.totalGamesPlayed);
        totalWins = findViewById(R.id.totalWins);
        winRate = findViewById(R.id.winRate);
        totalTime = findViewById(R.id.totalTime);

        // Setting the user information in the screen
        txtuserName.setText(controller.getUserName());
        totalPoints.setText("Puntos Totales: " + controller.getTotalPoints());
        totalGamesPlayed.setText("Partidas Jugadas: " + controller.getTotalFinishedGames());
        totalWins.setText("Partidas Ganadas: " + controller.getTotalWins());
        winRate.setText("Porcentaje de Victoria: " + controller.getVictoryPercentage() + "%");
        totalTime.setText(controller.getTotalTime());

        // Put also the Photo
        if(controller.getUserPhoto() != null){
            Glide.with(this).load(controller.getUserPhoto()).into(userImg);
        }

        // Set the button behavior
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Called when the initialization has finished and you are a listener of the event.
     * This method is implemented from the ActiveViewListener interface.
     * @param info Information regarding initialization
     */
    @Override
    public void onInitializationFinished(String info) {

    }

    /**
     * Called when there was an update in the model and you are a listener of the event.
     * This method is implemented from the ActiveViewListener interface.
     * @param updatedElement The element that was updated
     */
    @Override
    public void onUpdate(String updatedElement) {
        if(updatedElement.equals("stats_changed")){
            totalPoints.setText("Puntos Totales: " + controller.getTotalPoints());
            totalGamesPlayed.setText("Partidas Jugadas: " + controller.getTotalFinishedGames());
            totalWins.setText("Partidas Ganadas: " + controller.getTotalWins());
            winRate.setText("Porcentaje de Victoria: " + controller.getTotalWins());
        }
    }

    /**
     * Called when the view is been destroyed, removes the listener from the controller.
     */
    @Override
    protected void onDestroy() {
        controller.removeActiveViewListener(this);
        super.onDestroy();
    }
}
