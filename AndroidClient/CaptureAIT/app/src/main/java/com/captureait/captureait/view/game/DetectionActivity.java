package com.captureait.captureait.view.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.model.Game;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;

/**
 * Shows the user the results of the photo hw sent.
 */
public class DetectionActivity extends AppCompatActivity {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private TextView txtDetected;
    private ImageView imgView, btnBack;

    /** Game representing variables */
    private String code;
    private String codifiedPhoto;
    private boolean finish = false;

    /** Activity events */
    private final String OFF_DETECTING = "OFF_DETECTING";

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
        setContentView(R.layout.activity_detection);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Take the code of the game you are treating with
        Intent intentCaller = getIntent();
        code = intentCaller.getStringExtra("code");

        // There was an update in the game
        Game game = controller.getGame(code);

        // See if the game has finished
        if(game.isFinish()){
            finish = true;
        }

        // Take the visual elements using the id
        txtDetected = findViewById(R.id.txtDetected);
        imgView = findViewById(R.id.imgView);
        btnBack = findViewById(R.id.btnBack);

        // Take the photo from cache and transform it to an Image again to show it
        SharedPreferences pref = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        codifiedPhoto = pref.getString("photo", "");

        byte[] bytes = Base64.getDecoder().decode(codifiedPhoto);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true; // Optional for performance optimization
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, dpToPx(350), dpToPx(520), true);

        imgView.setImageBitmap(scaledBitmap);

        // Get the last detection made
        ArrayList<String> detectionsList = controller.getDetectionsList();
        ArrayList<String> positionList = controller.getPositionsList();

        // See if the detection was empty
        if(detectionsList == null){
            // Just inform the user there were no relevant detections
            txtDetected.setText("No se encontro ningun objeto nuevo o relevante");
        }else{
            // Show the detected elements in the screen
            String display = "";

            for(int i=0; i<detectionsList.size(); i++){
                display = display + detectionsList.get(i) + ": ";
                display = display + positionList.get(i) + "º\n";
            }

            txtDetected.setText(display);
        }

        // Set the button behavior
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(finish){
                    // Go to the Finish Game activity
                    Intent moveIntent  = new Intent(DetectionActivity.this, FinishActivity.class);
                    moveIntent.putExtra("code", code);
                    startActivity(moveIntent);
                    finish();
                }
                finish();
            }
        });
    }

    /**
     * Converts dp to pixel.
     * @param dp The value in dp to convert.
     * @return The corresponding value in pixels.
     */
    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Called when the back button is pressed, moves to finish activity if the game is finished.
     */
    @Override
    public void onBackPressed() {
        if(finish){
            // Go to the Finish Game activity
            Intent moveIntent  = new Intent(DetectionActivity.this, FinishActivity.class);
            moveIntent.putExtra("code", code);
            startActivity(moveIntent);
            finish();
        }
        super.onBackPressed();
    }

    /**
     * Called when the view is been destroyed, removes the listener from the controller.
     */
    @Override
    protected void onDestroy() {
        // Inform the gameActivity you are not active
        Intent intent = new Intent(OFF_DETECTING);
        sendBroadcast(intent);
        super.onDestroy();
    }
}
