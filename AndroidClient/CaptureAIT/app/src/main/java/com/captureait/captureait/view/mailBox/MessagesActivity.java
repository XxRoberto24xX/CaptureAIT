package com.captureait.captureait.view.mailBox;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.captureait.captureait.model.MessageCustomAdapter;
import com.captureait.captureait.view.game.GameActivity;
import com.captureait.captureait.view.game.WaitingRoomActivity;

/**
 * Allows the user to see the messages he has in his mailbox and accept their proposals or delete them.
 */
public class MessagesActivity extends AppCompatActivity implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private RecyclerView recyclerView;
    private ImageView btnBack;

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
        setContentView(R.layout.activity_messages);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);

        // Take the visual elements using their id
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        btnBack = findViewById(R.id.btnBack);

        // Display the messages in the screen
        showMessages();

        // Set the button behavior
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Makes a new adapter to show the messages in the model
     */
    public void showMessages(){
        MessageCustomAdapter customAdapter = new MessageCustomAdapter(getApplicationContext(), controller.getMessages(), new MessageCustomAdapter.MessageInteractionListener() {
            @Override
            public void onMessageDelete(String type, String userName, String code) {
                /* Delete the message */
                controller.deleteMessage(type, userName, code);
            }

            @Override
            public void onFriendRequestAccepted(String type, String userName, String code) {
                /* Accept the friend request */
                controller.acceptFriendRequest(userName, new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {
                        /* Once accepted, delete it */
                        controller.deleteMessage(type, userName, code);

                        /* Inform the user about the new friend */
                        Toast.makeText(getApplicationContext(), userName + " y tu sois amigos ahora", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String error) {
                        /* Inform the user about the error */
                        alertDialog("Accept request", "Error al aceptar la soclicitud de amistad", "Aceptar");

                        /* Put the error in the log */
                        Log.e("MessageActivity", "DATABASE_ERROR");
                    }
                });
            }

            @Override
            public void onGameInvitationAccepted(String type, String userName, String code) {
                controller.joinRoom(code, new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {
                        /* Once the joining was successful delete the message */
                        controller.deleteMessage(type, userName, code);

                        /* See what to do depending the success state */
                        if(info.equals("GAME_JOIN_ACHIEVED_WAIT")){
                            /* Move to the next activity and pass the code*/
                            Intent waitingRoomIntent  = new Intent(MessagesActivity.this, WaitingRoomActivity.class);
                            waitingRoomIntent.putExtra("code", code);
                            startActivity(waitingRoomIntent);
                            finish();
                        }else if(info.equals("GAME_JOIN_ACHIEVED_READY")){
                            /* Move to the next activity and pass the code*/
                            Intent gameIntent  = new Intent(MessagesActivity.this, GameActivity.class);
                            gameIntent.putExtra("code", code);
                            startActivity(gameIntent);
                            finish();
                        }else{
                            /* Inform the user about the error */
                            alertDialog("Room joining", "La partida no existe o ya ha comenzado", "Aceptar");

                            /* Put the error in the log */
                            Log.e("MessageActivity", "Unable to join the room");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        /* Inform the user about the error */
                        alertDialog("Room joining", "Error al unirse a la sala de juego, intentelo de nuevo mas tarde", "Aceptar");

                        /* Put the error in the log */
                        Log.e("MessageActivity", "Unable to join the room");
                    }
                });
            }
        });
        recyclerView.setAdapter(customAdapter);
    }

    /**
     * Displays an alert dialog with the given title, message, and button text.
     * @param title Title of the dialog
     * @param message Message to display in the dialog
     * @param button Text for the button in the dialog
     */
    public void alertDialog(String title, String message, String button){
        /* Building the AlertDialog */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        /* Create the content of the AlertDialog */
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /* Closes the dialog */
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
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
        if(updatedElement.equals("list_messages_changed")){
            runOnUiThread(()->{
                showMessages();
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
