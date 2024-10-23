package com.captureait.captureait.view.userData;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.captureait.captureait.R;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.captureait.captureait.view.authentication.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Allows the user to manage his session options
 */
public class PasswordSessionActivity extends AppCompatActivity {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private Button btnDeleteAct, btnPassword, btnName;
    private TextView txtuserName, userEmail, userID;
    private TextInputEditText labelName, labelPassword;
    private ImageView btnBack;

    /** Working progressbar */
    private ProgressDialog mProgressBar;

    /** Activity events */
    private final String ACCOUNT_OUT = "ACCOUNT_OUT";

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
        setContentView(R.layout.activity_sesion_password);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Searching the visual elements
        btnDeleteAct = findViewById(R.id.btnEliminarCta);
        btnPassword = findViewById(R.id.btnPassword);
        btnName = findViewById(R.id.btnName);
        btnBack = findViewById(R.id.btnBack);

        txtuserName = findViewById(R.id.userNombre);
        userEmail = findViewById(R.id.userEmail);
        userID = findViewById(R.id.userId);

        labelName = findViewById(R.id.labelName);
        labelPassword = findViewById(R.id.labelPassword);

        // Setting the user information in the screen
        txtuserName.setText(controller.getUserName());
        userEmail.setText(controller.getEmail());
        userID.setText(controller.getUserId());

        // We create the progress bar before showing it
        mProgressBar = new ProgressDialog(PasswordSessionActivity.this);

        // Setting click listeners
        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset password for the user
                controller.resetPassword(controller.getEmail(), new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {
                        // Inform the user the mail has been sent
                        alertDialog("Email Send", "El correo fue enviado correctamente a: " + controller.getEmail(), "Aceptar");
                    }

                    @Override
                    public void onError(String error) {
                        // Inform the user about the error
                        alertDialog("Email Send", "Error en el envío del correo, intetelo de nuevo más tarde", "Aceptar");

                        // Put the error in the log
                        Log.e("SesionActivity", "Unable to send the email");
                    }
                });
            }
        });

        btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Take the new user name
                String userName = labelName.getText().toString();

                if(userName.isEmpty() || userName.length() < 5 || userName.length() > 15){
                    labelName.setError("El nombre de usuario debe tener almenos 5 caracteres y maximo 15");
                }else{
                    // Show the progressBar
                    mProgressBar.setTitle("NameChange");
                    mProgressBar.setMessage("Realizando Cambios");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();

                    controller.changeUserName(userName, new CentralControllerCallBack() {
                        @Override
                        public void onSuccess(String info) {
                            mProgressBar.dismiss();

                            // Change the name in the view
                            runOnUiThread(() -> {
                                txtuserName.setText(controller.getUserName());
                            });
                        }

                        @Override
                        public void onError(String error) {
                            mProgressBar.dismiss();

                            switch (error){
                                case "NAME_IN_USE":
                                    labelName.setError("Nombre de usuario en uso, prueba con otro");
                                    break;
                                default:
                                    // Inform the user about the error
                                    alertDialog("Name change", "Error en el cambio del mensaje", "Aceptar");

                                    // Put the error in the log
                                    Log.e("SesiónActivity", "Unable to send the email");
                                    break;
                            }
                        }
                    });
                }
            }
        });

        btnDeleteAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = labelPassword.getText().toString();
                if(password.isEmpty()){
                    labelPassword.setError("Debe primero confirmar la contraseña");
                }else if(password.length() < 7){
                    labelPassword.setError("Clave no valida, minimo 7 caracteres");
                }else{
                    // First make the user confirm the action
                    confirmDeleteDialog("Delete Account", "¿Está seguro que desea borrar la cuenta?", "Aceptar", "Cancelar", password);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Just Finish the app
                finish();
            }
        });
    }

    /**
     * Shows a dialog to confirm an action of deletin the account
     *
     * @param title Title of the dialog
     * @param message Message to display in the dialog
     * @param acceptButton Text for the accept button in the dialog
     * @param cancelButton Text for the cancel button in the dialog
     */
    public void confirmDeleteDialog(String title, String message, String acceptButton, String cancelButton, String password){
        // Building the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create the content of the AlertDialog
        builder.setTitle(title)
                .setMessage(message)

                .setPositiveButton(acceptButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Closes the dialog
                        dialogInterface.cancel();

                        deleteActUserPassword(password);
                    }
                })

                .setNegativeButton(cancelButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })

                .create()
                .show();
    }

    /**
     * Method used to delete user_password account.
     *
     * @param password string posted by the user with his password
     */
    public void deleteActUserPassword(String password){
        // Show the progressBar
        mProgressBar.setTitle("Borrando Cuenta");
        mProgressBar.setMessage("Borrando Cuenta, espere un momento..");
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.show();

        controller.deleteActualUserPassword(password, new CentralControllerCallBack() {
            @Override
            public void onSuccess(String info) {
                mProgressBar.dismiss();

                // Inform the rest of the activities the user left
                Intent intent = new Intent(ACCOUNT_OUT);
                sendBroadcast(intent);

                // Go back to login
                Intent loginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginActivityIntent);
                finish();
            }

            @Override
            public void onError(String error) {
                mProgressBar.dismiss();

                switch (error){
                    case "REAUTHENTICATION_FAILED":
                        labelPassword.setError("Contraseña incorrecta");
                        break;
                    default:
                        // Inform the user about the error
                        alertDialog("Delete Account", "Error al borrar la cuenta", "Aceptar");

                        // Put the error in the log
                        Log.e("SesiónActivity", "Error accessing the database");
                        break;
                }
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
