package com.captureait.captureait.view.authentication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.captureait.captureait.view.mainMenu.HomeActivity;

/**
 * Starting class which sets the principal elements to start the app.
 */
public class StartActivity extends AppCompatActivity implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

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
        setContentView(R.layout.activity_start);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Subscribe to his initialization event, initialize it and wait to the respond event to be launched
        controller.listenInitializationFinished(this);
        controller.initializeCentralController();
    }

    /**
     * Called when the initialization has finished and you are a listener of the event.
     * This method is implemented from the ActiveViewListener interface.
     * @param info The information string indicating the result of initialization.
     */
    @Override
    public void onInitializationFinished(String info) {
        // See if the initialization went right
        if(info.equals("CORRECT")){
            // If it was successful, try the connection to socketIO server
            tryConnection();
        }else{
            // If there was any error, inform the user
            centralControllerCreationalertDialog("Error de Carga", "Error al cargar la aplicación", "Aceptar");
        }

    }

    /**
     * Tries the connection to socketIO server.
     */
    private void tryConnection(){
        // Try to connect to the server
        controller.socketConnection(new CentralControllerCallBack() {
            @Override
            public void onSuccess(String info) {
                if(info.equals("USER_READY")){
                    // There is an active user and has name
                    Intent homeActivityIntent  = new Intent(StartActivity.this, HomeActivity.class);
                    startActivity(homeActivityIntent);
                    finish();
                }else{
                    // There is no active user, or it has no name or verified email
                    Intent loginActivityIntent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(loginActivityIntent);
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                if(error.equals("CONNECTION_TIMEOUT")){
                    // Inform the user about the error
                    connectionAlertDialog("Error de Conexion", "No se pudo conectar con el servidor intentelo de nuevo", "Aceptar");

                    // Put the error in the log
                    Log.e("StartActivity", "Server connection error");
                }else{
                    // Inform the user about the error
                    connectionAlertDialog("Error", "Error al recuperar la información de las partidas del servidor", "Aceptar");

                    // Put the error in the log
                    Log.e("Login Activity", "Unable to take the user games information");
                }
            }
        });

    }

    /**
     * Creates and shows an alert dialog to inform the user about the socket connection error and retries it.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog.
     * @param button The text for the positive button.
     */
    public void connectionAlertDialog(String title, String message, String button){
        // Building the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create the content of the AlertDialog
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();

                        tryConnection();
                    }
                })
                .create()
                .show();
    }

    /**
     * Creates and shows an alert dialog to inform there was a problem initializing the app and restarts the activity.
     * @param title The title of the dialog.
     * @param message The message to display in the dialog.
     * @param button The text for the positive button.
     */
    public void centralControllerCreationalertDialog(String title, String message, String button){
        // Building the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create the content of the AlertDialog
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();

                        Intent startActivityIntent = new Intent(StartActivity.this, StartActivity.class);
                        startActivity(startActivityIntent);
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * Called when there was an update in the model and you are a listener of the event.
     * This method is implemented from the ActiveViewListener interface.
     * @param updatedElement The element that was updated.
     */
    @Override
    public void onUpdate(String updatedElement) {

    }
}
