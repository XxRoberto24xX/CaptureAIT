package com.captureait.captureait.view.authentication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.captureait.captureait.view.mainMenu.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Authentication main class which allows the user to log in, reset his password if he has forgotten or go to the registration activity.
 */
public class LoginActivity extends AppCompatActivity{

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private Button btnLogin, btnGoogle;
    private TextView txtNoAccount, txtResetPassword;
    private TextInputEditText labelEmail, labelPassword;

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
        setContentView(R.layout.activity_login);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Searching the visual elements
        btnLogin = findViewById(R.id.btnlogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtNoAccount = findViewById(R.id.txtNotieneCuenta);
        txtResetPassword = findViewById(R.id.txtResetPass);
        labelEmail = findViewById(R.id.labelEmail);
        labelPassword = findViewById(R.id.labelPassword);

        // We create the progress bar before showing it
        mProgressBar = new ProgressDialog(LoginActivity.this);

        // Set the listener for the broadcast events between activities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, new IntentFilter(USER_READY), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, new IntentFilter(USER_READY));
        }

        // Setting click listeners
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verify the credentials and continue with google
                credentialVerification();
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creation of the google Sign In options
                GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, options);

                // Make the intent to google sign in and wait for the response
                Intent googleSignInClientSignInIntent = googleSignInClient.getSignInIntent();
                googleResultLauncher.launch(googleSignInClientSignInIntent);
            }
        });

        txtResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the reset password activity
                Intent resertPasswordActivity = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(resertPasswordActivity);
            }
        });

        txtNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the register activity
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerActivity);
            }
        });

    }

    /**
     * Handler for the Google sign-in intent result. Processes the result of the Google sign-in attempt,
     * including authentication and registration if necessary.
     */
    private final ActivityResultLauncher<Intent> googleResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            // If the intent was ok we need to actually sign in and register if needed in Firebase
            if(o.getResultCode() == RESULT_OK){
                // Take the task resulting data
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(o.getData());

                // Show the progressBar
                mProgressBar.setTitle("Login");
                mProgressBar.setMessage("Iniciando sesión, espere un momento..");
                mProgressBar.setCanceledOnTouchOutside(false);
                mProgressBar.show();

                // Try to authenticate the user with the information given and registers him if he does not exist
                controller.googleAuthentication(accountTask, new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {
                        mProgressBar.dismiss();

                        switch (info){
                            // The authenticated user has no userName
                            case "NO_NAME":
                                mProgressBar.dismiss();
                                // The user has no userName so he has to get one
                                Intent nameSelectionActivityIntent = new Intent(LoginActivity.this, NameSelectionActivity.class);
                                startActivity(nameSelectionActivityIntent);
                                break;

                            // The authenticated user has userName
                            case "USER_READY":
                                mProgressBar.dismiss();
                                Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(homeActivityIntent);
                                finish();
                                break;
                        }
                    }

                    @Override
                    public void onError(String error) {
                        mProgressBar.dismiss();

                        if(error.equals("AUTHENTICATION_FAILED")){
                            // Inform the user about the error trying to authenticate with google
                            alertDialog("Error", "Error al iniciar sesión con Google, intentelo de nuevo", "Aceptar");

                            // Put the error in the log
                            Log.e("Login Activity", "Unable to make Google authentication");
                        }else{
                            // Inform the user about the error trying to authenticate with google
                            alertDialog("Error", "Error al recuperar la información de las partidas del servidor", "Aceptar");

                            // Put the error in the log
                            Log.e("Login Activity", "Unable to take the user games information");
                        }
                    }
                });
            }else{
                // Inform the user about the error trying to authenticate with google
                alertDialog("Error", "Error al iniciar sesión con Google, intentelo de nuevo", "Aceptar");

                // Put the error in the log
                Log.e("Login Activity", "Unable to make Google authentication");
            }
        }
    });

    /**
     * Called to verify if the introduced information is correct.
     * Takes the user input from email and password fields, validates the format of the form,
     * and attempts authentication with the provided credentials.
     */
    public void credentialVerification(){
        // Taking the user input
        String email = labelEmail.getText().toString();
        String password = labelPassword.getText().toString();

        // Validate the format of the form
        if (email.isEmpty() || !email.contains("@")) {
            labelEmail.setError("Email no valido");
        } else if (password.isEmpty() || password.length() < 7) {
            labelPassword.setError("Clave no valida, minimo 7 caracteres");
        } else {
            // Show the progressBar
            mProgressBar.setTitle("Login");
            mProgressBar.setMessage("Iniciando sesión, espere un momento..");
            mProgressBar.setCanceledOnTouchOutside(false);
            mProgressBar.show();

            // Try the authentication with email and password
            controller.passwordAuthentication(email, password, new CentralControllerCallBack() {
                @Override
                public void onSuccess(String info) {
                    mProgressBar.dismiss();
                    switch (info){
                        // The authenticated user's email is not validated yet
                        case "NO_USER_OR_VALIDATION":
                            Intent validationActivityIntent = new Intent(LoginActivity.this, ValidationActivity.class);
                            startActivity(validationActivityIntent);
                            break;

                        // The authenticated user has a validated email
                        case "USER_READY":
                            Intent homeActivityIntent  = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(homeActivityIntent);
                            finish();
                            break;
                    }
                }

                @Override
                public void onError(String error) {
                    // Take off the progressBar
                    mProgressBar.dismiss();

                    if(error.equals("WRONG_CREDENTIALS")) {
                        // Inform the user about the credentials error
                        alertDialog("Error", "Correo o contraseña incorrectos ", "Aceptar");

                        // Put the error in the log
                        Log.e("Login Activity", "Credentials don't match");
                    }else if(error.equals("AUTHENTICATION_FAILED")){
                        // Inform the user about the error trying to authenticate with credentials
                        alertDialog("Error", "Error al iniciar sesión con credenciales, intentelo de nuevo", "Aceptar");

                        // Put the error in the log
                        Log.e("Login Activity", "Unable to make credentials authentication");
                    }else{
                        // Inform the user about the error trying to authenticate with credentials
                        alertDialog("Error", "Error al recuperar la información de las partidas del servidor", "Aceptar");

                        // Put the error in the log
                        Log.e("Login Activity", "Unable to take the user games information");
                    }
                }
            });
        }
    }

    /**
     * Shows a dialog to inform the user.
     *
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
     * Called when the view is destroyed, unregisters the broadcast receiver.
     * Overrides the onDestroy method of the superclass.
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

}