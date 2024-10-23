package com.captureait.captureait.view.game;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.model.InviteFriendCustomAdapter;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Allows the user to invite a friend to the game he is waiting.
 */
public class InviteFriendActivity extends AppCompatActivity implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private ImageView btnBack;
    private RecyclerView recyclerView;
    private TextInputEditText labelName;

    /** Game representing variables */
    private String code;

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
        setContentView(R.layout.activity_invite_friend);

        // Take the references of the visual elements
        btnBack = findViewById(R.id.btnBack);
        labelName = findViewById(R.id.labelName);
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);

        // Take the code from the intent
        Intent intentCaller = getIntent();
        code = intentCaller.getStringExtra("code");

        // Show the list of friends in the screen
        showFriends();

        // Set the button actions
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Finish the activity and goes to the home activity which stills in the background
                finish();
            }
        });

        // Set the search bar interaction
        labelName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                runOnUiThread(()->{
                    showFriends();
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    // Makes the adapter to show the friends list in the model taking into account the search bar con
    public void showFriends(){
        // Take the input string
        String search = labelName.getText().toString();

        // Create the adapter
        InviteFriendCustomAdapter customAdapter = new InviteFriendCustomAdapter(getApplicationContext(), controller.searchFriends(search), new InviteFriendCustomAdapter.InviteFriendInteractionListener() {
            @Override
            public void onInviteFriend(String invitedUserName) {
                controller.sendGameInvitation(invitedUserName, code);
            }
        });
        recyclerView.setAdapter(customAdapter);
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
            // This happens when you are inviting a friend but the game has just tarted
            finish();
        }else if(updatedElement.equals("list_friends_changed")){
            // Happens when a friend deletes you while you are int this view
            runOnUiThread(()->{
                showFriends();
            });
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
