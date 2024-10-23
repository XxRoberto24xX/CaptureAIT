package com.captureait.captureait.view.authentication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.captureait.captureait.view.mainMenu.HomeActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Allows the user to select a username when he registers using a google account.
 */
public class NameSelectionActivity extends AppCompatActivity{

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private TextInputEditText labelName;
    private Button btnContinue;

    /** Activity events */
    private final String USER_READY = "USER_READY";

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
        setContentView(R.layout.activity_name_selection);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Take the visual elements using de id
        labelName = findViewById(R.id.labelName);
        btnContinue = findViewById(R.id.btnContinue);

        // Setting click listeners
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Take the user input
                String userName = labelName.getText().toString();

                if(userName.isEmpty() || userName.length() < 5 || userName.length() > 15){
                    labelName.setError("El nombre de usuario debe tener almenos 5 caracteres y maximo 15");
                }else{
                    controller.setUserName(userName, new CentralControllerCallBack() {
                        @Override
                        public void onSuccess(String info) {

                            if(info.equals("USER_READY")){
                                // Inform the rest of the activities the player is ready
                                Intent intent = new Intent(USER_READY);
                                sendBroadcast(intent);

                                // Move to the home activity
                                Intent homeActivityIntent  = new Intent(NameSelectionActivity.this, HomeActivity.class);
                                startActivity(homeActivityIntent);
                                finish();
                            }else{
                                // Inform the user about the error trying to authenticate with google
                                alertDialog("Error", "Error al acceder a la base de datos", "Aceptar");

                                // Put the error in the log
                                Log.e("NameSelectionActivity", "Error accessing the database");
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if(error.equals("NAME_IN_USE")){
                                // Inform the user the name is in use
                                labelName.setError("Nombre de usuario en uso, prueba con otro");
                            }else{
                                // Inform the user about the error trying to authenticate with google
                                alertDialog("Error", "Error al acceder a la base de datos", "Aceptar");

                                // Put the error in the log
                                Log.e("NameSelectionActivity", "Error accessing the database");
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Shows a dialog to inform the user.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog.
     * @param button The text for the positive button.
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
