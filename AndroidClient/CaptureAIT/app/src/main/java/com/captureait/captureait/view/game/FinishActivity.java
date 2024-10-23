package com.captureait.captureait.view.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.model.Game;

/**
 * Represents the activity that displays the final results of a game.
 */
public class FinishActivity extends AppCompatActivity {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private TextView txtPrimero, txtSegundo, txtTercero, pointsPrimero, pointsSegundo, pointsTercero, time;
    private ImageView btnBack;

    /** Game representing variables */
    String code;

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
        setContentView(R.layout.activity_finish);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Take the visual elements using de id
        btnBack = findViewById(R.id.btnBack);

        txtPrimero = findViewById(R.id.txtPrimero);
        txtSegundo = findViewById(R.id.txtSegundo);
        txtTercero = findViewById(R.id.txtTercero);

        pointsPrimero = findViewById(R.id.pointsPrimero);
        pointsSegundo = findViewById(R.id.pointsSegundo);
        pointsTercero = findViewById(R.id.pointsTercero);

        time = findViewById(R.id.time);

        // Get the room code for future messages
        Intent intentCaller = getIntent();
        code = intentCaller.getStringExtra("code");

        // Get the game last information before closing
        Game game = controller.getGame(code);

        // Send the finish ack to the server
        controller.sendFinishConfirmation(code);

        // Show the information of the game
        switch (game.getPlayersArrayList().size()){
            case 1:
                txtPrimero.setText(game.getPlayersArrayList().get(0).getName());
                pointsPrimero.setText(game.getPlayersArrayList().get(0).getPoints() + " pts");
                txtSegundo.setText("");
                txtSegundo.setBackgroundColor(getResources().getColor(R.color.transparent));
                pointsSegundo.setText("");
                txtTercero.setText("");
                txtTercero.setBackgroundColor(getResources().getColor(R.color.transparent));
                pointsTercero.setText("");
                break;
            case 2:
                txtPrimero.setText(game.getPlayersArrayList().get(0).getName());
                pointsPrimero.setText(game.getPlayersArrayList().get(0).getPoints() + " pts");
                txtSegundo.setText(game.getPlayersArrayList().get(1).getName());
                pointsSegundo.setText(game.getPlayersArrayList().get(1).getPoints() + " pts");
                txtTercero.setText("");
                txtTercero.setBackgroundColor(getResources().getColor(R.color.transparent));
                pointsTercero.setText("");
                break;
            case 3:
                txtPrimero.setText(game.getPlayersArrayList().get(0).getName());
                pointsPrimero.setText(game.getPlayersArrayList().get(0).getPoints() + " pts");
                txtSegundo.setText(game.getPlayersArrayList().get(1).getName());
                pointsSegundo.setText(game.getPlayersArrayList().get(1).getPoints() + " pts");
                txtTercero.setText(game.getPlayersArrayList().get(2).getName());
                pointsTercero.setText(game.getPlayersArrayList().get(2).getPoints() + " pts");
                break;
            case 4:
                txtPrimero.setText(game.getPlayersArrayList().get(0).getName());
                pointsPrimero.setText(game.getPlayersArrayList().get(0).getPoints() + " pts");
                txtSegundo.setText(game.getPlayersArrayList().get(1).getName());
                pointsSegundo.setText(game.getPlayersArrayList().get(1).getPoints() + " pts");
                txtTercero.setText(game.getPlayersArrayList().get(2).getName());
                pointsTercero.setText(game.getPlayersArrayList().get(2).getPoints() + " pts");
                break;
        }

        time.setText(game.getTime());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
