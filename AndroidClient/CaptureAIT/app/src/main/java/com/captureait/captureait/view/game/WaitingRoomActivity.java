package com.captureait.captureait.view.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Makes the user wait until the game is ready to start, it also allows the player to share the code and invite a friend to the game.
 */
public class WaitingRoomActivity extends AppCompatActivity implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private TextView txtCode;
    private FloatingActionButton btnShare,btnAddFriend;

    /** Game representing variables */
    private String code;
    private boolean onGame = false;

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
        setContentView(R.layout.activity_waiting_room);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);

        // Take the visual elements using de id
        txtCode = findViewById(R.id.txtCode);
        btnShare = findViewById(R.id.btnShare);
        btnAddFriend = findViewById(R.id.btnAddFriend);

        // Take the code of the game you are treating with
        Intent intentCaller = getIntent();
        code = intentCaller.getStringExtra("code");

        // Set the code in the screen
        txtCode.setText(code);

        // Set the button action
        /* Uses the own phone features to share the code of the game using any social media */
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "Unete a mi sala de codigo: " + code.toString());
                intent.setType("text/plain");
                startActivity(intent);
            }
        });

        /* Sends you to a new view where you can invite a friends to the room you are waiting for */
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inviteFriendActivity = new Intent(WaitingRoomActivity.this, InviteFriendActivity.class);
                inviteFriendActivity.putExtra("code", code);
                startActivity(inviteFriendActivity);
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
        if(updatedElement.equals(code)){
            // Set the flag to true
            onGame = true;

            // The game starts, move to the next activity
            Intent gameActivityIntent  = new Intent(WaitingRoomActivity.this, GameActivity.class);
            gameActivityIntent.putExtra("code", code);
            startActivity(gameActivityIntent);
            finish();
        }
    }

    /**
     * Called when the view is been destroyed, removes the listener from the controller.
     */
    @Override
    protected void onDestroy() {
        if(!onGame){
            controller.leaveWaitingRoom(code);
        }
        controller.removeActiveViewListener(this);
        super.onDestroy();
    }
}
