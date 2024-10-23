package com.captureait.captureait.view.authentication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Allows the user to register in the users database.
 */
public class RegisterActivity extends AppCompatActivity{

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private TextView txtHasAccount;
    private Button btnRegister;
    private TextInputEditText labelName, labelEmail, labelPassword, labelPassword2;

    /** Working progressbar */
    private ProgressDialog mProgressBar;

    /** Activity events */
    private final String USER_READY = "USER_READY";

    /** Receiver for event between activities */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(USER_READY)){
                finish();
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
        setContentView(R.layout.activity_register);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Take the visual elements using de id
        txtHasAccount = findViewById(R.id.alreadyHaveAccount);
        btnRegister = findViewById(R.id.btnRegister);
        labelName = findViewById(R.id.labelName);
        labelEmail = findViewById(R.id.labelEmail);
        labelPassword = findViewById(R.id.labelPassword);
        labelPassword2 = findViewById(R.id.labelPasswordRepeat);

        // We create the progress bar before showing it
        mProgressBar = new ProgressDialog(RegisterActivity.this);

        // Set the listener for the broadcast events between activities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, new IntentFilter(USER_READY), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, new IntentFilter(USER_READY));
        }

        // Setting click listeners
        txtHasAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                credentialVerification();
            }
        });
    }

    /**
     * Called to verify if the introduced information is correct and the registration can continue.
     */
    public void credentialVerification(){
        // Taking the user input
        String userName = labelName.getText().toString();
        String email = labelEmail.getText().toString();
        String password = labelPassword.getText().toString();
        String confirmPass = labelPassword2.getText().toString();

        // Start verifying
        if(userName.isEmpty() || userName.length() < 5  || userName.length() > 15){
            labelName.setError("El nombre de usuario debe tener almenos 5 caracteres y maximo 15");
        } else if (email.isEmpty() || !email.contains("@") || !email.contains(".")){
            labelEmail.setError("Email no valido");
        } else if (password.isEmpty() || password.length() < 7){
            labelPassword.setError("Clave no valida, minimo 7 caracteres");
        } else if (confirmPass.isEmpty() || !confirmPass.equals(password)){
            labelPassword2.setError("Clave no valida o no coincide.");
        } else {
            // Save the password temporarily in case we need to redo the register
            SharedPreferences pref = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("password", password);
            editor.commit();

            // Show the progressBar
            mProgressBar.setTitle("Registering");
            mProgressBar.setMessage("Registrando al usuario, espere un momento");
            mProgressBar.setCanceledOnTouchOutside(false);
            mProgressBar.show();

            // Try to do the register
            controller.passwordRegister(email, password, userName, new CentralControllerCallBack() {
                @Override
                public void onSuccess(String info) {
                    mProgressBar.dismiss();

                    // The user is registered so he needs to validate email using the link sent to his account
                    Intent validationActivityIntent = new Intent(RegisterActivity.this, ValidationActivity.class);
                    startActivity(validationActivityIntent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    mProgressBar.dismiss();

                    switch (error){
                        // The userName introduced is in use
                        case "NAME_IN_USE":
                            labelName.setError("Nombre de usuario en uso, prueba con otro");
                            break;

                        // The mail introduced is already in use
                        case "EMAIL_IN_USE":
                            labelEmail.setError("Email ya en uso");
                            break;

                        // Error sending the verification email
                        case "VERIFICATION_EMAIL_ERROR":
                            alertDialog("Error", "Usuario registrado, pero no se puedo enviar el correo de verificación", "Aceptar");
                            break;

                        // There was a database error
                        default:
                            alertDialog("Error", "No se puedo conectar con la base de datos, intentelo de nuevo", "Aceptar");
                            break;
                    }
                }
            });
        }
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
     * Called when the view is been destroyed,  unregisters the broadcast receiver.
     * @Override
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
