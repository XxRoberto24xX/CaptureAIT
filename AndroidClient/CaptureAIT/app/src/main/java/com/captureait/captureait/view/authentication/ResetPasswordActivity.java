package com.captureait.captureait.view.authentication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.google.android.material.textfield.TextInputEditText;

/* Allows the user to register int he application database */
public class ResetPasswordActivity extends AppCompatActivity{

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private TextInputEditText labelEmail;
    private Button btnSend;

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
        setContentView(R.layout.activity_reset_password);

        /* Take the controller instance */
        controller = CentralController.getInstance();

        /* Searching for the visual elements */
        btnSend = findViewById(R.id.btnSend);
        labelEmail = findViewById(R.id.labelEmail);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Taking the user input */
                String email = labelEmail.getText().toString();
                if (email.isEmpty() || !email.contains("@")){
                    labelEmail.setError("Formato no valido");
                }else{
                    controller.resetPassword(email, new CentralControllerCallBack() {
                        @Override
                        public void onSuccess(String info) {
                            /* Inform the user the mail has been send */
                            alertDialog("Email Send", "El correo fue enviado correctamente a: " + email, "Aceptar");
                        }

                        @Override
                        public void onError(String error) {
                            if(error.equals("MAIL_NOT_REGISTERED")){
                                /* Inform the user the mail has been sent */
                                labelEmail.setError("Email desconocido");
                            }else{
                                /* Inform the user about the error */
                                alertDialog("Email Send", "Error en el envío del correo, intetelo de nuevo más tarde", "Aceptar");

                                /* Put the error in the log */
                                Log.e("Reset Password", "Unable to send the email");
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

                        /* Send the user to the previous activity*/
                        finish();
                    }
                })
                .create()
                .show();
    }
}