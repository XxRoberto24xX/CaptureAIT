package com.captureait.captureait.view.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Allows the user to manage the validation of his account's email.
 */
public class ValidationActivity extends AppCompatActivity {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private Button btnNewEmail, btnNewRegister;
    private Timer timer = new Timer();

    /** Working progressbar */
    private ProgressDialog mProgressBar;

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
        setContentView(R.layout.activity_validation);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Take the visual elements using de id
        btnNewEmail = findViewById(R.id.btnNewEmail);
        btnNewRegister = findViewById(R.id.btnNewRegister);

        // We create the progress bar before showing it
        mProgressBar = new ProgressDialog(ValidationActivity.this);

        // Setting click listeners
        btnNewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.resendVerificationEmail(new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {
                        // We inform the user a new mail had been send
                        alertDialog("Informacion", "Se envio un nuevo correo de verificacion, mire su bandeja de entrada", "Aceptar");
                    }

                    @Override
                    public void onError(String error) {
                        // We inform the user about the error
                        alertDialog("Informacion", "Huvo algún error al enviar el correro, intentelo de nuevo mas tarde", "Aceptar");
                    }
                });
            }
        });

        btnNewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the progressBar
                mProgressBar.setTitle("Reintento de registro");
                mProgressBar.setMessage("Reseteando el registro de usuario, espere un momento..");
                mProgressBar.setCanceledOnTouchOutside(false);
                mProgressBar.show();

                // We take the password from cache
                SharedPreferences pref = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                String password = pref.getString("password", "");

                // Delete the user
                controller.deleteActualUserPassword(password, new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {
                        Intent loginActivity = new Intent(ValidationActivity.this, RegisterActivity.class);
                        startActivity(loginActivity);
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        mProgressBar.dismiss();

                        // Inform the user about the error
                        alertDialog("Error", "No se pudo borrar el usuario intentelo de nuevo", "Aceptar");

                        // Register the error
                        Log.e("ValidationActivity", "Error when deleting the user");
                    }
                });
            }
        });

        // Create a timer to refresh the user and the email validation flag
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                controller.getUserState(new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {
                        // See if the user is ready
                        if(info.equals("USER_READY")){
                            // Cancel the timer
                            timer.cancel();

                            // Inform the rest of the activities the player is ready
                            Intent intent = new Intent(USER_READY);
                            sendBroadcast(intent);

                            // Continue to the home menu
                            Intent homeActivityIntent  = new Intent(ValidationActivity.this, HomeActivity.class);
                            startActivity(homeActivityIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Cancel the timer
                        timer.cancel();

                        // Inform the user about the error
                        alertDialog("Error", "Error al recuperar la información del usuario", "Aceptar");

                        // Put the error in the log
                        Log.e("ValidationActivity", "Unable to take the user friends information");
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0 , 2000);

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

    /**
     * Called when the view is been destroyed, cancels the timer before it finishes.
     */
    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
