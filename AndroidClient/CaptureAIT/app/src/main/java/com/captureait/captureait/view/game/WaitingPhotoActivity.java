package com.captureait.captureait.view.game;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;

/**
 * Leaves the user waiting while the photo is processing.
 */
public class WaitingPhotoActivity extends AppCompatActivity implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** Game representing variables */
    private String code;

    /** Activity events */
    private final String ON_DETECTING = "ON_DETECTING";

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
        setContentView(R.layout.activity_waiting_photo);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);

        // Inform the gameActivity you are active
        Intent intent = new Intent(ON_DETECTING);
        sendBroadcast(intent);

        // Take the code of the game you are treating with
        Intent intentCaller = getIntent();
        code = intentCaller.getStringExtra("code");

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
        if(updatedElement.equals(code)){
            // There was an update in the game, we need to know if the game has ended to finish the activity immediately
            if(controller.getGame(code).isFinish()){
                finish();
            }
        }else if(updatedElement.equals("detection")){
            // There was a response form the server and we need to show the detected element
            Intent intent  = new Intent(WaitingPhotoActivity.this, DetectionActivity.class);
            intent.putExtra("code", code);
            startActivity(intent);
            finish();
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
