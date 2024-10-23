package com.captureait.captureait.view.game;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.model.Game;
import com.captureait.captureait.model.PlayerCustomAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Represents an activity displaying game information and allowing users to take photos to score.
 */
public class GameActivity extends AppCompatActivity implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private TextView txtCode, txtElements1, txtElements2, txtElements3, txtElements4;
    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd, btnAbandom;
    private ImageView btnBack;

    /** Game representing variables */
    private String code;
    private boolean onDetection = false;

    /** Permissions needed */
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;

    private static final int REQUEST_IMAGE_CAPTURE = 2;

    /** Activity events */
    private final String ON_DETECTING = "ON_DETECTING";
    private final String OFF_DETECTING = "OFF_DETECTING";

    /** Receiver for event between activities */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ON_DETECTING)){
                // Indicate detection event
                Log.w("informo", "en el evento de deteccion");
                onDetection = true;
            }else if(intent.getAction().equals(OFF_DETECTING)){
                // Indicate detection event end
                Log.w("informo", "fuera del evento de deteccion");
                onDetection = false;
            }
        }
    };

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
        setContentView(R.layout.activity_game);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);

        // Take the code of the game you are treating with
        Intent intentCaller = getIntent();
        code = intentCaller.getStringExtra("code");

        // Take the visual elements using de id
        btnBack = findViewById(R.id.btnBack);

        txtCode = findViewById(R.id.txtCode);
        txtElements1 = findViewById(R.id.txtElements1);
        txtElements2 = findViewById(R.id.txtElements2);
        txtElements3 = findViewById(R.id.txtElements3);
        txtElements4 = findViewById(R.id.txtElements4);

        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAdd = findViewById(R.id.btnAdd);
        btnAbandom = findViewById(R.id.btnAbandom);

        // Set the listener for the broadcast events between activities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, new IntentFilter(ON_DETECTING), Context.RECEIVER_EXPORTED);
            registerReceiver(broadcastReceiver, new IntentFilter(OFF_DETECTING), Context.RECEIVER_EXPORTED);
        }else {
            registerReceiver(broadcastReceiver, new IntentFilter(ON_DETECTING));
            registerReceiver(broadcastReceiver, new IntentFilter(OFF_DETECTING));
        }

        // Display the Code
        txtCode.setText(code);

        // Display the game information
        Game game = controller.getGame(code);

        txtElements1.setText(game.getDetections()[0]);
        txtElements2.setText(game.getDetections()[1]);
        txtElements3.setText(game.getDetections()[2]);
        txtElements4.setText(game.getDetections()[3]);

        PlayerCustomAdapter customAdapter = new PlayerCustomAdapter(this, game.getPlayersArrayList());
        recyclerView.setAdapter(customAdapter);

        // Set the button actions
        btnAbandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make the user confirm the action
                abandomGameAlertDialog("Abandonar Partida", "Esta seguro que desea abandonar la partida permanentemente", "Aceptar", "Cancelar");
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
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
     * Initiates the process to capture an image using the device's camera.
     */
    public void captureImage(){
        // See if we have the permission
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            // We have the permission to take pictures open the camera app
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

        }else{
            // We don´t have the permission to take pictures so we request them
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(resultCode, resultCode, data);

        // See if the photo has been traken
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            // Everything went ok so we take the captured image
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");

            // To be able to pass it through an http request it needs to be a String so we codify it in Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            String codifiedPhoto = Base64.getEncoder().encodeToString(bytes);

            // Save it in cache
            SharedPreferences pref = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("photo", codifiedPhoto);
            editor.commit();

            // Go to the next activity
            Intent waitingPhotoActivity  = new Intent(GameActivity.this, WaitingPhotoActivity.class);
            waitingPhotoActivity.putExtra("code", code);
            startActivity(waitingPhotoActivity);

            // Send the photo to the server
            controller.sendPhoto(codifiedPhoto, code);
        }
    }

    /**
     * Displays an AlertDialog confirming the user's intention to abandon the game.
     * @param title Title of the AlertDialog
     * @param message Message of the AlertDialog
     * @param accept Label for the positive button
     * @param deny Label for the negative button
     */
    public void abandomGameAlertDialog(String title, String message, String accept, String deny){
        // Building the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create the content of the AlertDialog
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Closes the dialog
                        dialogInterface.cancel();

                        // Abandon the game
                        controller.abandonGame(code);

                        // Finish this activity
                        finish();
                    }
                })
                .setNegativeButton(deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Closes the dialog
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
        if(updatedElement.equals(code)){
            // There was an update in the game
            Game game = controller.getGame(code);

            // See if the game has finished
            if(game.isFinish()){
                // See if the user is on detection activity
                if(!onDetection){
                    Log.w("gameActivity", "llamo de finishActivity");
                    // Go to the Finish Game activity
                    Intent moveIntent  = new Intent(GameActivity.this, FinishActivity.class);
                    moveIntent.putExtra("code", code);
                    startActivity(moveIntent);
                    finish();
                }else{
                    finish();
                }
            }else{
                // This should be done by the userInterfaceThread as it has to change it
                runOnUiThread(()->{
                    // Set the new information in display because there was an update
                    PlayerCustomAdapter customAdapter = new PlayerCustomAdapter(this, game.getPlayersArrayList());
                    recyclerView.setAdapter(customAdapter);
                });
            }
        }
    }

    /**
     * Called when the view is been destroyed, removes the listener from the controller.
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        controller.removeActiveViewListener(this);
        super.onDestroy();
    }
}
