package com.captureait.captureait.view.game;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Allows the player to make a new game or join to an existing one knowing the code.
 */
public class NewGameActivity extends AppCompatActivity{

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private Button btnJoin, btnNew;
    private ImageView  btnBack;
    private TextInputEditText labelCode,labelPlayers;

    /** Working progressbar */
    private ProgressDialog mProgressBar;

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
        setContentView(R.layout.activity_new_game);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Take the visual elements using de id
        btnJoin = findViewById(R.id.btnJoin);
        btnNew = findViewById(R.id.btnNew);
        btnBack = findViewById(R.id.btnBack);

        labelPlayers = findViewById(R.id.labelPlayers);
        labelCode = findViewById(R.id.labelCode);

        // We create the progress bar before showing it
        mProgressBar = new ProgressDialog(NewGameActivity.this);

        // Setting the button actions
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Take the number of players
                String numPlayers = labelPlayers.getText().toString();

                if(numPlayers.isEmpty()){
                    labelPlayers.setError("El numero de jugadores debe estar entre 2 y 4");
                }else if(Integer.parseInt(numPlayers)<2 || Integer.parseInt(numPlayers)>4){
                    labelPlayers.setError("El numero de jugadores debe estar entre 2 y 4");
                }else{
                    // Show the progressBar
                    mProgressBar.setTitle("New Game");
                    mProgressBar.setMessage("creando nueva sala");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();

                    // We need to tell the server that we want a new room and give him the information needed
                    controller.createRoom(numPlayers, new CentralControllerCallBack() {
                        @Override
                        public void onSuccess(String info) {
                            mProgressBar.dismiss();

                            // Move to the next activity and pass the code to know which game is representing
                            Intent waitingRoomIntent  = new Intent(NewGameActivity.this, WaitingRoomActivity.class);
                            waitingRoomIntent.putExtra("code", info);
                            startActivity(waitingRoomIntent);
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            mProgressBar.dismiss();

                            // Inform the user about the error
                            alertDialog("Room creation", "Error al crear la sala de juego, intentelo de nuevo mas tarde", "Aceptar");

                            // Put the error in the log
                            Log.e("NewGameActivity", "Unable to create the room");

                        }
                    });
                }
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Take the information form the label
                String code = labelCode.getText().toString().toUpperCase();

                if(code.length()!=4){
                    labelCode.setError("El codigo deben ser 4 letras");
                }else{
                    // Show the progressBar
                    mProgressBar.setTitle("Join Game");
                    mProgressBar.setMessage("unindose a nueva sala");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();

                    // We need to tell tell the server the room we want to join to
                    controller.joinRoom(code, new CentralControllerCallBack() {
                        @Override
                        public void onSuccess(String info) {
                            mProgressBar.dismiss();

                            if(info.equals("GAME_JOIN_ACHIEVED_WAIT")){
                                // Move to the next activity and pass the code to know which game is representing
                                Intent waitingRoomIntent  = new Intent(NewGameActivity.this, WaitingRoomActivity.class);
                                waitingRoomIntent.putExtra("code", code);
                                startActivity(waitingRoomIntent);
                                finish();
                            }else if(info.equals("GAME_JOIN_ACHIEVED_READY")){
                                // Move to the next activity and pass the code to know which game is representing
                                Intent gameActivityIntent  = new Intent(NewGameActivity.this, GameActivity.class);
                                gameActivityIntent.putExtra("code", code);
                                startActivity(gameActivityIntent);
                                finish();
                            }else{
                                // Inform the user about the error
                                runOnUiThread(()->{
                                    alertDialog("Room joining", "La partida no existe o ya ha comenzado", "Aceptar");
                                });

                                // Put the error in the log
                                Log.e("MessageActivity", "Unable to join the room");
                            }
                        }

                        @Override
                        public void onError(String error) {
                            mProgressBar.dismiss();

                            // Inform the user about the error
                            runOnUiThread(()->{
                                alertDialog("Room joining", "Error al unirse a la sala de juego, intentelo de nuevo mas tarde", "Aceptar");
                            });

                            // Put the error in the log
                            Log.e("NewGameActivity", "Unable to join the room");
                        }
                    });
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Displays an alert dialog with the given title, message, and button text.
     * @param title Title of the dialog
     * @param message Message to display in the dialog
     * @param button Text for the button in the dialog
     */
    public void alertDialog(String title, String message, String button){
        // Building the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create the content of the AlertDialog
        builder.setTitle(title)
                .setMessage(message)

                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Closes the dialog
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
    }
}
